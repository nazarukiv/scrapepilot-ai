package com.nazarukiv.scrapepilotai.dto;

import com.nazarukiv.scrapepilotai.entity.ScrapeExecutionStatus;
import java.time.Instant;

public record ScrapeExecutionResponseDto(
        Long id,
        Long taskId,
        String title,
        String metaDescription,
        String firstH1,
        int totalLinks,
        ScrapeExecutionStatus status,
        String errorMessage,
        Instant executedAt,
        long responseTimeMillis
) {
}
