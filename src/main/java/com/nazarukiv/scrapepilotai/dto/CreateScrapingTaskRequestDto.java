package com.nazarukiv.scrapepilotai.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateScrapingTaskRequestDto(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "URL is required")
        String url
) {
}
