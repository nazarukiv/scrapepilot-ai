package com.nazarukiv.scrapepilotai.dto;

import com.nazarukiv.scrapepilotai.entity.ScrapeExecutionStatus;
import java.time.Instant;

public record TaskExecutionSummaryDto(
        Long taskId,
        String taskName,
        String targetUrl,
        boolean active,
        long totalExecutions,
        long successCount,
        long failureCount,
        int successRate,
        String successRateLabel,
        long averageResponseTimeMillis,
        String averageResponseTimeLabel,
        Instant latestExecutionTimestamp,
        String latestExecutionTimestampLabel,
        ScrapeExecutionStatus latestExecutionStatus
) {
}
