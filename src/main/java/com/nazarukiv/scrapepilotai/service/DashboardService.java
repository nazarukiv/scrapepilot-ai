package com.nazarukiv.scrapepilotai.service;

import com.nazarukiv.scrapepilotai.dto.DashboardStatsDto;
import com.nazarukiv.scrapepilotai.dto.DashboardViewDto;
import com.nazarukiv.scrapepilotai.dto.ExecutionOverviewDto;
import com.nazarukiv.scrapepilotai.dto.TaskExecutionSummaryDto;
import com.nazarukiv.scrapepilotai.entity.ScrapeExecution;
import com.nazarukiv.scrapepilotai.entity.ScrapeExecutionStatus;
import com.nazarukiv.scrapepilotai.entity.ScrapingTask;
import com.nazarukiv.scrapepilotai.repository.ScrapeExecutionRepository;
import com.nazarukiv.scrapepilotai.repository.ScrapingTaskRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private static final int HISTORY_LIMIT = 50;

    private final ScrapingTaskRepository scrapingTaskRepository;
    private final ScrapeExecutionRepository scrapeExecutionRepository;
    private final DashboardDisplayFormatter dashboardDisplayFormatter;

    public DashboardService(
            ScrapingTaskRepository scrapingTaskRepository,
            ScrapeExecutionRepository scrapeExecutionRepository,
            DashboardDisplayFormatter dashboardDisplayFormatter
    ) {
        this.scrapingTaskRepository = scrapingTaskRepository;
        this.scrapeExecutionRepository = scrapeExecutionRepository;
        this.dashboardDisplayFormatter = dashboardDisplayFormatter;
    }

    @Transactional(readOnly = true)
    public DashboardViewDto getDashboard(Long requestedTaskId) {
        List<ScrapingTask> tasks = scrapingTaskRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<ScrapeExecution> executions = scrapeExecutionRepository.findAllByOrderByExecutedAtDescIdDesc();
        Map<Long, List<ScrapeExecution>> executionsByTask = executions.stream()
                .collect(Collectors.groupingBy(execution -> execution.getTask().getId()));

        Optional<ScrapingTask> selectedTask = selectedTask(tasks, requestedTaskId);
        Long selectedTaskId = selectedTask.map(ScrapingTask::getId).orElse(null);
        String selectedTaskName = selectedTask.map(ScrapingTask::getName).orElse(null);

        return new DashboardViewDto(
                buildStats(tasks.size(), executions),
                buildTaskSummaries(tasks, executionsByTask),
                buildExecutionHistory(executions, selectedTaskId),
                selectedTaskId,
                selectedTaskName,
                HISTORY_LIMIT
        );
    }

    private Optional<ScrapingTask> selectedTask(List<ScrapingTask> tasks, Long requestedTaskId) {
        if (requestedTaskId == null) {
            return Optional.empty();
        }

        return tasks.stream()
                .filter(task -> Objects.equals(task.getId(), requestedTaskId))
                .findFirst();
    }

    private DashboardStatsDto buildStats(int totalTasks, List<ScrapeExecution> executions) {
        long totalExecutions = executions.size();
        long totalFailures = countStatus(executions, ScrapeExecutionStatus.FAILED);
        long totalSuccesses = countStatus(executions, ScrapeExecutionStatus.SUCCESS);
        Instant latestExecutionTimestamp = executions.isEmpty() ? null : executions.get(0).getExecutedAt();
        int overallSuccessRate = successRate(totalSuccesses, totalExecutions);
        long averageResponseTimeMillis = averageResponseTimeMillis(executions);
        boolean hasExecutions = totalExecutions > 0;

        return new DashboardStatsDto(
                totalTasks,
                totalExecutions,
                totalFailures,
                overallSuccessRate,
                dashboardDisplayFormatter.successRate(overallSuccessRate, hasExecutions),
                averageResponseTimeMillis,
                dashboardDisplayFormatter.responseTime(averageResponseTimeMillis, hasExecutions),
                latestExecutionTimestamp,
                dashboardDisplayFormatter.timestamp(latestExecutionTimestamp)
        );
    }

    private List<TaskExecutionSummaryDto> buildTaskSummaries(
            List<ScrapingTask> tasks,
            Map<Long, List<ScrapeExecution>> executionsByTask
    ) {
        return tasks.stream()
                .map(task -> buildTaskSummary(task, executionsByTask.getOrDefault(task.getId(), List.of())))
                .toList();
    }

    private TaskExecutionSummaryDto buildTaskSummary(ScrapingTask task, List<ScrapeExecution> executions) {
        long totalExecutions = executions.size();
        long successCount = countStatus(executions, ScrapeExecutionStatus.SUCCESS);
        long failureCount = countStatus(executions, ScrapeExecutionStatus.FAILED);
        ScrapeExecution latestExecution = executions.isEmpty() ? null : executions.get(0);
        int successRate = successRate(successCount, totalExecutions);
        long averageResponseTimeMillis = averageResponseTimeMillis(executions);
        boolean hasExecutions = totalExecutions > 0;
        Instant latestExecutionTimestamp = latestExecution == null ? null : latestExecution.getExecutedAt();

        return new TaskExecutionSummaryDto(
                task.getId(),
                task.getName(),
                task.getUrl(),
                task.isActive(),
                totalExecutions,
                successCount,
                failureCount,
                successRate,
                dashboardDisplayFormatter.successRate(successRate, hasExecutions),
                averageResponseTimeMillis,
                dashboardDisplayFormatter.responseTime(averageResponseTimeMillis, hasExecutions),
                latestExecutionTimestamp,
                dashboardDisplayFormatter.timestamp(latestExecutionTimestamp),
                latestExecution == null ? null : latestExecution.getStatus()
        );
    }

    private List<ExecutionOverviewDto> buildExecutionHistory(List<ScrapeExecution> executions, Long selectedTaskId) {
        return executions.stream()
                .filter(execution -> selectedTaskId == null || Objects.equals(execution.getTask().getId(), selectedTaskId))
                .limit(HISTORY_LIMIT)
                .map(this::toExecutionOverview)
                .toList();
    }

    private ExecutionOverviewDto toExecutionOverview(ScrapeExecution execution) {
        return new ExecutionOverviewDto(
                execution.getId(),
                execution.getTask().getId(),
                execution.getTask().getName(),
                execution.getStatus(),
                execution.getResponseTimeMillis(),
                dashboardDisplayFormatter.responseTime(execution.getResponseTimeMillis(), true),
                execution.getExecutedAt(),
                dashboardDisplayFormatter.tableTimestamp(execution.getExecutedAt()),
                cleanedText(execution.getTitle()),
                cleanedText(execution.getErrorMessage())
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

    private String cleanedText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
