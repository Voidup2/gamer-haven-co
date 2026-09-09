package com.gamesphere.groups.service;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.common.exception.ConflictException;
import com.gamesphere.common.exception.ResourceNotFoundException;
import com.gamesphere.groups.api.GroupMemberResponse;
import com.gamesphere.groups.api.GroupRequest;
import com.gamesphere.groups.api.GroupResponse;
import com.gamesphere.groups.domain.GameGroup;
import com.gamesphere.groups.domain.GroupMember;
import com.gamesphere.groups.repository.GameGroupRepository;
import com.gamesphere.groups.repository.GroupMemberRepository;
import com.gamesphere.notifications.domain.Notification.NotificationType;
import com.gamesphere.notifications.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GroupService {
    private final GameGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public GroupService(GameGroupRepository groupRepository, GroupMemberRepository memberRepository,
                        UserRepository userRepository, NotificationService notificationService) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public GroupResponse create(GroupRequest request) {
        User user = currentUser();
        if (groupRepository.existsByOwnerIdAndName(user.getId(), request.name())) throw new ConflictException("You already have a group with this name");
        GameGroup group = groupRepository.save(new GameGroup(user, request.name(), request.description(), request.imageUrl(), request.publicGroup()));
        memberRepository.save(new GroupMember(group, user));
        return toResponse(group, user);
    }

    @Transactional(readOnly = true)
    public Page<GroupResponse> findPublic(String search, Pageable pageable) {
        User user = currentUserOrNull(); Long userId = user == null ? null : user.getId();
        Page<GameGroup> groups = search == null || search.isBlank()
                ? groupRepository.findByPublicGroupTrueOrderByCreatedAtDesc(pageable)
                : groupRepository.findByPublicGroupTrueAndNameContainingIgnoreCaseOrderByCreatedAtDesc(search.trim(), pageable);
        return groups.map(group -> toResponse(group, userId));
    }

    @Transactional(readOnly = true)
    public Page<GroupResponse> mine(Pageable pageable) {
        User user = currentUser();
        return groupRepository.findByOwnerIdOrderByCreatedAtDesc(user.getId(), pageable).map(group -> toResponse(group, user.getId()));
    }

    @Transactional(readOnly = true)
    public GroupResponse findById(UUID id) {
        GameGroup group = groupRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Group not found: " + id));
        User user = currentUserOrNull();
        if (!group.isPublicGroup() && (user == null || !isMember(group, user.getId()))) throw new AccessDeniedException("You must be a group member to view this group");
        return toResponse(group, user == null ? null : user.getId());
    }

    @Transactional
    public GroupResponse update(UUID id, GroupRequest request) {
        User user = currentUser(); GameGroup group = ownedGroup(id, user.getId());
        if (groupRepository.existsByOwnerIdAndNameAndIdNot(user.getId(), request.name(), id)) throw new ConflictException("You already have a group with this name");
        group.update(request.name(), request.description(), request.imageUrl(), request.publicGroup());
        return toResponse(groupRepository.save(group), user);
    }

    @Transactional
    public void delete(UUID id) { User user = currentUser(); groupRepository.delete(ownedGroup(id, user.getId())); }

    @Transactional
    public GroupResponse join(UUID id) {
        User user = currentUser();
        GameGroup group = groupRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Group not found: " + id));
        if (!group.isPublicGroup()) throw new AccessDeniedException("This group is private");
        if (memberRepository.existsByGroupIdAndUserId(id, user.getId())) throw new ConflictException("You are already a member of this group");
        memberRepository.save(new GroupMember(group, user));
        if (!group.getOwner().getId().equals(user.getId())) {
            notificationService.create(group.getOwner(), NotificationType.SYSTEM, "New group member",
                    user.getDisplayName() != null ? user.getDisplayName() + " joined " + group.getName() : user.getUsername() + " joined " + group.getName(),
                    "GROUP", id.toString());
        }
        return toResponse(group, user);
    }

    @Transactional
    public void leave(UUID id) {
        User user = currentUser(); GameGroup group = groupRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Group not found: " + id));
        if (group.getOwner().getId().equals(user.getId())) throw new ConflictException("The group owner cannot leave the group");
        if (!memberRepository.existsByGroupIdAndUserId(id, user.getId())) throw new ConflictException("You are not a member of this group");
        memberRepository.deleteByGroupIdAndUserId(id, user.getId());
    }

    @Transactional(readOnly = true)
    public List<GroupMemberResponse> members(UUID id) {
        GameGroup group = groupRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Group not found: " + id));
        if (!group.isPublicGroup()) { User user = currentUser(); if (!isMember(group, user.getId())) throw new AccessDeniedException("You must be a group member to view members"); }
        return memberRepository.findByGroupIdOrderByJoinedAtAsc(id).stream().map(GroupMemberResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public boolean isMember(UUID id) { User user = currentUser(); return groupRepository.existsById(id) && memberRepository.existsByGroupIdAndUserId(id, user.getId()); }

    private GameGroup ownedGroup(UUID id, Long userId) { return groupRepository.findByIdAndOwnerId(id, userId).orElseThrow(() -> new AccessDeniedException("Only the group owner can modify this group")); }
    private boolean isMember(GameGroup group, Long userId) { return group.getOwner().getId().equals(userId) || memberRepository.existsByGroupIdAndUserId(group.getId(), userId); }
    private GroupResponse toResponse(GameGroup group, User user) { return toResponse(group, user == null ? null : user.getId()); }
    private GroupResponse toResponse(GameGroup group, Long userId) { return GroupResponse.from(group, memberRepository.countByGroupId(group.getId()), userId != null && isMember(group, userId)); }
    private User currentUser() { Authentication a = SecurityContextHolder.getContext().getAuthentication(); if (a == null || !a.isAuthenticated() || "anonymousUser".equals(a.getPrincipal())) throw new AccessDeniedException("Authentication required"); return userRepository.findByUsername(a.getName()).orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found")); }
    private User currentUserOrNull() { Authentication a = SecurityContextHolder.getContext().getAuthentication(); if (a == null || !a.isAuthenticated() || "anonymousUser".equals(a.getPrincipal())) return null; return userRepository.findByUsername(a.getName()).orElse(null); }
}
