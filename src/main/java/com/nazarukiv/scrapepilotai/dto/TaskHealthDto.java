package com.nazarukiv.scrapepilotai.dto;

public record TaskHealthDto(
        String healthLabel,
        String healthBadgeClass,
        String schedulerStateLabel,
        String executionFrequencyLabel,
        String latestSuccessfulRunLabel,
        String latestFailedRunLabel,
        long consecutiveFailures
) {
}
