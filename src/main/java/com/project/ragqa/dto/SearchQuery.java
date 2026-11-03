package com.project.ragqa.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SearchQuery(
        @NotBlank @Size(max = 500) String query,
        UUID documentId,
        @Min(1) @Max(20) Integer topK
) {}