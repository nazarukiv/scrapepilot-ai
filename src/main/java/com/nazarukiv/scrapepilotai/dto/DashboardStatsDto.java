package com.nazarukiv.scrapepilotai.dto;

import java.time.Instant;

public record DashboardStatsDto(
        long totalTasks,
        long totalExecutions,
        long totalFailures,
        int overallSuccessRate,
        String overallSuccessRateLabel,
        long averageResponseTimeMillis,
        String averageResponseTimeLabel,
        Instant latestExecutionTimestamp,
        String latestExecutionTimestampLabel
) {
}
