package com.gamesphere.groups.api;

import com.gamesphere.activity.repository.UserActivityRepository;
import com.gamesphere.auth.domain.Role;
import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.RoleRepository;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.groups.domain.GameGroup;
import com.gamesphere.groups.domain.GroupMember;
import com.gamesphere.groups.repository.GameGroupRepository;
import com.gamesphere.groups.repository.GroupMemberRepository;
import com.gamesphere.notifications.repository.NotificationRepository;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class GroupControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private GameGroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository memberRepository;

    @Autowired
    private UserActivityRepository userActivityRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @AfterEach
    void cleanUp() {
        notificationRepository.deleteAll();
        memberRepository.deleteAll();
        groupRepository.deleteAll();

        userRepository.findAll()
                .forEach(user -> userActivityRepository.deleteByUserId(user.getId()));

        userRepository.deleteAll();
    }

    @Test
    void createGroupWithoutJwtShouldReturnUnauthorized() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/v1/groups"),
                json(
                        "name", "Unauthenticated Group",
                        "description", "Test group",
                        "imageUrl", null,
                        "publicGroup", true
                ),
                Map.class
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void authenticatedUserShouldCreateGroup() {
        String token = login("groupcreator", "groupcreator@example.com");

        ResponseEntity<Map> response = exchange(
                "/api/v1/groups",
                HttpMethod.POST,
                token,
                "{\"name\":\"My Group\",\"description\":\"My gaming group\",\"imageUrl\":null,\"publicGroup\":true}"
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        assertThat(groupRepository.findAll())
                .hasSize(1);

        assertThat(memberRepository.findAll())
                .hasSize(1);
    }

    @Test
    void ownerShouldUpdateOwnGroup() {
        String token = login("groupowner", "groupowner@example.com");

        User owner = userRepository.findByUsername("groupowner")
                .orElseThrow();

        GameGroup group = groupRepository.save(
                new GameGroup(
                        owner,
                        "Original Group",
                        "Original description",
                        null,
                        true
                )
        );

        memberRepository.save(new GroupMember(group, owner));

        ResponseEntity<Map> response = exchange(
                "/api/v1/groups/" + group.getId(),
                HttpMethod.PUT,
                token,
                "{\"name\":\"Updated Group\",\"description\":\"Updated description\",\"imageUrl\":null,\"publicGroup\":false}"
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        GameGroup updated = groupRepository.findById(group.getId())
                .orElseThrow();

        assertThat(updated.getName())
                .isEqualTo("Updated Group");

        assertThat(updated.getDescription())
                .isEqualTo("Updated description");

        assertThat(updated.isPublicGroup())
                .isFalse();
    }

    @Test
    void userShouldNotUpdateAnotherUsersGroup() {
        login("groupownerupdate", "groupownerupdate@example.com");
        String otherToken = login("groupotherupdate", "groupotherupdate@example.com");

        User owner = userRepository.findByUsername("groupownerupdate")
                .orElseThrow();

        GameGroup group = groupRepository.save(
                new GameGroup(
                        owner,
                        "Protected Group",
                        "Original description",
                        null,
                        true
                )
        );

        ResponseEntity<Map> response = exchange(
                "/api/v1/groups/" + group.getId(),
                HttpMethod.PUT,
                otherToken,
                "{\"name\":\"Hacked Group\",\"description\":\"Changed\",\"imageUrl\":null,\"publicGroup\":false}"
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        assertThat(groupRepository.findById(group.getId())
                .orElseThrow()
                .getName())
                .isEqualTo("Protected Group");
    }

    @Test
    void ownerShouldDeleteOwnGroup() {
        String token = login("groupdeleter", "groupdeleter@example.com");

        User owner = userRepository.findByUsername("groupdeleter")
                .orElseThrow();

        GameGroup group = groupRepository.save(
                new GameGroup(
                        owner,
                        "Delete Me",
                        "Group to delete",
                        null,
                        true
                )
        );

        memberRepository.save(new GroupMember(group, owner));

        ResponseEntity<Void> response = exchange(
                "/api/v1/groups/" + group.getId(),
                HttpMethod.DELETE,
                token,
                null,
                Void.class
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(groupRepository.findById(group.getId()))
                .isEmpty();
    }

    @Test
    void userShouldNotDeleteAnotherUsersGroup() {
        login("groupownerdel", "groupownerdel@example.com");
        String otherToken = login("groupotherdel", "groupotherdel@example.com");

        User owner = userRepository.findByUsername("groupownerdel")
                .orElseThrow();

        GameGroup group = groupRepository.save(
                new GameGroup(
                        owner,
                        "Protected Delete Group",
                        "Protected group",
                        null,
                        true
                )
        );

        ResponseEntity<Map> response = exchange(
                "/api/v1/groups/" + group.getId(),
                HttpMethod.DELETE,
                otherToken,
                null
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        assertThat(groupRepository.findById(group.getId()))
                .isPresent();
    }

    @Test
    void userShouldJoinPublicGroup() {
        login("groupownerjoin", "groupownerjoin@example.com");
        String memberToken = login("groupmemberjoin", "groupmemberjoin@example.com");

        User owner = userRepository.findByUsername("groupownerjoin")
                .orElseThrow();

        GameGroup group = groupRepository.save(
                new GameGroup(
                        owner,
                        "Public Group",
                        "Anyone can join",
                        null,
                        true
                )
        );

        ResponseEntity<Map> response = exchange(
                "/api/v1/groups/" + group.getId() + "/join",
                HttpMethod.POST,
                memberToken,
                null
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        User member = userRepository.findByUsername("groupmemberjoin")
                .orElseThrow();

        assertThat(memberRepository.existsByGroupIdAndUserId(
                group.getId(),
                member.getId()
        )).isTrue();
    }

    @Test
    void userShouldNotJoinPrivateGroup() {
        login("privategroupowner", "privateowner@example.com");
        String memberToken = login("privategroupuser", "privateuser@example.com");

        User owner = userRepository.findByUsername("privategroupowner")
                .orElseThrow();

        GameGroup group = groupRepository.save(
                new GameGroup(
                        owner,
                        "Private Group",
                        "Members only",
                        null,
                        false
                )
        );

        ResponseEntity<Map> response = exchange(
                "/api/v1/groups/" + group.getId() + "/join",
                HttpMethod.POST,
                memberToken,
                null
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        User member = userRepository.findByUsername("privategroupuser")
                .orElseThrow();

        assertThat(memberRepository.existsByGroupIdAndUserId(
                group.getId(),
                member.getId()
        )).isFalse();
    }

    @Test
    void nonMemberShouldNotViewPrivateGroup() {
        login("privateviewowner", "privateviewowner@example.com");
        String otherToken = login("privateviewother", "privateviewother@example.com");

        User owner = userRepository.findByUsername("privateviewowner")
                .orElseThrow();

        GameGroup group = groupRepository.save(
                new GameGroup(
                        owner,
                        "Private View Group",
                        "Private group",
                        null,
                        false
                )
        );

        ResponseEntity<Map> response = exchange(
                "/api/v1/groups/" + group.getId(),
                HttpMethod.GET,
                otherToken,
                null
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void nonMemberShouldNotViewPrivateGroupMembers() {
        login("privatemembersowner", "privatemembersowner@example.com");
        String otherToken = login("privatemembersother", "privatemembersother@example.com");

        User owner = userRepository.findByUsername("privatemembersowner")
                .orElseThrow();

        GameGroup group = groupRepository.save(
                new GameGroup(
                        owner,
                        "Private Members Group",
                        "Private group",
                        null,
                        false
                )
        );

        memberRepository.save(new GroupMember(group, owner));

        ResponseEntity<Map> response = exchange(
                "/api/v1/groups/" + group.getId() + "/members",
                HttpMethod.GET,
                otherToken,
                null
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void groupMemberShouldViewPrivateGroupMembers() {
        login("privateallowedowner", "privateallowedowner@example.com");
        String memberToken = login(
                "privateallowedmember",
                "privateallowedmember@example.com"
        );

        User owner = userRepository.findByUsername("privateallowedowner")
                .orElseThrow();

        User member = userRepository.findByUsername("privateallowedmember")
                .orElseThrow();

        GameGroup group = groupRepository.save(
                new GameGroup(
                        owner,
                        "Private Allowed Group",
                        "Private group",
                        null,
                        false
                )
        );

        memberRepository.save(new GroupMember(group, owner));
        memberRepository.save(new GroupMember(group, member));

        ResponseEntity<List> response = exchange(
                "/api/v1/groups/" + group.getId() + "/members",
                HttpMethod.GET,
                memberToken,
                null,
                List.class
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .isNotNull()
                .hasSize(2);
    }

    @Test
    void ownerShouldNotLeaveOwnGroup() {
        String token = login(
                "groupleaveowner",
                "groupleaveowner@example.com"
        );

        User owner = userRepository.findByUsername("groupleaveowner")
                .orElseThrow();

        GameGroup group = groupRepository.save(
                new GameGroup(
                        owner,
                        "Owner Leave Group",
                        "Owner cannot leave",
                        null,
                        true
                )
        );

        memberRepository.save(new GroupMember(group, owner));

        ResponseEntity<Map> response = exchange(
                "/api/v1/groups/" + group.getId() + "/leave",
                HttpMethod.DELETE,
                token,
                null
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(memberRepository.existsByGroupIdAndUserId(
                group.getId(),
                owner.getId()
        )).isTrue();
    }

    @Test
    void membershipWithoutJwtShouldReturnForbidden() {
        UUID id = UUID.randomUUID();

        ResponseEntity<Map> response = exchange(
                "/api/v1/groups/" + id + "/membership",
                HttpMethod.GET,
                null,
                null
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
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
                        "displayName", "Group Tester"
                ),
                Map.class
        );

        assertThat(register.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        User user = userRepository.findByUsername(username)
                .orElseThrow();

        user.setEmailVerified(true);
        userRepository.saveAndFlush(user);

        return loginTokenForExistingUser(username);
    }

    private String loginTokenForExistingUser(String username) {
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

    private <T> ResponseEntity<T> exchange(
            String path,
            HttpMethod method,
            String token,
            String body,
            Class<T> responseType
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (token != null) {
            headers.setBearerAuth(token);
        }

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(
                url(path),
                method,
                entity,
                responseType
        );
    }

    private ResponseEntity<Map> exchange(
            String path,
            HttpMethod method,
            String token,
            String body
    ) {
        return exchange(path, method, token, body, Map.class);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private Map<String, Object> json(Object... values) {
        Map<String, Object> body = new java.util.HashMap<>();

        for (int i = 0; i < values.length; i += 2) {
            body.put((String) values[i], values[i + 1]);
        }

        return body;
    }
}