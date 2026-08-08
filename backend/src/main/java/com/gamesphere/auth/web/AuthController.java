package com.gamesphere.auth.web;

import com.gamesphere.auth.api.LoginRequest;
import com.gamesphere.auth.api.LoginResponse;
import com.gamesphere.auth.api.RegisterRequest;
import com.gamesphere.auth.api.RegisterResponse;
import com.gamesphere.auth.service.AuthService;
import com.gamesphere.auth.service.AuthenticationService;
import com.gamesphere.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationService authenticationService;

    public AuthController(AuthService authService, AuthenticationService authenticationService) {
        this.authService = authService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = authenticationService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }
}
