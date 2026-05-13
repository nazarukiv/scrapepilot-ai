package com.nazarukiv.scrapepilotai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.nazarukiv.scrapepilotai.dto.DashboardViewDto;
import com.nazarukiv.scrapepilotai.dto.TaskExecutionSummaryDto;
import com.nazarukiv.scrapepilotai.entity.ScrapeExecution;
import com.nazarukiv.scrapepilotai.entity.ScrapeExecutionStatus;
import com.nazarukiv.scrapepilotai.entity.ScrapingTask;
import com.nazarukiv.scrapepilotai.repository.ScrapeExecutionRepository;
import com.nazarukiv.scrapepilotai.repository.ScrapingTaskRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTests {

    @Mock
    private ScrapingTaskRepository scrapingTaskRepository;

    @Mock
    private ScrapeExecutionRepository scrapeExecutionRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(
                scrapingTaskRepository,
                scrapeExecutionRepository,
                new DashboardDisplayFormatter()
        );
    }

    @Test
    void aggregatesDashboardMetricsAndTaskSummaries() {
        ScrapingTask firstTask = task(1L, "Docs monitor", true);
        ScrapingTask secondTask = task(2L, "Status monitor", false);
        Instant now = Instant.parse("2026-05-13T10:15:30Z");

        ScrapeExecution latestSuccess = execution(
                3L,
                firstTask,
                ScrapeExecutionStatus.SUCCESS,
                "Docs",
                null,
                100,
                now
        );
        ScrapeExecution failure = execution(
                2L,
                firstTask,
                ScrapeExecutionStatus.FAILED,
                null,
                "Timeout",
                300,
                now.minusSeconds(60)
        );
        ScrapeExecution otherSuccess = execution(
                1L,
                secondTask,
                ScrapeExecutionStatus.SUCCESS,
                "Status",
                null,
                200,
                now.minusSeconds(120)
        );

        when(scrapingTaskRepository.findAll(any(Sort.class))).thenReturn(List.of(firstTask, secondTask));
        when(scrapeExecutionRepository.findAllByOrderByExecutedAtDescIdDesc())
                .thenReturn(List.of(latestSuccess, failure, otherSuccess));

        DashboardViewDto dashboard = dashboardService.getDashboard(null);

        assertEquals(2, dashboard.stats().totalTasks());
        assertEquals(3, dashboard.stats().totalExecutions());
        assertEquals(1, dashboard.stats().totalFailures());
        assertEquals(67, dashboard.stats().overallSuccessRate());
        assertEquals("67%", dashboard.stats().overallSuccessRateLabel());
        assertEquals(200, dashboard.stats().averageResponseTimeMillis());
        assertEquals("200ms", dashboard.stats().averageResponseTimeLabel());
        assertEquals(now, dashboard.stats().latestExecutionTimestamp());
        assertEquals("13 May 2026, 10:15", dashboard.stats().latestExecutionTimestampLabel());
        assertEquals(3, dashboard.executionHistory().size());

        TaskExecutionSummaryDto firstSummary = dashboard.taskSummaries().stream()
                .filter(summary -> summary.taskId().equals(1L))
                .findFirst()
                .orElseThrow();

        assertEquals(2, firstSummary.totalExecutions());
        assertEquals(1, firstSummary.successCount());
        assertEquals(1, firstSummary.failureCount());
        assertEquals(50, firstSummary.successRate());
        assertEquals("50%", firstSummary.successRateLabel());
        assertEquals(200, firstSummary.averageResponseTimeMillis());
        assertEquals("200ms", firstSummary.averageResponseTimeLabel());
        assertEquals(ScrapeExecutionStatus.SUCCESS, firstSummary.latestExecutionStatus());
        assertEquals(now, firstSummary.latestExecutionTimestamp());
        assertEquals("13 May 2026, 10:15", firstSummary.latestExecutionTimestampLabel());
    }

    @Test
    void filtersExecutionHistoryBySelectedTaskWithoutChangingGlobalStats() {
        ScrapingTask firstTask = task(1L, "Docs monitor", true);
        ScrapingTask secondTask = task(2L, "Status monitor", true);
        Instant now = Instant.parse("2026-05-13T10:15:30Z");

        ScrapeExecution firstExecution = execution(
                2L,
                firstTask,
                ScrapeExecutionStatus.SUCCESS,
                "Docs",
                null,
                120,
                now
        );
        ScrapeExecution secondExecution = execution(
                1L,
                secondTask,
                ScrapeExecutionStatus.SUCCESS,
                "Status",
                null,
                180,
                now.minusSeconds(60)
        );

        when(scrapingTaskRepository.findAll(any(Sort.class))).thenReturn(List.of(firstTask, secondTask));
        when(scrapeExecutionRepository.findAllByOrderByExecutedAtDescIdDesc())
                .thenReturn(List.of(firstExecution, secondExecution));

        DashboardViewDto dashboard = dashboardService.getDashboard(2L);

        assertEquals(2, dashboard.stats().totalExecutions());
        assertEquals(2L, dashboard.selectedTaskId());
        assertEquals("Status monitor", dashboard.selectedTaskName());
        assertEquals(1, dashboard.executionHistory().size());
        assertEquals(2L, dashboard.executionHistory().get(0).taskId());
        assertEquals("13 May 2026, 10:14", dashboard.executionHistory().get(0).executedAtLabel());
        assertEquals("180ms", dashboard.executionHistory().get(0).responseTimeLabel());
    }

    @Test
    void returnsZeroMetricsForEmptyDashboard() {
        when(scrapingTaskRepository.findAll(any(Sort.class))).thenReturn(List.of());
        when(scrapeExecutionRepository.findAllByOrderByExecutedAtDescIdDesc()).thenReturn(List.of());

        DashboardViewDto dashboard = dashboardService.getDashboard(null);

        assertEquals(0, dashboard.stats().totalTasks());
        assertEquals(0, dashboard.stats().totalExecutions());
        assertEquals(0, dashboard.stats().totalFailures());
        assertEquals(0, dashboard.stats().overallSuccessRate());
        assertEquals("No runs", dashboard.stats().overallSuccessRateLabel());
        assertEquals(0, dashboard.stats().averageResponseTimeMillis());
        assertEquals("No samples", dashboard.stats().averageResponseTimeLabel());
        assertNull(dashboard.stats().latestExecutionTimestamp());
        assertEquals("Waiting for first execution", dashboard.stats().latestExecutionTimestampLabel());
        assertTrue(dashboard.taskSummaries().isEmpty());
        assertTrue(dashboard.executionHistory().isEmpty());
    }

    private ScrapingTask task(Long id, String name, boolean active) {
        ScrapingTask task = new ScrapingTask(name, "https://example.com/" + id);
        task.update(task.getName(), task.getUrl(), active, null);
        ReflectionTestUtils.setField(task, "id", id);
        ReflectionTestUtils.setField(task, "createdAt", Instant.parse("2026-05-13T09:00:00Z"));
        return task;
    }

    private ScrapeExecution execution(
            Long id,
            ScrapingTask task,
            ScrapeExecutionStatus status,
            String title,
            String errorMessage,
            long responseTimeMillis,
            Instant executedAt
    ) {
        ScrapeExecution execution = new ScrapeExecution(
                task,
                title,
                null,
                null,
                0,
                status,
                errorMessage,
                responseTimeMillis
        );
        ReflectionTestUtils.setField(execution, "id", id);
        ReflectionTestUtils.setField(execution, "executedAt", executedAt);
        return execution;
    }
}
