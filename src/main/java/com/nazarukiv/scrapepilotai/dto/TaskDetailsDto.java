package com.nazarukiv.scrapepilotai.dto;

import com.nazarukiv.scrapepilotai.entity.ScrapeExecutionStatus;
import java.time.Instant;
import java.util.List;

public record TaskDetailsDto(
        Long taskId,
        String taskName,
        String targetUrl,
        boolean active,
        Integer executionIntervalSeconds,
        String executionIntervalLabel,
        Instant createdAt,
        String createdAtLabel,
        Instant lastExecutedAt,
        String lastExecutedAtLabel,
        ScrapeExecutionStatus latestExecutionStatus,
        String latestExecutionStatusLabel,
        long totalExecutions,
        long successCount,
        long failureCount,
        int successRate,
        String successRateLabel,
        long averageResponseTimeMillis,
        String averageResponseTimeLabel,
        TaskHealthDto health,
        TaskExecutionAnalyticsDto analytics,
        List<TaskExecutionDetailDto> executionHistory
) {
}
