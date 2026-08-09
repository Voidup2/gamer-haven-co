package com.gamesphere.auth.web;

import com.gamesphere.auth.domain.Role;
import com.gamesphere.auth.repository.RoleRepository;
import com.gamesphere.auth.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
class AuthControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        if (roleRepository.findByName("USER").isEmpty()) {
            roleRepository.save(new Role("USER"));
        }
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void registrationShouldCreateUser() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/v1/auth/register"),
                json("username", "integrationuser", "email", "integration@example.com",
                        "password", "Test@12345", "displayName", "Integration User"),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsEntry("success", true);
        assertThat(userRepository.findByUsername("integrationuser")).isPresent();
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
        assertThat(data).containsKeys("accessToken", "tokenType", "expiresIn", "userId", "username");
        assertThat(data.get("accessToken")).isInstanceOf(String.class).isNotEqualTo("");
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

        ResponseEntity<Map> login = restTemplate.postForEntity(
                url("/api/v1/auth/login"),
                json("usernameOrEmail", "protecteduser", "password", "Test@12345"),
                Map.class);

        Map data = (Map) login.getBody().get("data");
        String token = (String) data.get("accessToken");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/v1/users/me"), HttpMethod.GET, request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("success", true);
    }

    private void register(String username, String email) {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/v1/auth/register"),
                json("username", username, "email", email,
                        "password", "Test@12345", "displayName", "Test User"),
                Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    private HttpEntity<String> json(Object... values) {
        StringBuilder body = new StringBuilder("{");
        for (int i = 0; i < values.length; i += 2) {
            if (i > 0) body.append(',');
            body.append('"').append(values[i]).append("\":\"")
                    .append(values[i + 1]).append('"');
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
