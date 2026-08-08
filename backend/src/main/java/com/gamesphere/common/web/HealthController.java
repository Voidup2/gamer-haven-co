package com.gamesphere.common.web;

import com.gamesphere.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.success("GameSphere backend is running", Map.of(
                "status", "UP",
                "service", "gamesphere-backend"
        ));
    }
}
