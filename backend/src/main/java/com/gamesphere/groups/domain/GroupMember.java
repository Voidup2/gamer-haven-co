package com.gamesphere.groups.domain;

import com.gamesphere.auth.domain.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "group_members", uniqueConstraints = @UniqueConstraint(name = "uq_group_members_group_user", columnNames = {"group_id", "user_id"}))
public class GroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private GameGroup group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "joined_at", nullable = false)
    private OffsetDateTime joinedAt;

    protected GroupMember() {}

    public GroupMember(GameGroup group, User user) {
        this.group = group;
        this.user = user;
        this.joinedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public GameGroup getGroup() { return group; }
    public User getUser() { return user; }
    public OffsetDateTime getJoinedAt() { return joinedAt; }
}
