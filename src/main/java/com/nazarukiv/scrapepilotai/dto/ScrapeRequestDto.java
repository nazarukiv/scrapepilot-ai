package com.nazarukiv.scrapepilotai.dto;

import jakarta.validation.constraints.NotBlank;

public record ScrapeRequestDto(
        @NotBlank(message = "URL is required")
        String url
) {
}
