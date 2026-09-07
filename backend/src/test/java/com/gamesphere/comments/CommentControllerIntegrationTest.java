package com.gamesphere.comments;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamesphere.auth.domain.Role;
import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.RoleRepository;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.games.domain.Game;
import com.gamesphere.games.repository.GameRepository;
import com.gamesphere.discussions.domain.Discussion;
import com.gamesphere.discussions.repository.DiscussionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CommentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private DiscussionRepository discussionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user;
    private User secondUser;
    private Game game;
    private Discussion discussion;

    @BeforeEach
    void setUp() {
        user = createUser(
                "commentuser",
                "commentuser@example.com"
        );

        secondUser = createUser(
                "commentuser2",
                "commentuser2@example.com"
        );

        game = createGame();

        discussion = discussionRepository.save(
                new Discussion(
                        game,
                        user,
                        "Test Discussion",
                        "This is a test discussion."
                )
        );
    }

    @Test
    void createCommentShouldWorkForAuthenticatedUser() throws Exception {

        String token = loginAs("commentuser");

        String body = """
                {
                    "content": "This is my first comment."
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/v1/discussions/{discussionId}/comments",
                                discussion.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content")
                        .value("This is my first comment."))
                .andExpect(jsonPath("$.data.username")
                        .value("commentuser"))
                .andExpect(jsonPath("$.data.discussionId")
                        .value(discussion.getId().toString()));
    }

    @Test
    void unauthenticatedUserShouldNotCreateComment() throws Exception {

        String body = """
                {
                    "content": "This should fail."
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/v1/discussions/{discussionId}/comments",
                                discussion.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCommentsShouldBePublic() throws Exception {

        String token = loginAs("commentuser");

        createComment(token, "First comment");

        mockMvc.perform(
                        get(
                                "/api/v1/discussions/{discussionId}/comments",
                                discussion.getId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].content")
                        .value("First comment"));
    }

    @Test
    void getCommentShouldBePublic() throws Exception {

        String token = loginAs("commentuser");

        UUID commentId = createComment(
                token,
                "Individual comment"
        );

        mockMvc.perform(
                        get("/api/v1/comments/{id}", commentId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id")
                        .value(commentId.toString()))
                .andExpect(jsonPath("$.data.content")
                        .value("Individual comment"));
    }

    @Test
    void ownerShouldBeAbleToUpdateComment() throws Exception {

        String token = loginAs("commentuser");

        UUID commentId = createComment(
                token,
                "Original content"
        );

        String body = """
                {
                    "content": "Updated content"
                }
                """;

        mockMvc.perform(
                        put("/api/v1/comments/{id}", commentId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content")
                        .value("Updated content"));
    }

    @Test
    void nonOwnerShouldNotBeAbleToUpdateComment() throws Exception {

        String ownerToken = loginAs("commentuser");

        UUID commentId = createComment(
                ownerToken,
                "Original content"
        );

        String secondUserToken = loginAs("commentuser2");

        String body = """
                {
                    "content": "Unauthorized update"
                }
                """;

        mockMvc.perform(
                        put("/api/v1/comments/{id}", commentId)
                                .header(
                                        "Authorization",
                                        "Bearer " + secondUserToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void ownerShouldBeAbleToDeleteComment() throws Exception {

        String token = loginAs("commentuser");

        UUID commentId = createComment(
                token,
                "Delete me"
        );

        mockMvc.perform(
                        delete("/api/v1/comments/{id}", commentId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(
                        get("/api/v1/comments/{id}", commentId)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void nonexistentDiscussionShouldReturn404() throws Exception {

        String token = loginAs("commentuser");

        UUID nonexistentId = UUID.randomUUID();

        String body = """
                {
                    "content": "This should fail."
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/v1/discussions/{discussionId}/comments",
                                nonexistentId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void nonexistentCommentShouldReturn404() throws Exception {

        mockMvc.perform(
                        get(
                                "/api/v1/comments/{id}",
                                UUID.randomUUID()
                        )
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void blankCommentShouldReturn400() throws Exception {

        String token = loginAs("commentuser");

        String body = """
                {
                    "content": ""
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/v1/discussions/{discussionId}/comments",
                                discussion.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest());
    }

    private UUID createComment(
            String token,
            String content
    ) throws Exception {

        String body = objectMapper.writeValueAsString(
                java.util.Map.of("content", content)
        );

        String response = mockMvc.perform(
                        post(
                                "/api/v1/discussions/{discussionId}/comments",
                                discussion.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        return UUID.fromString(
                json.get("data")
                        .get("id")
                        .asText()
        );
    }

    private User createUser(
            String username,
            String email
    ) {

        User createdUser = new User(
                username,
                email,
                passwordEncoder.encode("Password123!"),
                username
        );

        Role userRole = roleRepository
                .findByName("USER")
                .orElseGet(() ->
                        roleRepository.save(
                                new Role("USER")
                        )
                );

        createdUser.getRoles().add(userRole);

        return userRepository.save(createdUser);
    }

    private Game createGame() {

        Game game = new Game();

        game.setId("test-comment-game");
        game.setTitle("Test Comment Game");
        game.setDescription(
                "Game used for comment integration tests."
        );

        return gameRepository.save(game);
    }

    private String loginAs(String username) throws Exception {

        String body = objectMapper.writeValueAsString(
                java.util.Map.of(
                        "usernameOrEmail",
                        username,
                        "password",
                        "Password123!"
                )
        );

        String response = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        return json.get("data")
                .get("accessToken")
                .asText();
    }
}