package com.gamesphere.auth.web;

import com.gamesphere.auth.api.LoginRequest;
import com.gamesphere.auth.api.LoginResponse;
import com.gamesphere.auth.api.LogoutRequest;
import com.gamesphere.auth.api.RefreshTokenRequest;
import com.gamesphere.auth.api.RegisterRequest;
import com.gamesphere.auth.api.RegisterResponse;
import com.gamesphere.auth.api.VerifyEmailRequest;
import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.auth.service.AuthService;
import com.gamesphere.auth.service.AuthenticationService;
import com.gamesphere.auth.service.EmailVerificationService;
import com.gamesphere.common.api.ApiResponse;
import com.gamesphere.common.web.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationService authenticationService;
    private final EmailVerificationService emailVerificationService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, AuthenticationService authenticationService,
                          EmailVerificationService emailVerificationService,
                          UserRepository userRepository) {
        this.authService = authService;
        this.authenticationService = authenticationService;
        this.emailVerificationService = emailVerificationService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = authenticationService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authenticationService.refresh(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest request) {
        authenticationService.logout(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {
        emailVerificationService.verify(request.token());
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully", null));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAll(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
        authenticationService.logoutAll(user);
        return ResponseEntity.ok(ApiResponse.success("All sessions logged out successfully", null));
    }
}
