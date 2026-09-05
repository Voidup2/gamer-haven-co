package com.gamesphere.moderation.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContentReportRequest(
        @NotBlank @Size(max = 500) String reason
) {}
