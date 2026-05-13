package com.nazarukiv.scrapepilotai.dto;

public record TaskExecutionAnalyticsDto(
        long totalExecutions,
        long successCount,
        long failureCount,
        int recentSuccessRate,
        String recentSuccessRateLabel,
        long averageResponseTimeMillis,
        String averageResponseTimeLabel,
        long fastestResponseTimeMillis,
        String fastestResponseTimeLabel,
        long slowestResponseTimeMillis,
        String slowestResponseTimeLabel
) {
}
