package com.gamesphere.auth.web;

import com.gamesphere.auth.domain.EmailVerificationToken;
import com.gamesphere.auth.domain.Role;
import com.gamesphere.auth.repository.EmailVerificationTokenRepository;
import com.gamesphere.auth.repository.RoleRepository;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.auth.service.EmailSender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @MockBean
    private EmailSender emailSender;

    @BeforeEach
    void setUp() {
        reset(emailSender);
        if (roleRepository.findByName("USER").isEmpty()) {
            roleRepository.save(new Role("USER"));
        }
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void registrationShouldCreateUnverifiedUser() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/v1/auth/register"),
                json("username", "integrationuser", "email", "integration@example.com",
                        "password", "Test@12345", "displayName", "Integration User"),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsEntry("success", true);
        assertThat(userRepository.findByUsername("integrationuser")).isPresent();
        assertThat(userRepository.findByUsername("integrationuser").orElseThrow().isEmailVerified()).isFalse();
    }

    @Test
    void registrationShouldSendVerificationToken() {
        registerUnverified("verificationuser", "verification@example.com");

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSender).sendVerificationEmail(emailCaptor.capture(), tokenCaptor.capture());

        assertThat(emailCaptor.getValue()).isEqualTo("verification@example.com");
        assertThat(tokenCaptor.getValue()).isNotBlank();
        assertThat(emailVerificationTokenRepository.findAll()).hasSize(1);
        assertThat(emailVerificationTokenRepository.findAll().getFirst().getTokenHash())
                .isEqualTo(hash(tokenCaptor.getValue()));
    }

    @Test
    void emailVerificationShouldSucceedAndTokenShouldBecomeInvalid() {
        registerUnverified("verifyuser", "verify@example.com");
        String token = latestVerificationToken();

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/v1/auth/verify-email"),
                json("token", token),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(userRepository.findByUsername("verifyuser").orElseThrow().isEmailVerified()).isTrue();

        ResponseEntity<Map> reuseResponse = restTemplate.postForEntity(
                url("/api/v1/auth/verify-email"),
                json("token", token),
                Map.class);

        assertThat(reuseResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void invalidVerificationTokenShouldBeRejected() {
        registerUnverified("invalidverify", "invalidverify@example.com");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/v1/auth/verify-email"),
                json("token", "not-a-real-verification-token"),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(userRepository.findByUsername("invalidverify").orElseThrow().isEmailVerified()).isFalse();
    }

    @Test
    void expiredVerificationTokenShouldBeRejected() {
        registerUnverified("expiredverify", "expiredverify@example.com");
        var user = userRepository.findByUsername("expiredverify").orElseThrow();
        String expiredToken = "expired-verification-token";
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        emailVerificationTokenRepository.save(new EmailVerificationToken(
                UUID.randomUUID(),
                user,
                hash(expiredToken),
                now.minusMinutes(1),
                now.minusMinutes(2)
        ));

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/v1/auth/verify-email"),
                json("token", expiredToken),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(user.isEmailVerified()).isFalse();
    }

    @Test
    void alreadyVerifiedUserShouldRemainVerifiedWhenTokenIsReused() {
        registerUnverified("alreadyverified", "alreadyverified@example.com");
        String token = latestVerificationToken();

        ResponseEntity<Map> firstResponse = restTemplate.postForEntity(
                url("/api/v1/auth/verify-email"),
                json("token", token),
                Map.class);
        ResponseEntity<Map> secondResponse = restTemplate.postForEntity(
                url("/api/v1/auth/verify-email"),
                json("token", token),
                Map.class);

        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(userRepository.findByUsername("alreadyverified").orElseThrow().isEmailVerified()).isTrue();
    }

    @Test
    void unverifiedUserShouldNotBeAllowedToLogin() {
        registerUnverified("unverifiedlogin", "unverifiedlogin@example.com");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/v1/auth/login"),
                json("usernameOrEmail", "unverifiedlogin", "password", "Test@12345"),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void duplicateUsernameShouldBeRejected() {
        register("duplicateuser", "duplicate@example.com");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/v1/auth/register"),
                json("username", "duplicateuser", "email", "another@example.com",
                        "password", "Test@12345", "displayName", "Duplicate User"),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void loginShouldReturnJwt() {
        register("loginuser", "login@example.com");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/v1/auth/login"),
                json("usernameOrEmail", "loginuser", "password", "Test@12345"),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map data = (Map) response.getBody().get("data");
        assertThat(data).containsKeys("accessToken", "refreshToken", "tokenType", "expiresIn", "refreshExpiresIn", "userId", "username");
        assertThat(data.get("accessToken")).isInstanceOf(String.class).isNotEqualTo("");
        assertThat(data.get("refreshToken")).isInstanceOf(String.class).isNotEqualTo("");
        assertThat(data.get("tokenType")).isEqualTo("Bearer");
        assertThat(data.get("username")).isEqualTo("loginuser");
    }

    @Test
    void wrongPasswordShouldBeRejected() {
        register("wrongpassworduser", "wrongpassword@example.com");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/v1/auth/login"),
                json("usernameOrEmail", "wrongpassworduser", "password", "Wrong@12345"),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void protectedEndpointShouldRejectMissingJwt() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                url("/api/v1/users/me"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void protectedEndpointShouldAcceptValidJwt() {
        register("protecteduser", "protected@example.com");

        Map data = login("protecteduser");
        String token = (String) data.get("accessToken");

        ResponseEntity<Map> response = authenticatedRequest(token, HttpMethod.GET, "/api/v1/users/me", null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("success", true);
    }

    @Test
    void refreshShouldRotateRefreshTokenAndReturnNewAccessToken() {
        register("refreshuser", "refresh@example.com");
        Map login = login("refreshuser");
        String oldRefreshToken = (String) login.get("refreshToken");

        ResponseEntity<Map> refreshResponse = restTemplate.postForEntity(
                url("/api/v1/auth/refresh"),
                json("refreshToken", oldRefreshToken),
                Map.class);

        assertThat(refreshResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map refreshed = (Map) refreshResponse.getBody().get("data");
        assertThat(refreshed.get("accessToken")).isInstanceOf(String.class).isNotEqualTo("");
        assertThat(refreshed.get("refreshToken")).isInstanceOf(String.class).isNotEqualTo(oldRefreshToken);

        ResponseEntity<Map> oldTokenResponse = restTemplate.postForEntity(
                url("/api/v1/auth/refresh"),
                json("refreshToken", oldRefreshToken),
                Map.class);

        assertThat(oldTokenResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void logoutShouldRevokeSessionAndInvalidateAccessToken() {
        register("logoutuser", "logout@example.com");
        Map login = login("logoutuser");
        String accessToken = (String) login.get("accessToken");
        String refreshToken = (String) login.get("refreshToken");

        ResponseEntity<Map> logoutResponse = restTemplate.postForEntity(
                url("/api/v1/auth/logout"),
                json("refreshToken", refreshToken),
                Map.class);

        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Map> protectedResponse = authenticatedRequest(
                accessToken, HttpMethod.GET, "/api/v1/users/me", null);

        assertThat(protectedResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void logoutAllShouldRevokeAllUserSessions() {
        register("logoutalluser", "logoutall@example.com");

        Map firstLogin = login("logoutalluser");
        Map secondLogin = login("logoutalluser");
        String firstAccessToken = (String) firstLogin.get("accessToken");
        String secondAccessToken = (String) secondLogin.get("accessToken");

        ResponseEntity<Map> logoutAllResponse = authenticatedRequest(
                firstAccessToken, HttpMethod.POST, "/api/v1/auth/logout-all", null);

        assertThat(logoutAllResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Map> firstProtectedResponse = authenticatedRequest(
                firstAccessToken, HttpMethod.GET, "/api/v1/users/me", null);
        ResponseEntity<Map> secondProtectedResponse = authenticatedRequest(
                secondAccessToken, HttpMethod.GET, "/api/v1/users/me", null);

        assertThat(firstProtectedResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(secondProtectedResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void logoutAllShouldRequireAuthentication() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/v1/auth/logout-all"), null, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void rememberMeShouldUseLongerRefreshLifetime() {
        register("rememberuser", "remember@example.com");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/v1/auth/login"),
                json("usernameOrEmail", "rememberuser", "password", "Test@12345", "rememberMe", true),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map data = (Map) response.getBody().get("data");
        assertThat(((Number) data.get("refreshExpiresIn")).longValue()).isEqualTo(30L * 24 * 60 * 60);
    }

    @Test
    void invalidRefreshTokenShouldBeRejected() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/v1/auth/refresh"),
                json("refreshToken", "not-a-real-refresh-token"),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private Map login(String username) {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/v1/auth/login"),
                json("usernameOrEmail", username, "password", "Test@12345"),
                Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return (Map) response.getBody().get("data");
    }

    private void register(String username, String email) {
        registerUnverified(username, email);
        String token = latestVerificationToken();
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/v1/auth/verify-email"),
                json("token", token),
                Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private void registerUnverified(String username, String email) {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/v1/auth/register"),
                json("username", username, "email", email,
                        "password", "Test@12345", "displayName", "Test User"),
                Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    private String latestVerificationToken() {
        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSender).sendVerificationEmail(
                org.mockito.ArgumentMatchers.anyString(), tokenCaptor.capture());
        return tokenCaptor.getValue();
    }

    private ResponseEntity<Map> authenticatedRequest(
            String token, HttpMethod method, String path, HttpEntity<?> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<?> request = body == null
                ? new HttpEntity<>(headers)
                : new HttpEntity<>(body.getBody(), headers);
        return restTemplate.exchange(url(path), method, request, Map.class);
    }

    private HttpEntity<String> json(Object... values) {
        StringBuilder body = new StringBuilder("{");
        for (int i = 0; i < values.length; i += 2) {
            if (i > 0) body.append(',');
            body.append('"').append(values[i]).append("\":");
            Object value = values[i + 1];
            if (value instanceof Boolean || value instanceof Number) {
                body.append(value);
            } else {
                body.append('"').append(value).append('"');
            }
        }
        body.append('}');

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body.toString(), headers);
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(64);
            for (byte value : digest) {
                result.append(String.format("%02x", value));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
