package com.nazarukiv.scrapepilotai.dto;

import com.nazarukiv.scrapepilotai.entity.ScrapeExecutionStatus;
import java.time.Instant;

public record TaskExecutionDetailDto(
        Long executionId,
        ScrapeExecutionStatus status,
        String statusLabel,
        long responseTimeMillis,
        String responseTimeLabel,
        Instant executedAt,
        String executedAtLabel,
        String extractedTitle,
        String errorMessage,
        int totalLinks,
        String totalLinksLabel,
        String firstH1
) {
}
