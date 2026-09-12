package com.gamesphere.library.web;

import com.gamesphere.auth.domain.Role;
import com.gamesphere.activity.repository.UserActivityRepository;
import com.gamesphere.auth.repository.RoleRepository;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.games.domain.Game;
import com.gamesphere.games.repository.GameRepository;
import com.gamesphere.library.repository.UserGameLibraryRepository;
import com.gamesphere.library.repository.UserGameWishlistRepository;
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
import com.gamesphere.auth.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class LibraryControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserActivityRepository userActivityRepository;  

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserGameLibraryRepository libraryRepository;

    @Autowired
    private UserGameWishlistRepository wishlistRepository;

    @AfterEach
    void cleanUp() {
        wishlistRepository.deleteAll();
        libraryRepository.deleteAll();
        gameRepository.deleteAll();
        userRepository.findAll()
            .forEach(user -> userActivityRepository.deleteByUserId(user.getId()));
        userRepository.deleteAll();
    }

    @Test
    void addGameToLibraryShouldWork() {

        String token = login(
                "libraryuser",
                "library@example.com"
        );

        createGame(
                "library-game",
                "Library Game"
        );

        ResponseEntity<Map> response = exchange(
                "/api/v1/library/library-game",
                HttpMethod.POST,
                token,
                null
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        assertThat(response.getBody())
                .containsEntry("success", true);

        assertThat(libraryRepository.findAll())
                .hasSize(1);

        assertThat(libraryRepository.findAll().get(0).getGame().getId())
                .isEqualTo("library-game");
    }

    @Test
    void getLibraryShouldReturnUserGames() {

        String token = login(
                "librarylistuser",
                "librarylist@example.com"
        );

        createGame(
                "library-game-1",
                "Library Game One"
        );

        createGame(
                "library-game-2",
                "Library Game Two"
        );

        ResponseEntity<Map> firstAdd = exchange(
                "/api/v1/library/library-game-1",
                HttpMethod.POST,
                token,
                null
        );

        ResponseEntity<Map> secondAdd = exchange(
                "/api/v1/library/library-game-2",
                HttpMethod.POST,
                token,
                null
        );

        assertThat(firstAdd.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        assertThat(secondAdd.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        ResponseEntity<Map> response = exchange(
                "/api/v1/library",
                HttpMethod.GET,
                token,
                null
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .containsEntry("success", true);

        assertThat((List<?>) response.getBody().get("data"))
                .hasSize(2);
    }

    @Test
    void duplicateLibraryGameShouldReturnConflict() {

        String token = login(
                "libraryduplicate",
                "libraryduplicate@example.com"
        );

        createGame(
                "duplicate-library-game",
                "Duplicate Library Game"
        );

        ResponseEntity<Map> first = exchange(
                "/api/v1/library/duplicate-library-game",
                HttpMethod.POST,
                token,
                null
        );

        assertThat(first.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        ResponseEntity<Map> second = exchange(
                "/api/v1/library/duplicate-library-game",
                HttpMethod.POST,
                token,
                null
        );

        assertThat(second.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(libraryRepository.findAll())
                .hasSize(1);
    }

    @Test
    void removeGameFromLibraryShouldWork() {

        String token = login(
                "libraryremove",
                "libraryremove@example.com"
        );

        createGame(
                "remove-library-game",
                "Remove Library Game"
        );

        ResponseEntity<Map> add = exchange(
                "/api/v1/library/remove-library-game",
                HttpMethod.POST,
                token,
                null
        );

        assertThat(add.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        ResponseEntity<Map> response = exchange(
                "/api/v1/library/remove-library-game",
                HttpMethod.DELETE,
                token,
                null
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(libraryRepository.findAll())
                .isEmpty();
    }

    @Test
    void removingGameNotInLibraryShouldReturnNotFound() {

        String token = login(
                "librarymissing",
                "librarymissing@example.com"
        );

        ResponseEntity<Map> response = exchange(
                "/api/v1/library/missing-library-game",
                HttpMethod.DELETE,
                token,
                null
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void addGameToWishlistShouldWork() {

        String token = login(
                "wishlistuser",
                "wishlist@example.com"
        );

        createGame(
                "wishlist-game",
                "Wishlist Game"
        );

        ResponseEntity<Map> response = exchange(
                "/api/v1/wishlist/wishlist-game",
                HttpMethod.POST,
                token,
                null
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        assertThat(response.getBody())
                .containsEntry("success", true);

        assertThat(wishlistRepository.findAll())
                .hasSize(1);

        assertThat(wishlistRepository.findAll().get(0).getGame().getId())
                .isEqualTo("wishlist-game");
    }

    @Test
    void getWishlistShouldReturnUserGames() {

        String token = login(
                "wishlistlistuser",
                "wishlistlist@example.com"
        );

        createGame(
                "wishlist-game-1",
                "Wishlist Game One"
        );

        createGame(
                "wishlist-game-2",
                "Wishlist Game Two"
        );

        ResponseEntity<Map> firstAdd = exchange(
                "/api/v1/wishlist/wishlist-game-1",
                HttpMethod.POST,
                token,
                null
        );

        ResponseEntity<Map> secondAdd = exchange(
                "/api/v1/wishlist/wishlist-game-2",
                HttpMethod.POST,
                token,
                null
        );

        assertThat(firstAdd.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        assertThat(secondAdd.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        ResponseEntity<Map> response = exchange(
                "/api/v1/wishlist",
                HttpMethod.GET,
                token,
                null
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .containsEntry("success", true);

        assertThat((List<?>) response.getBody().get("data"))
                .hasSize(2);
    }

    @Test
    void duplicateWishlistGameShouldReturnConflict() {

        String token = login(
                "wishlistduplicate",
                "wishlistduplicate@example.com"
        );

        createGame(
                "duplicate-wishlist-game",
                "Duplicate Wishlist Game"
        );

        ResponseEntity<Map> first = exchange(
                "/api/v1/wishlist/duplicate-wishlist-game",
                HttpMethod.POST,
                token,
                null
        );

        assertThat(first.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        ResponseEntity<Map> second = exchange(
                "/api/v1/wishlist/duplicate-wishlist-game",
                HttpMethod.POST,
                token,
                null
        );

        assertThat(second.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(wishlistRepository.findAll())
                .hasSize(1);
    }

    @Test
    void removeGameFromWishlistShouldWork() {

        String token = login(
                "wishlistremove",
                "wishlistremove@example.com"
        );

        createGame(
                "remove-wishlist-game",
                "Remove Wishlist Game"
        );

        ResponseEntity<Map> add = exchange(
                "/api/v1/wishlist/remove-wishlist-game",
                HttpMethod.POST,
                token,
                null
        );

        assertThat(add.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        ResponseEntity<Map> response = exchange(
                "/api/v1/wishlist/remove-wishlist-game",
                HttpMethod.DELETE,
                token,
                null
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(wishlistRepository.findAll())
                .isEmpty();
    }

    @Test
    void removingGameNotInWishlistShouldReturnNotFound() {

        String token = login(
                "wishlistmissing",
                "wishlistmissing@example.com"
        );

        ResponseEntity<Map> response = exchange(
                "/api/v1/wishlist/missing-wishlist-game",
                HttpMethod.DELETE,
                token,
                null
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void libraryWithoutJwtShouldBeRejected() {

        ResponseEntity<Map> response =
                restTemplate.getForEntity(
                        url("/api/v1/library"),
                        Map.class
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void wishlistWithoutJwtShouldBeRejected() {

        ResponseEntity<Map> response =
                restTemplate.getForEntity(
                        url("/api/v1/wishlist"),
                        Map.class
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private String login(
        String username,
        String email
) {

    roleRepository.findByName("USER")
            .orElseGet(() ->
                    roleRepository.save(new Role("USER"))
            );

    ResponseEntity<Map> register =
            restTemplate.postForEntity(
                    url("/api/v1/auth/register"),
                    json(
                            "username", username,
                            "email", email,
                            "password", "Test@12345",
                            "displayName", "Library Tester"
                    ),
                    Map.class
            );

    assertThat(register.getStatusCode())
            .isEqualTo(HttpStatus.CREATED);

    User user = userRepository.findByUsername(username)
            .orElseThrow();
    user.setEmailVerified(true);
    userRepository.save(user);

    ResponseEntity<Map> login =
            restTemplate.postForEntity(
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

    private void createGame(
            String id,
            String title
    ) {

        Game game = new Game();

        game.setId(id);
        game.setTitle(title);
        game.setDescription("A test game description.");
        game.setRating(new BigDecimal("8.5"));
        game.setReviewCount(100);
        game.setPrice(new BigDecimal("49.99"));
        game.setReleaseYear(2026);
        game.setDeveloper("Test Studio");
        game.setPublisher("Test Publisher");

        game.setGenres(List.of("Action", "RPG"));
        game.setPlatforms(List.of("PC"));
        game.setTags(List.of("Open World"));
        game.setLanguages(List.of("English"));
        game.setFeatures(List.of("Single Player"));
        game.setStores(List.of());
        game.setRequirements(List.of());

        gameRepository.save(game);
    }

    private ResponseEntity<Map> exchange(
            String path,
            HttpMethod method,
            String token,
            String body
    ) {

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

        StringBuilder body =
                new StringBuilder("{");

        for (int i = 0; i < values.length; i += 2) {

            if (i > 0) {
                body.append(',');
            }

            body.append('"')
                    .append(values[i])
                    .append("\":\"")
                    .append(values[i + 1])
                    .append('"');
        }

        body.append('}');

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_JSON
        );

        return new HttpEntity<>(
                body.toString(),
                headers
        );
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}