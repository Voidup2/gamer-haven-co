package com.gamesphere.groups.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GroupRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 1000) String description,
        @Size(max = 2000) String imageUrl,
        boolean publicGroup
) {}
