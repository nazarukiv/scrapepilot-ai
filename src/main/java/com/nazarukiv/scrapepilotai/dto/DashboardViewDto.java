package com.nazarukiv.scrapepilotai.dto;

import java.util.List;

public record DashboardViewDto(
        DashboardStatsDto stats,
        List<TaskExecutionSummaryDto> taskSummaries,
        List<ExecutionOverviewDto> executionHistory,
        Long selectedTaskId,
        String selectedTaskName,
        int historyLimit
) {
}
