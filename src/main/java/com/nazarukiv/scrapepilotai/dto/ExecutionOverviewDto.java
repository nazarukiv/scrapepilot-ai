package com.nazarukiv.scrapepilotai.dto;

import com.nazarukiv.scrapepilotai.entity.ScrapeExecutionStatus;
import java.time.Instant;

public record ExecutionOverviewDto(
        Long executionId,
        Long taskId,
        String taskName,
        ScrapeExecutionStatus status,
        long responseTimeMillis,
        String responseTimeLabel,
        Instant executedAt,
        String executedAtLabel,
        String extractedTitle,
        String errorMessage
) {
}
