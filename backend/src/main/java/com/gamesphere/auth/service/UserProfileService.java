package com.gamesphere.auth.service;

import com.gamesphere.auth.api.ChangePasswordRequest;
import com.gamesphere.auth.api.UpdateProfileRequest;
import com.gamesphere.auth.api.UserProfileResponse;
import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.AuthSessionRepository;
import com.gamesphere.auth.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final AuthSessionRepository authSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    public UserProfileService(
            UserRepository userRepository,
            AuthSessionRepository authSessionRepository,
            PasswordEncoder passwordEncoder,
            EmailVerificationService emailVerificationService) {
        this.userRepository = userRepository;
        this.authSessionRepository = authSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationService = emailVerificationService;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String username) {
        return UserProfileResponse.from(findUser(username));
    }

    @Transactional
    public UserProfileResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = findUser(username);
        boolean emailChanged = !user.getEmail().equalsIgnoreCase(request.email().trim());

        userRepository.findByEmail(request.email().trim().toLowerCase())
                .filter(existing -> !existing.getId().equals(user.getId()))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Email is already in use");
                });

        user.setEmail(request.email().trim());
        user.setDisplayName(request.displayName());

        if (emailChanged) {
            user.setEmailVerified(false);
            userRepository.save(user);
            authSessionRepository.revokeAllForUser(user.getId(), OffsetDateTime.now(ZoneOffset.UTC));
            emailVerificationService.sendVerificationEmail(user);
        }

        return UserProfileResponse.from(userRepository.save(user));
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = findUser(username);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("New password must be different from the current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // A password change invalidates every existing access/refresh session.
        // JwtAuthenticationFilter checks session state on each request, so this
        // also makes previously issued access tokens unusable immediately.
        authSessionRepository.revokeAllForUser(user.getId(), OffsetDateTime.now(ZoneOffset.UTC));
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
