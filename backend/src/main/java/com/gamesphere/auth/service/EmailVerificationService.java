package com.gamesphere.auth.service;

import com.gamesphere.auth.domain.EmailVerificationToken;
import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.EmailVerificationTokenRepository;
import com.gamesphere.auth.repository.UserRepository;
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
public class EmailVerificationService {

    private static final long TOKEN_EXPIRATION_SECONDS = 24L * 60 * 60;

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailSender emailSender;
    private final SecureRandom secureRandom = new SecureRandom();

    public EmailVerificationService(UserRepository userRepository,
                                    EmailVerificationTokenRepository tokenRepository,
                                    EmailSender emailSender) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailSender = emailSender;
    }

    @Transactional
    public void sendVerificationEmail(User user) {
        if (user.isEmailVerified()) {
            return;
        }

        String token = generateToken();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        tokenRepository.save(new EmailVerificationToken(
                UUID.randomUUID(),
                user,
                hash(token),
                now.plusSeconds(TOKEN_EXPIRATION_SECONDS),
                now
        ));

        emailSender.sendVerificationEmail(user.getEmail(), token);
    }

    @Transactional
    public void verify(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Verification token is required");
        }

        EmailVerificationToken verificationToken = tokenRepository.findByTokenHash(hash(token))
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification token"));

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (!verificationToken.isActive(now)) {
            throw new IllegalArgumentException("Invalid or expired verification token");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        verificationToken.markUsed(now);
        userRepository.save(user);
        tokenRepository.save(verificationToken);
    }

    private String generateToken() {
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
