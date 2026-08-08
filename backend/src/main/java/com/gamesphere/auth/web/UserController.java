package com.gamesphere.auth.web;

import com.gamesphere.common.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> me(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                "Authenticated user",
                Map.of(
                        "username", authentication.getName(),
                        "authenticated", authentication.isAuthenticated()
                )
        ));
    }
}
