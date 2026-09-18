package com.gamesphere.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LoggingEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    @Value("${app.frontend-url:http://localhost:8081}")
    private String frontendUrl;

    @Override
    public void sendVerificationEmail(String email, String token) {
        String verificationUrl = frontendUrl.replaceAll("/$", "") + "/verify-email?token=" + token;
        log.info("Email verification requested for {}. Verification URL: {}", email, verificationUrl);
    }
}
