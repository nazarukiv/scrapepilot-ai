package com.nazarukiv.scrapepilotai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record UpdateScrapingTaskRequestDto(
        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must be 255 characters or fewer")
        String name,

        @NotBlank(message = "URL is required")
        @Size(max = 2048, message = "URL must be 2048 characters or fewer")
        @URL(message = "URL must be valid")
        @Pattern(regexp = "https?://.+", message = "URL must start with http:// or https://")
        String url,

        @NotNull(message = "Active status is required")
        Boolean active,

        @Positive(message = "Execution interval must be greater than 0 seconds")
        Integer executionIntervalSeconds
) {
}
