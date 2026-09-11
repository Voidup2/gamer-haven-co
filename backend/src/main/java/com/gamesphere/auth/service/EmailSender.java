package com.gamesphere.auth.service;

public interface EmailSender {
    void sendVerificationEmail(String email, String token);
}
