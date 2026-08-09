package com.gamesphere.games.web;

import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.games.repository.GameRepository;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class GameControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanUp() {
        gameRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void gamesListShouldBePublic() {
        ResponseEntity<Map> response = restTemplate.getForEntity(url("/api/v1/games"), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("success", true);
    }

    @Test
    void createGetUpdateAndDeleteGameShouldWorkForAuthenticatedUser() {
        String token = loginAs("gameuser", "game@example.com");

        ResponseEntity<Map> create = exchange("/api/v1/games", HttpMethod.POST, token, gameJson("ashen-crown", "Ashen Crown"));
        assertThat(create.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<Map> get = exchange("/api/v1/games/ashen-crown", HttpMethod.GET, token, null);
        assertThat(get.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map data = (Map) get.getBody().get("data");
        assertThat(data.get("title")).isEqualTo("Ashen Crown");

        ResponseEntity<Map> update = exchange("/api/v1/games/ashen-crown", HttpMethod.PUT, token, gameJson("ashen-crown", "Ashen Crown Updated"));
        assertThat(update.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((Map) update.getBody().get("data")).get("title")).isEqualTo("Ashen Crown Updated");

        ResponseEntity<Map> delete = exchange("/api/v1/games/ashen-crown", HttpMethod.DELETE, token, null);
        assertThat(delete.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(gameRepository.findById("ashen-crown")).isEmpty();
    }

    @Test
    void gameCreationWithoutJwtShouldBeRejected() {
        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/v1/games"),
                HttpMethod.POST,
                new HttpEntity<>(gameJson("unauthenticated-game", "Unauthenticated Game")),
                Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void duplicateGameIdShouldBeRejected() {
        String token = loginAs("duplicategameuser", "duplicategame@example.com");
        ResponseEntity<Map> first = exchange("/api/v1/games", HttpMethod.POST, token, gameJson("duplicate-game", "Duplicate Game"));
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<Map> second = exchange("/api/v1/games", HttpMethod.POST, token, gameJson("duplicate-game", "Duplicate Game"));
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void unknownGameShouldReturnNotFound() {
        ResponseEntity<Map> response = restTemplate.getForEntity(url("/api/v1/games/missing-game"), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private String loginAs(String username, String email) {
        ResponseEntity<Map> register = restTemplate.postForEntity(
                url("/api/v1/auth/register"),
                json("username", username, "email", email, "password", "Test@12345", "displayName", "Game Tester"),
                Map.class);
        assertThat(register.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<Map> login = restTemplate.postForEntity(
                url("/api/v1/auth/login"),
                json("usernameOrEmail", username, "password", "Test@12345"),
                Map.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        return (String) ((Map) login.getBody().get("data")).get("accessToken");
    }

    private ResponseEntity<Map> exchange(String path, HttpMethod method, String token, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange(url(path), method, new HttpEntity<>(body, headers), Map.class);
    }

    private HttpEntity<String> json(String... values) {
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

    private String gameJson(String id, String title) {
        return """
                {
                  "id": "%s",
                  "title": "%s",
                  "description": "A test game description.",
                  "rating": 8.5,
                  "reviewCount": 100,
                  "price": 49.99,
                  "releaseYear": 2026,
                  "developer": "Test Studio",
                  "publisher": "Test Publisher",
                  "genres": ["Action", "RPG"],
                  "platforms": ["PC"],
                  "tags": ["Open World"],
                  "languages": ["English"],
                  "features": ["Single Player"],
                  "stores": [],
                  "requirements": []
                }
                """.formatted(id, title);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
