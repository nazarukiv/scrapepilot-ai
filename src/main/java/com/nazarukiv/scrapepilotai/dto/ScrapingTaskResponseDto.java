package com.nazarukiv.scrapepilotai.dto;

import java.time.Instant;

public record ScrapingTaskResponseDto(
        Long id,
        String name,
        String url,
        boolean active,
        Integer executionIntervalSeconds,
        Instant lastExecutedAt,
        Instant createdAt
) {
}
