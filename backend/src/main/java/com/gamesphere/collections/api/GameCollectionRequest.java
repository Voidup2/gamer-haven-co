package com.gamesphere.collections.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GameCollectionRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description,
        boolean publicCollection
) {}