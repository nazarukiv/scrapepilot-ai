package com.nazarukiv.scrapepilotai.service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class DashboardDisplayFormatter {

    private static final String NO_EXECUTIONS_LABEL = "Waiting for first execution";
    private static final String NO_RUNS_LABEL = "No runs";
    private static final String NO_SAMPLES_LABEL = "No samples";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter
            .ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH)
            .withZone(ZoneOffset.UTC);

    public String timestamp(Instant timestamp) {
        return timestamp == null ? NO_EXECUTIONS_LABEL : TIMESTAMP_FORMATTER.format(timestamp);
    }

    public String tableTimestamp(Instant timestamp) {
        return timestamp == null ? "-" : TIMESTAMP_FORMATTER.format(timestamp);
    }

    public String successRate(int successRate, boolean hasExecutions) {
        return hasExecutions ? successRate + "%" : NO_RUNS_LABEL;
    }

    public String responseTime(long responseTimeMillis, boolean hasExecutions) {
        return hasExecutions ? responseTimeMillis + "ms" : NO_SAMPLES_LABEL;
    }

    public String interval(Integer intervalSeconds) {
        if (intervalSeconds == null) {
            return "Manual trigger";
        }

        if (intervalSeconds < 60) {
            return "Every " + intervalSeconds + "s";
        }

        if (intervalSeconds % 3_600 == 0) {
            return "Every " + (intervalSeconds / 3_600) + "h";
        }

        if (intervalSeconds % 60 == 0) {
            return "Every " + (intervalSeconds / 60) + "m";
        }

        return "Every " + intervalSeconds + "s";
    }
}
