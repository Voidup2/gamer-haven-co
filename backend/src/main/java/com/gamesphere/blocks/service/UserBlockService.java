package com.gamesphere.blocks.service;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.blocks.api.UserBlockDtos;
import com.gamesphere.blocks.domain.UserBlock;
import com.gamesphere.blocks.repository.UserBlockRepository;
import com.gamesphere.common.exception.ConflictException;
import com.gamesphere.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserBlockService {
    private final UserBlockRepository blockRepository;
    private final UserRepository userRepository;
    public UserBlockService(UserBlockRepository blockRepository, UserRepository userRepository) { this.blockRepository = blockRepository; this.userRepository = userRepository; }

    @Transactional
    public UserBlockDtos.BlockResponse block(String username, Long blockedId) {
        User blocker = find(username);
        User blocked = userRepository.findById(blockedId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (blocker.getId().equals(blocked.getId())) throw new IllegalArgumentException("You cannot block yourself");
        if (blockRepository.existsByBlockerIdAndBlockedId(blocker.getId(), blockedId)) throw new ConflictException("User is already blocked");
        UserBlock saved = blockRepository.save(new UserBlock(blocker, blocked));
        return toResponse(saved);
    }

    @Transactional
    public void unblock(String username, Long blockedId) {
        User blocker = find(username);
        if (!blockRepository.existsByBlockerIdAndBlockedId(blocker.getId(), blockedId)) throw new ResourceNotFoundException("Block not found");
        blockRepository.deleteByBlockerIdAndBlockedId(blocker.getId(), blockedId);
    }

    @Transactional(readOnly = true)
    public List<UserBlockDtos.BlockResponse> mine(String username) {
        return blockRepository.findByBlockerIdOrderByCreatedAtDesc(find(username).getId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public boolean isBlocked(String username, Long userId) {
        return blockRepository.existsByBlockerIdAndBlockedId(find(username).getId(), userId);
    }

    @Transactional(readOnly = true)
    public boolean isEitherBlocked(Long firstUserId, Long secondUserId) {
        return blockRepository.existsByBlockerIdAndBlockedId(firstUserId, secondUserId)
                || blockRepository.existsByBlockerIdAndBlockedId(secondUserId, firstUserId);
    }

    private User find(String username) { return userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found")); }
    private UserBlockDtos.BlockResponse toResponse(UserBlock block) { User u = block.getBlocked(); return new UserBlockDtos.BlockResponse(u.getId(), u.getUsername(), u.getDisplayName(), block.getCreatedAt()); }
}
