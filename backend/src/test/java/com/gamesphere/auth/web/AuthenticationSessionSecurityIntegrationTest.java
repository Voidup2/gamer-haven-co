package com.gamesphere.auth.web;

import com.gamesphere.auth.domain.Role;
import com.gamesphere.auth.repository.RoleRepository;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.auth.service.EmailSender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthenticationSessionSecurityIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @MockBean
    private EmailSender emailSender;

    @BeforeEach
    void setUp() {
        roleRepository.findByName("USER").orElseGet(() -> roleRepository.save(new Role("USER")));
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void passwordChangeShouldInvalidateExistingAccessAndRefreshTokens() {
        registerAndVerify("sessionpassword", "sessionpassword@example.com", "Old@12345");
        Map login = login("sessionpassword", "Old@12345");
        String accessToken = (String) login.get("accessToken");
        String refreshToken = (String) login.get("refreshToken");

        ResponseEntity<Map> changeResponse = restTemplate.exchange(
                url("/api/v1/users/me/password"),
                HttpMethod.PUT,
                bearerJson(accessToken, "{\"currentPassword\":\"Old@12345\",\"newPassword\":\"New@12345\"}"),
                Map.class);

        assertThat(changeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Map> oldAccessResponse = restTemplate.exchange(
                url("/api/v1/users/me"), HttpMethod.GET, bearer(accessToken), Map.class);
        assertThat(oldAccessResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ResponseEntity<Map> oldRefreshResponse = restTemplate.postForEntity(
                url("/api/v1/auth/refresh"),
                json("refreshToken", refreshToken),
                Map.class);
        assertThat(oldRefreshResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ResponseEntity<Map> newLoginResponse = restTemplate.postForEntity(
                url("/api/v1/auth/login"),
                json("usernameOrEmail", "sessionpassword", "password", "New@12345"),
                Map.class);
        assertThat(newLoginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void emailChangeShouldInvalidateExistingAccessTokenAndRequireVerification() {
        registerAndVerify("sessionemail", "sessionemail@example.com", "Test@12345");
        Map login = login("sessionemail", "Test@12345");
        String accessToken = (String) login.get("accessToken");

        ResponseEntity<Map> updateResponse = restTemplate.exchange(
                url("/api/v1/users/me"),
                HttpMethod.PUT,
                bearerJson(accessToken, "{\"email\":\"changed@example.com\",\"displayName\":\"Changed User\"}"),
                Map.class);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(userRepository.findByUsername("sessionemail").orElseThrow().isEmailVerified()).isFalse();

        ResponseEntity<Map> oldAccessResponse = restTemplate.exchange(
                url("/api/v1/users/me"), HttpMethod.GET, bearer(accessToken), Map.class);
        assertThat(oldAccessResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ResponseEntity<Map> loginBeforeVerification = restTemplate.postForEntity(
                url("/api/v1/auth/login"),
                json("usernameOrEmail", "sessionemail", "password", "Test@12345"),
                Map.class);
        assertThat(loginBeforeVerification.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private void registerAndVerify(String username, String email, String password) {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/v1/auth/register"),
                json("username", username, "email", email, "password", password, "displayName", "Test User"),
                Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        var user = userRepository.findByUsername(username).orElseThrow();
        user.setEmailVerified(true);
        userRepository.saveAndFlush(user);
    }

    private Map login(String username, String password) {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/v1/auth/login"),
                json("usernameOrEmail", username, "password", password),
                Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return (Map) response.getBody().get("data");
    }

    private HttpEntity<String> bearer(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }

    private HttpEntity<String> bearerJson(String token, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
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

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
