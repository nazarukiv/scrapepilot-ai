package com.nazarukiv.scrapepilotai.service;

import com.nazarukiv.scrapepilotai.dto.TaskDetailsDto;
import com.nazarukiv.scrapepilotai.dto.TaskExecutionAnalyticsDto;
import com.nazarukiv.scrapepilotai.dto.TaskExecutionDetailDto;
import com.nazarukiv.scrapepilotai.dto.TaskHealthDto;
import com.nazarukiv.scrapepilotai.entity.ScrapeExecution;
import com.nazarukiv.scrapepilotai.entity.ScrapeExecutionStatus;
import com.nazarukiv.scrapepilotai.entity.ScrapingTask;
import com.nazarukiv.scrapepilotai.exception.ScrapingTaskNotFoundException;
import com.nazarukiv.scrapepilotai.repository.ScrapeExecutionRepository;
import com.nazarukiv.scrapepilotai.repository.ScrapingTaskRepository;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskDetailsService {

    private static final int RECENT_EXECUTION_LIMIT = 5;

    private final ScrapingTaskRepository scrapingTaskRepository;
    private final ScrapeExecutionRepository scrapeExecutionRepository;
    private final DashboardDisplayFormatter dashboardDisplayFormatter;

    public TaskDetailsService(
            ScrapingTaskRepository scrapingTaskRepository,
            ScrapeExecutionRepository scrapeExecutionRepository,
            DashboardDisplayFormatter dashboardDisplayFormatter
    ) {
        this.scrapingTaskRepository = scrapingTaskRepository;
        this.scrapeExecutionRepository = scrapeExecutionRepository;
        this.dashboardDisplayFormatter = dashboardDisplayFormatter;
    }

    @Transactional(readOnly = true)
    public TaskDetailsDto getTaskDetails(Long taskId) {
        ScrapingTask task = scrapingTaskRepository.findById(taskId)
                .orElseThrow(() -> new ScrapingTaskNotFoundException(taskId));
        List<ScrapeExecution> executions = scrapeExecutionRepository.findByTaskOrderByExecutedAtDescIdDesc(task);

        long totalExecutions = executions.size();
        long successCount = countStatus(executions, ScrapeExecutionStatus.SUCCESS);
        long failureCount = countStatus(executions, ScrapeExecutionStatus.FAILED);
        int successRate = successRate(successCount, totalExecutions);
        long averageResponseTimeMillis = averageResponseTimeMillis(executions);
        ScrapeExecution latestExecution = executions.isEmpty() ? null : executions.get(0);
        ScrapeExecution latestSuccess = latestExecutionWithStatus(executions, ScrapeExecutionStatus.SUCCESS);
        ScrapeExecution latestFailure = latestExecutionWithStatus(executions, ScrapeExecutionStatus.FAILED);
        boolean hasExecutions = totalExecutions > 0;

        return new TaskDetailsDto(
                task.getId(),
                task.getName(),
                task.getUrl(),
                task.isActive(),
                task.getExecutionIntervalSeconds(),
                dashboardDisplayFormatter.interval(task.getExecutionIntervalSeconds()),
                task.getCreatedAt(),
                dashboardDisplayFormatter.tableTimestamp(task.getCreatedAt()),
                task.getLastExecutedAt(),
                dashboardDisplayFormatter.timestamp(task.getLastExecutedAt()),
                latestExecution == null ? null : latestExecution.getStatus(),
                latestExecution == null ? "WAITING" : latestExecution.getStatus().name(),
                totalExecutions,
                successCount,
                failureCount,
                successRate,
                dashboardDisplayFormatter.successRate(successRate, hasExecutions),
                averageResponseTimeMillis,
                dashboardDisplayFormatter.responseTime(averageResponseTimeMillis, hasExecutions),
                buildHealth(task, executions, latestExecution, latestSuccess, latestFailure),
                buildAnalytics(executions, totalExecutions, successCount, failureCount),
                executions.stream().map(this::toExecutionDetail).toList()
        );
    }

    private TaskHealthDto buildHealth(
            ScrapingTask task,
            List<ScrapeExecution> executions,
            ScrapeExecution latestExecution,
            ScrapeExecution latestSuccess,
            ScrapeExecution latestFailure
    ) {
        long consecutiveFailures = consecutiveFailures(executions);

        return new TaskHealthDto(
                healthLabel(task, latestExecution, consecutiveFailures),
                healthBadgeClass(task, latestExecution, consecutiveFailures),
                schedulerStateLabel(task),
                dashboardDisplayFormatter.interval(task.getExecutionIntervalSeconds()),
                latestExecutionLabel(latestSuccess, "No successful runs"),
                latestExecutionLabel(latestFailure, "No failed runs"),
                consecutiveFailures
        );
    }

    private TaskExecutionAnalyticsDto buildAnalytics(
            List<ScrapeExecution> executions,
            long totalExecutions,
            long successCount,
            long failureCount
    ) {
        boolean hasExecutions = totalExecutions > 0;
        List<ScrapeExecution> recentExecutions = executions.stream()
                .limit(RECENT_EXECUTION_LIMIT)
                .toList();
        long recentSuccessCount = countStatus(recentExecutions, ScrapeExecutionStatus.SUCCESS);
        int recentSuccessRate = successRate(recentSuccessCount, recentExecutions.size());
        long averageResponseTimeMillis = averageResponseTimeMillis(executions);
        long fastestResponseTimeMillis = executions.stream()
                .min(Comparator.comparingLong(ScrapeExecution::getResponseTimeMillis))
                .map(ScrapeExecution::getResponseTimeMillis)
                .orElse(0L);
        long slowestResponseTimeMillis = executions.stream()
                .max(Comparator.comparingLong(ScrapeExecution::getResponseTimeMillis))
                .map(ScrapeExecution::getResponseTimeMillis)
                .orElse(0L);

        return new TaskExecutionAnalyticsDto(
                totalExecutions,
                successCount,
                failureCount,
                recentSuccessRate,
                dashboardDisplayFormatter.successRate(recentSuccessRate, !recentExecutions.isEmpty()),
                averageResponseTimeMillis,
                dashboardDisplayFormatter.responseTime(averageResponseTimeMillis, hasExecutions),
                fastestResponseTimeMillis,
                dashboardDisplayFormatter.responseTime(fastestResponseTimeMillis, hasExecutions),
                slowestResponseTimeMillis,
                dashboardDisplayFormatter.responseTime(slowestResponseTimeMillis, hasExecutions)
        );
    }

    private TaskExecutionDetailDto toExecutionDetail(ScrapeExecution execution) {
        return new TaskExecutionDetailDto(
                execution.getId(),
                execution.getStatus(),
                execution.getStatus().name(),
                execution.getResponseTimeMillis(),
                dashboardDisplayFormatter.responseTime(execution.getResponseTimeMillis(), true),
                execution.getExecutedAt(),
                dashboardDisplayFormatter.tableTimestamp(execution.getExecutedAt()),
                cleanedText(execution.getTitle()),
                cleanedText(execution.getErrorMessage()),
                execution.getTotalLinks(),
                Integer.toString(execution.getTotalLinks()),
                cleanedText(execution.getFirstH1())
        );
    }

    private long countStatus(List<ScrapeExecution> executions, ScrapeExecutionStatus status) {
        return executions.stream()
                .filter(execution -> execution.getStatus() == status)
                .count();
    }

    private int successRate(long successes, long totalExecutions) {
        if (totalExecutions == 0) {
            return 0;
        }

        return (int) Math.round((successes * 100.0) / totalExecutions);
    }

    private long averageResponseTimeMillis(List<ScrapeExecution> executions) {
        return Math.round(executions.stream()
                .mapToLong(ScrapeExecution::getResponseTimeMillis)
                .average()
                .orElse(0));
    }

    private ScrapeExecution latestExecutionWithStatus(List<ScrapeExecution> executions, ScrapeExecutionStatus status) {
        return executions.stream()
                .filter(execution -> execution.getStatus() == status)
                .findFirst()
                .orElse(null);
    }

    private long consecutiveFailures(List<ScrapeExecution> executions) {
        long failures = 0;
        for (ScrapeExecution execution : executions) {
            if (execution.getStatus() != ScrapeExecutionStatus.FAILED) {
                break;
            }
            failures++;
        }
        return failures;
    }

    private String healthLabel(ScrapingTask task, ScrapeExecution latestExecution, long consecutiveFailures) {
        if (!task.isActive()) {
            return "PAUSED";
        }

        if (latestExecution == null) {
            return "WAITING";
        }

        if (latestExecution.getStatus() == ScrapeExecutionStatus.SUCCESS) {
            return "HEALTHY";
        }

        return consecutiveFailures >= 3 ? "DOWN" : "DEGRADED";
    }

    private String healthBadgeClass(ScrapingTask task, ScrapeExecution latestExecution, long consecutiveFailures) {
        if (!task.isActive()) {
            return "status-inactive";
        }

        if (latestExecution == null) {
            return "status-waiting";
        }

        if (latestExecution.getStatus() == ScrapeExecutionStatus.SUCCESS) {
            return "status-success";
        }

        return consecutiveFailures >= 3 ? "status-failed" : "status-waiting";
    }

    private String schedulerStateLabel(ScrapingTask task) {
        if (!task.isActive()) {
            return "Paused";
        }

        return task.getExecutionIntervalSeconds() == null ? "Manual" : "Scheduled";
    }

    private String latestExecutionLabel(ScrapeExecution execution, String emptyLabel) {
        if (execution == null) {
            return emptyLabel;
        }

        Instant executedAt = execution.getExecutedAt();
        return dashboardDisplayFormatter.tableTimestamp(executedAt);
    }

    private String cleanedText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
