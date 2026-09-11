package com.gamesphere.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    @Override
    public void sendVerificationEmail(String email, String token) {
        log.info("Email verification requested for {}. Verification token: {}", email, token);
    }
}
