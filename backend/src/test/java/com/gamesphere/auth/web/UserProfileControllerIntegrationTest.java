package com.gamesphere.auth.web;

import com.gamesphere.auth.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserProfileControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void getProfileShouldReturnAuthenticatedUser() {
        String token = login("profileuser", "profile@example.com", "Test@12345");

        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/v1/users/me"), HttpMethod.GET, bearer(token), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map data = (Map) response.getBody().get("data");
        assertThat(data).containsEntry("username", "profileuser");
        assertThat(data).containsEntry("email", "profile@example.com");
        assertThat(data).containsEntry("displayName", "Profile User");
    }

    @Test
    void getProfileWithoutJwtShouldReturnUnauthorized() {
        ResponseEntity<Map> response = restTemplate.getForEntity(url("/api/v1/users/me"), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void updateProfileShouldChangeEmailAndDisplayName() {
        String token = login("updateuser", "update@example.com", "Test@12345");

        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/v1/users/me"), HttpMethod.PUT, bearerJson(token,
                        "{\"email\":\"updated@example.com\",\"displayName\":\"Updated User\"}"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("success", true);
        assertThat(userRepository.findByUsername("updateuser").orElseThrow().getEmail())
                .isEqualTo("updated@example.com");
    }

    @Test
    void updateProfileWithDuplicateEmailShouldReturnBadRequest() {
        login("firstuser", "shared@example.com", "Test@12345");
        String token = login("seconduser", "second@example.com", "Test@12345");

        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/v1/users/me"), HttpMethod.PUT, bearerJson(token,
                        "{\"email\":\"shared@example.com\",\"displayName\":\"Second User\"}"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void changePasswordShouldAllowLoginWithNewPassword() {
        String token = login("passworduser", "password@example.com", "Old@12345");

        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/v1/users/me/password"), HttpMethod.PUT, bearerJson(token,
                        "{\"currentPassword\":\"Old@12345\",\"newPassword\":\"New@12345\"}"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Map> login = restTemplate.postForEntity(
                url("/api/v1/auth/login"), json("usernameOrEmail", "passworduser", "password", "New@12345"), Map.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void changePasswordWithWrongCurrentPasswordShouldReturnUnauthorized() {
        String token = login("wrongcurrent", "wrongcurrent@example.com", "Correct@12345");

        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/v1/users/me/password"), HttpMethod.PUT, bearerJson(token,
                        "{\"currentPassword\":\"Wrong@12345\",\"newPassword\":\"New@12345\"}"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void changePasswordWithoutJwtShouldReturnUnauthorized() {
        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/v1/users/me/password"), HttpMethod.PUT,
                json("currentPassword", "Old@12345", "newPassword", "New@12345"), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private String login(String username, String email, String password) {
        ResponseEntity<Map> register = restTemplate.postForEntity(
                url("/api/v1/auth/register"),
                json("username", username, "email", email, "password", password, "displayName", username.equals("profileuser") ? "Profile User" : "Test User"),
                Map.class);
        assertThat(register.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<Map> login = restTemplate.postForEntity(
                url("/api/v1/auth/login"),
                json("usernameOrEmail", username, "password", password), Map.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        return (String) ((Map) login.getBody().get("data")).get("accessToken");
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
            body.append('"').append(values[i]).append("\":\"").append(values[i + 1]).append('"');
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
