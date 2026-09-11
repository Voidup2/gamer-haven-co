package com.gamesphere.auth.service;

import com.gamesphere.auth.api.LoginRequest;
import com.gamesphere.auth.api.LoginResponse;
import com.gamesphere.auth.domain.AuthSession;
import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.AuthSessionRepository;
import com.gamesphere.auth.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.UUID;

@Service
public class AuthenticationService {

    private static final long STANDARD_REFRESH_SECONDS = 7L * 24 * 60 * 60;
    private static final long REMEMBER_ME_REFRESH_SECONDS = 30L * 24 * 60 * 60;

    private final UserRepository userRepository;
    private final AuthSessionRepository authSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthenticationService(UserRepository userRepository,
                                 AuthSessionRepository authSessionRepository,
                                 PasswordEncoder passwordEncoder,
                                 JwtService jwtService) {
        this.userRepository = userRepository;
        this.authSessionRepository = authSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.usernameOrEmail().trim())
                .or(() -> userRepository.findByEmail(request.usernameOrEmail().trim().toLowerCase()))
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!user.isEnabled() || !user.isEmailVerified()
                || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        return createSession(user, request.rememberMe());
    }

    @Transactional
    public LoginResponse refresh(String refreshToken) {
        AuthSession session = authSessionRepository.findByRefreshTokenHash(hash(refreshToken))
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (!session.isActive(now) || !session.getUser().isEnabled()) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        String newRefreshToken = generateRefreshToken();
        long refreshExpiresIn = session.isRememberMe() ? REMEMBER_ME_REFRESH_SECONDS : STANDARD_REFRESH_SECONDS;
        session.rotate(hash(newRefreshToken), now.plusSeconds(refreshExpiresIn), now);
        authSessionRepository.save(session);

        String accessToken = jwtService.generateAccessToken(session.getUser(), session.getId());
        return new LoginResponse(accessToken, newRefreshToken, "Bearer", jwtService.getExpirationSeconds(),
                refreshExpiresIn, session.getUser().getId(), session.getUser().getUsername());
    }

    @Transactional
    public void logout(String refreshToken) {
        authSessionRepository.findByRefreshTokenHash(hash(refreshToken)).ifPresent(session ->
                session.revoke(OffsetDateTime.now(ZoneOffset.UTC)));
    }

    @Transactional
    public void logoutAll(User user) {
        authSessionRepository.revokeAllForUser(user.getId(), OffsetDateTime.now(ZoneOffset.UTC));
    }

    private LoginResponse createSession(User user, boolean rememberMe) {
        String refreshToken = generateRefreshToken();
        UUID sessionId = UUID.randomUUID();
        long refreshExpiresIn = rememberMe ? REMEMBER_ME_REFRESH_SECONDS : STANDARD_REFRESH_SECONDS;
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        authSessionRepository.save(new AuthSession(sessionId, user, hash(refreshToken), rememberMe,
                now.plusSeconds(refreshExpiresIn), now));

        String accessToken = jwtService.generateAccessToken(user, sessionId);
        return new LoginResponse(accessToken, refreshToken, "Bearer", jwtService.getExpirationSeconds(),
                refreshExpiresIn, user.getId(), user.getUsername());
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(64);
            for (byte value : digest) {
                result.append(String.format("%02x", value));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
