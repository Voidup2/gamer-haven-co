package com.gamesphere.forum;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.activity.repository.UserActivityRepository;
import com.gamesphere.notifications.repository.NotificationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import com.gamesphere.auth.service.EmailSender;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.gamesphere.auth.domain.Role;
import com.gamesphere.auth.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ForumControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ForumTopicRepository topicRepository;

    @Autowired
    private ForumPostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @MockBean
    private EmailSender emailSender;

    @Autowired
    private UserActivityRepository userActivityRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
        void setUp() {
        reset(emailSender);

        if (roleRepository.findByName("USER").isEmpty()) {
                roleRepository.save(new Role("USER"));
        }
        }

    @AfterEach
        void cleanUp() {
        reset(emailSender);

        notificationRepository.deleteAll();
        postRepository.deleteAll();
        topicRepository.deleteAll();

        userRepository.findAll()
            .forEach(user -> userActivityRepository.deleteByUserId(user.getId()));

        userRepository.deleteAll();
        }

    @Test
    void unauthenticatedShouldNotCreateTopic() {
        ResponseEntity<Map> response = exchange(
                "/api/v1/forums/topics",
                HttpMethod.POST,
                null,
                json(
                        "title", "Unauthorized Topic",
                        "category", "general",
                        "content", "This should not be created."
                )
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void authenticatedUserShouldCreateTopic() {
        String token = login(
                "forumcreator",
                "forumcreator@example.com"
        );

        ResponseEntity<Map> response = exchange(
                "/api/v1/forums/topics",
                HttpMethod.POST,
                token,
                json(
                        "title", "My Forum Topic",
                        "category", "general",
                        "content", "Hello forum."
                )
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        assertThat(topicRepository.count())
                .isEqualTo(1);
    }

    @Test
    void anyoneShouldViewPublicTopic() {
        User user = createUser(
                "topicviewer",
                "topicviewer@example.com"
        );

        ForumTopic topic = topicRepository.save(
                new ForumTopic(
                        "Public Topic",
                        "general",
                        "Public content",
                        user,
                        null
                )
        );

        ResponseEntity<Map> response = exchange(
                "/api/v1/forums/topics/" + topic.getId(),
                HttpMethod.GET,
                null,
                null
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void topicOwnerShouldUpdateTopic() {
        String token = login(
                "topicowner",
                "topicowner@example.com"
        );

        User owner = userRepository.findByUsername("topicowner")
                .orElseThrow();

        ForumTopic topic = topicRepository.save(
                new ForumTopic(
                        "Original Topic",
                        "general",
                        "Original content",
                        owner,
                        null
                )
        );

        ResponseEntity<Map> response = exchange(
                "/api/v1/forums/topics/" + topic.getId(),
                HttpMethod.PUT,
                token,
                json(
                        "title", "Updated Topic",
                        "category", "general",
                        "content", "Updated content"
                )
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void otherUserShouldNotUpdateTopic() {
        login(
                "topicowner2",
                "topicowner2@example.com"
        );

        String otherToken = login(
                "topicother",
                "topicother@example.com"
        );

        User owner = userRepository.findByUsername("topicowner2")
                .orElseThrow();

        ForumTopic topic = topicRepository.save(
                new ForumTopic(
                        "Protected Topic",
                        "general",
                        "Protected content",
                        owner,
                        null
                )
        );

        ResponseEntity<Map> response = exchange(
                "/api/v1/forums/topics/" + topic.getId(),
                HttpMethod.PUT,
                otherToken,
                json(
                        "title", "Hacked Topic",
                        "category", "general",
                        "content", "Should not update"
                )
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void topicOwnerShouldDeleteTopic() {
        String token = login(
                "topicdeleteowner",
                "topicdeleteowner@example.com"
        );

        User owner = userRepository.findByUsername("topicdeleteowner")
                .orElseThrow();

        ForumTopic topic = topicRepository.save(
                new ForumTopic(
                        "Delete Me",
                        "general",
                        "Delete content",
                        owner,
                        null
                )
        );

        ResponseEntity<Void> response = exchange(
                "/api/v1/forums/topics/" + topic.getId(),
                HttpMethod.DELETE,
                token,
                null,
                Void.class
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(topicRepository.existsById(topic.getId()))
                .isFalse();
    }

    @Test
    void otherUserShouldNotDeleteTopic() {
        login(
                "deleteowner",
                "deleteowner@example.com"
        );

        String otherToken = login(
                "deleteother",
                "deleteother@example.com"
        );

        User owner = userRepository.findByUsername("deleteowner")
                .orElseThrow();

        ForumTopic topic = topicRepository.save(
                new ForumTopic(
                        "Protected Delete",
                        "general",
                        "Protected",
                        owner,
                        null
                )
        );

        ResponseEntity<Void> response = exchange(
                "/api/v1/forums/topics/" + topic.getId(),
                HttpMethod.DELETE,
                otherToken,
                null,
                Void.class
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void nonAdminShouldNotLockTopic() {
        String token = login(
                "lockuser",
                "lockuser@example.com"
        );

        User user = userRepository.findByUsername("lockuser")
                .orElseThrow();

        ForumTopic topic = topicRepository.save(
                new ForumTopic(
                        "Lock Protected",
                        "general",
                        "Content",
                        user,
                        null
                )
        );

        ResponseEntity<Map> response = exchange(
                "/api/v1/forums/topics/" + topic.getId() + "/lock?locked=true",
                HttpMethod.PUT,
                token,
                null
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void nonAdminShouldNotPinTopic() {
        String token = login(
                "pinuser",
                "pinuser@example.com"
        );

        User user = userRepository.findByUsername("pinuser")
                .orElseThrow();

        ForumTopic topic = topicRepository.save(
                new ForumTopic(
                        "Pin Protected",
                        "general",
                        "Content",
                        user,
                        null
                )
        );

        ResponseEntity<Map> response = exchange(
                "/api/v1/forums/topics/" + topic.getId() + "/pin?pinned=true",
                HttpMethod.PUT,
                token,
                null
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void anyoneShouldViewPosts() {
        User user = createUser(
                "postviewer",
                "postviewer@example.com"
        );

        ForumTopic topic = topicRepository.save(
                new ForumTopic(
                        "Post Topic",
                        "general",
                        "Topic content",
                        user,
                        null
                )
        );

        postRepository.save(
                new ForumPost(
                        topic,
                        user,
                        "A forum post."
                )
        );

        ResponseEntity<Map> response = exchange(
                "/api/v1/forums/topics/" + topic.getId() + "/posts",
                HttpMethod.GET,
                null,
                null
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void authenticatedUserShouldCreatePost() {
        String token = login(
                "postcreator",
                "postcreator@example.com"
        );

        User author = userRepository.findByUsername("postcreator")
                .orElseThrow();

        ForumTopic topic = topicRepository.save(
                new ForumTopic(
                        "Create Post Topic",
                        "general",
                        "Topic content",
                        author,
                        null
                )
        );

        ResponseEntity<Map> response = exchange(
                "/api/v1/forums/topics/" + topic.getId() + "/posts",
                HttpMethod.POST,
                token,
                json(
                        "content", "My forum reply."
                )
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        assertThat(postRepository.count())
                .isEqualTo(1);
    }

    @Test
    void lockedTopicShouldRejectNewPost() {
        String token = login(
                "lockedposter",
                "lockedposter@example.com"
        );

        User author = userRepository.findByUsername("lockedposter")
                .orElseThrow();

        ForumTopic topic = new ForumTopic(
                "Locked Topic",
                "general",
                "Locked content",
                author,
                null
        );
        topic.setLocked(true);

        topic = topicRepository.save(topic);

        ResponseEntity<Map> response = exchange(
                "/api/v1/forums/topics/" + topic.getId() + "/posts",
                HttpMethod.POST,
                token,
                json(
                        "content", "This should fail."
                )
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void postAuthorShouldUpdateOwnPost() {
        String token = login(
                "postowner",
                "postowner@example.com"
        );

        User owner = userRepository.findByUsername("postowner")
                .orElseThrow();

        ForumTopic topic = topicRepository.save(
                new ForumTopic(
                        "Post Update Topic",
                        "general",
                        "Topic content",
                        owner,
                        null
                )
        );

        ForumPost post = postRepository.save(
                new ForumPost(
                        topic,
                        owner,
                        "Original post"
                )
        );

        ResponseEntity<Map> response = exchange(
                "/api/v1/forums/posts/" + post.getId(),
                HttpMethod.PUT,
                token,
                json(
                        "content", "Updated post"
                )
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void otherUserShouldNotUpdatePost() {
        login(
                "postowner2",
                "postowner2@example.com"
        );

        String otherToken = login(
                "postother",
                "postother@example.com"
        );

        User owner = userRepository.findByUsername("postowner2")
                .orElseThrow();

        ForumTopic topic = topicRepository.save(
                new ForumTopic(
                        "Protected Post Topic",
                        "general",
                        "Topic content",
                        owner,
                        null
                )
        );

        ForumPost post = postRepository.save(
                new ForumPost(
                        topic,
                        owner,
                        "Protected post"
                )
        );

        ResponseEntity<Map> response = exchange(
                "/api/v1/forums/posts/" + post.getId(),
                HttpMethod.PUT,
                otherToken,
                json(
                        "content", "Unauthorized update"
                )
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void postAuthorShouldDeleteOwnPost() {
        String token = login(
                "postdeleteowner",
                "postdeleteowner@example.com"
        );

        User owner = userRepository.findByUsername("postdeleteowner")
                .orElseThrow();

        ForumTopic topic = topicRepository.save(
                new ForumTopic(
                        "Post Delete Topic",
                        "general",
                        "Topic content",
                        owner,
                        null
                )
        );

        ForumPost post = postRepository.save(
                new ForumPost(
                        topic,
                        owner,
                        "Delete this post"
                )
        );

        ResponseEntity<Void> response = exchange(
                "/api/v1/forums/posts/" + post.getId(),
                HttpMethod.DELETE,
                token,
                null,
                Void.class
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(postRepository.existsById(post.getId()))
                .isFalse();
    }

    @Test
    void otherUserShouldNotDeletePost() {
        login(
                "postdeleteowner2",
                "postdeleteowner2@example.com"
        );

        String otherToken = login(
                "postdeleteother",
                "postdeleteother@example.com"
        );

        User owner = userRepository.findByUsername("postdeleteowner2")
                .orElseThrow();

        ForumTopic topic = topicRepository.save(
                new ForumTopic(
                        "Protected Post Delete",
                        "general",
                        "Topic content",
                        owner,
                        null
                )
        );

        ForumPost post = postRepository.save(
                new ForumPost(
                        topic,
                        owner,
                        "Protected post"
                )
        );

        ResponseEntity<Void> response = exchange(
                "/api/v1/forums/posts/" + post.getId(),
                HttpMethod.DELETE,
                otherToken,
                null,
                Void.class
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    private User createUser(String username, String email) {
        String token = login(username, email);

        return userRepository.findByUsername(username)
                .orElseThrow();
    }

    private String login(String username, String email) {
        createUserIfNeeded(username, email);

        ResponseEntity<Map> login = restTemplate.postForEntity(
                "/api/v1/auth/login",
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

    private void createUserIfNeeded(String username, String email) {
    reset(emailSender);

    ResponseEntity<Map> response = restTemplate.postForEntity(
            "/api/v1/auth/register",
            json(
                    "username", username,
                    "email", email,
                    "password", "Test@12345",
                    "displayName", "Test User"
            ),
            Map.class
    );

    if (response.getStatusCode() == HttpStatus.CREATED) {
        ArgumentCaptor<String> tokenCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(emailSender).sendVerificationEmail(
                org.mockito.ArgumentMatchers.anyString(),
                tokenCaptor.capture()
        );

        ResponseEntity<Map> verificationResponse = restTemplate.postForEntity(
                "/api/v1/auth/verify-email",
                json("token", tokenCaptor.getValue()),
                Map.class
        );

        assertThat(verificationResponse.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        return;
    }

        if (response.getStatusCode() != HttpStatus.CONFLICT) {
        System.out.println("REGISTER STATUS: " + response.getStatusCode());
        System.out.println("REGISTER HEADERS: " + response.getHeaders());
        System.out.println("REGISTER BODY: " + response.getBody());

        throw new AssertionError(
                    "Registration failed: " + response.getStatusCode()
                            + " body=" + response.getBody()
                );
        }
}    

    private ResponseEntity<Map> exchange(
            String url,
            HttpMethod method,
            String token,
            Map<String, Object> body) {

        return exchange(url, method, token, body, Map.class);
    }

    private <T> ResponseEntity<T> exchange(
            String url,
            HttpMethod method,
            String token,
            Object body,
            Class<T> responseType) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (token != null) {
            headers.setBearerAuth(token);
        }

        HttpEntity<Object> entity =
                new HttpEntity<>(body, headers);

        return restTemplate.exchange(
                url,
                method,
                entity,
                responseType
        );
    }

    private Map<String, Object> json(Object... values) {
        Map<String, Object> result = new java.util.HashMap<>();

        for (int i = 0; i < values.length; i += 2) {
            result.put(
                    String.valueOf(values[i]),
                    values[i + 1]
            );
        }

        return result;
    }
}