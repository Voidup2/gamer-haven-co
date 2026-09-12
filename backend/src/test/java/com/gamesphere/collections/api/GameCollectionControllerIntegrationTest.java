package com.gamesphere.collections.api;

import com.gamesphere.activity.repository.UserActivityRepository;
import com.gamesphere.auth.domain.Role;
import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.RoleRepository;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.collections.domain.GameCollection;
import com.gamesphere.collections.repository.GameCollectionRepository;
import org.junit.jupiter.api.AfterEach;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class GameCollectionControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private GameCollectionRepository collectionRepository;

    @Autowired
    private UserActivityRepository userActivityRepository;

    @AfterEach
    void cleanUp() {
        collectionRepository.deleteAll();
        userActivityRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createCollectionWithoutJwtShouldReturnUnauthorized() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/v1/collections"),
                json(
                        "name", "Private Collection",
                        "description", "Test collection",
                        "publicCollection", "false"
                ),
                Map.class
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void userShouldCreateOwnCollection() {
        String token = login("collectionuser", "collection@example.com");

        ResponseEntity<Map> response = exchange(
                "/api/v1/collections",
                HttpMethod.POST,
                token,
                "{\"name\":\"My Collection\",\"description\":\"My games\",\"publicCollection\":false}"
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        assertThat(collectionRepository.findAll())
                .hasSize(1);

        assertThat(collectionRepository.findAll().get(0).getName())
                .isEqualTo("My Collection");
    }

    @Test
    void userShouldNotUpdateAnotherUsersCollection() {
        login("collectionowner", "owner@example.com");
        String otherToken = login("collectionother", "other@example.com");

        User owner = userRepository.findByUsername("collectionowner")
                .orElseThrow();

        GameCollection collection = collectionRepository.save(
                new GameCollection(
                        owner,
                        "Owner Collection",
                        "Owner collection",
                        false
                )
        );

        ResponseEntity<Map> response = exchange(
                "/api/v1/collections/" + collection.getId(),
                HttpMethod.PUT,
                otherToken,
                "{\"name\":\"Hacked Collection\",\"description\":\"Changed\",\"publicCollection\":false}"
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        assertThat(collectionRepository.findById(collection.getId()).orElseThrow().getName())
                .isEqualTo("Owner Collection");
    }

    @Test
    void userShouldNotDeleteAnotherUsersCollection() {
        login("collectiondeleteowner", "deleteowner@example.com");
        String otherToken = login("collectiondeleteother", "deleteother@example.com");

        User owner = userRepository.findByUsername("collectiondeleteowner")
                .orElseThrow();

        GameCollection collection = collectionRepository.save(
                new GameCollection(
                        owner,
                        "Protected Collection",
                        "Protected collection",
                        false
                )
        );

        ResponseEntity<Map> response = exchange(
                "/api/v1/collections/" + collection.getId(),
                HttpMethod.DELETE,
                otherToken,
                null
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        assertThat(collectionRepository.findById(collection.getId()))
                .isPresent();
    }

    private String login(String username, String email) {

        roleRepository.findByName("USER")
                .orElseGet(() -> roleRepository.save(new Role("USER")));

        ResponseEntity<Map> register = restTemplate.postForEntity(
                url("/api/v1/auth/register"),
                json(
                        "username", username,
                        "email", email,
                        "password", "Test@12345",
                        "displayName", "Collection Tester"
                ),
                Map.class
        );

        assertThat(register.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        User user = userRepository.findByUsername(username)
                .orElseThrow();

        user.setEmailVerified(true);
        userRepository.saveAndFlush(user);

        ResponseEntity<Map> login = restTemplate.postForEntity(
                url("/api/v1/auth/login"),
                json(
                        "usernameOrEmail", username,
                        "password", "Test@12345"
                ),
                Map.class
        );

        assertThat(login.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        return (String) ((Map) login.getBody()
                .get("data"))
                .get("accessToken");
    }

    private ResponseEntity<Map> exchange(
            String path,
            HttpMethod method,
            String token,
            String body) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        return restTemplate.exchange(
                url(path),
                method,
                new HttpEntity<>(body, headers),
                Map.class
        );
    }

    private HttpEntity<String> json(String... values) {

        StringBuilder body = new StringBuilder("{");

        for (int i = 0; i < values.length; i += 2) {
            if (i > 0) {
                body.append(',');
            }

            String value = values[i + 1];

            body.append('"')
                    .append(values[i])
                    .append("\":");

            if ("true".equals(value) || "false".equals(value)) {
                body.append(value);
            } else {
                body.append('"')
                        .append(value)
                        .append('"');
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