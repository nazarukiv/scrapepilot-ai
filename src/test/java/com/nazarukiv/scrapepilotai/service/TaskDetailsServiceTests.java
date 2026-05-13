package com.nazarukiv.scrapepilotai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.nazarukiv.scrapepilotai.dto.TaskDetailsDto;
import com.nazarukiv.scrapepilotai.entity.ScrapeExecution;
import com.nazarukiv.scrapepilotai.entity.ScrapeExecutionStatus;
import com.nazarukiv.scrapepilotai.entity.ScrapingTask;
import com.nazarukiv.scrapepilotai.repository.ScrapeExecutionRepository;
import com.nazarukiv.scrapepilotai.repository.ScrapingTaskRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TaskDetailsServiceTests {

    @Mock
    private ScrapingTaskRepository scrapingTaskRepository;

    @Mock
    private ScrapeExecutionRepository scrapeExecutionRepository;

    private TaskDetailsService taskDetailsService;

    @BeforeEach
    void setUp() {
        taskDetailsService = new TaskDetailsService(
                scrapingTaskRepository,
                scrapeExecutionRepository,
                new DashboardDisplayFormatter()
        );
    }

    @Test
    void aggregatesTaskDetailsHealthAnalyticsAndHistory() {
        ScrapingTask task = task(1L, "Docs monitor", true, 300);
        Instant now = Instant.parse("2026-05-13T10:15:30Z");
        ScrapeExecution latestFailure = execution(
                3L,
                task,
                ScrapeExecutionStatus.FAILED,
                null,
                null,
                "Timeout",
                300,
                now
        );
        ScrapeExecution previousFailure = execution(
                2L,
                task,
                ScrapeExecutionStatus.FAILED,
                null,
                null,
                "Connection reset",
                200,
                now.minusSeconds(60)
        );
        ScrapeExecution success = execution(
                1L,
                task,
                ScrapeExecutionStatus.SUCCESS,
                "Docs",
                "Documentation",
                null,
                100,
                now.minusSeconds(120)
        );

        when(scrapingTaskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(scrapeExecutionRepository.findByTaskOrderByExecutedAtDescIdDesc(task))
                .thenReturn(List.of(latestFailure, previousFailure, success));

        TaskDetailsDto details = taskDetailsService.getTaskDetails(1L);

        assertEquals(1L, details.taskId());
        assertEquals("Docs monitor", details.taskName());
        assertEquals("Every 5m", details.executionIntervalLabel());
        assertEquals(ScrapeExecutionStatus.FAILED, details.latestExecutionStatus());
        assertEquals("FAILED", details.latestExecutionStatusLabel());
        assertEquals(3, details.totalExecutions());
        assertEquals(1, details.successCount());
        assertEquals(2, details.failureCount());
        assertEquals(33, details.successRate());
        assertEquals("33%", details.successRateLabel());
        assertEquals(200, details.averageResponseTimeMillis());
        assertEquals("200ms", details.averageResponseTimeLabel());

        assertEquals("DEGRADED", details.health().healthLabel());
        assertEquals("status-waiting", details.health().healthBadgeClass());
        assertEquals("Scheduled", details.health().schedulerStateLabel());
        assertEquals("Every 5m", details.health().executionFrequencyLabel());
        assertEquals("13 May 2026, 10:13", details.health().latestSuccessfulRunLabel());
        assertEquals("13 May 2026, 10:15", details.health().latestFailedRunLabel());
        assertEquals(2, details.health().consecutiveFailures());

        assertEquals(33, details.analytics().recentSuccessRate());
        assertEquals("33%", details.analytics().recentSuccessRateLabel());
        assertEquals(100, details.analytics().fastestResponseTimeMillis());
        assertEquals("100ms", details.analytics().fastestResponseTimeLabel());
        assertEquals(300, details.analytics().slowestResponseTimeMillis());
        assertEquals("300ms", details.analytics().slowestResponseTimeLabel());

        assertEquals(3, details.executionHistory().size());
        assertEquals("FAILED", details.executionHistory().get(0).statusLabel());
        assertEquals("300ms", details.executionHistory().get(0).responseTimeLabel());
        assertEquals("13 May 2026, 10:15", details.executionHistory().get(0).executedAtLabel());
        assertEquals("Timeout", details.executionHistory().get(0).errorMessage());
        assertEquals("7", details.executionHistory().get(2).totalLinksLabel());
        assertEquals("Documentation", details.executionHistory().get(2).firstH1());
    }

    @Test
    void returnsWaitingStateForTaskWithoutExecutions() {
        ScrapingTask task = task(2L, "Manual monitor", true, null);
        when(scrapingTaskRepository.findById(2L)).thenReturn(Optional.of(task));
        when(scrapeExecutionRepository.findByTaskOrderByExecutedAtDescIdDesc(task)).thenReturn(List.of());

        TaskDetailsDto details = taskDetailsService.getTaskDetails(2L);

        assertNull(details.latestExecutionStatus());
        assertEquals("WAITING", details.latestExecutionStatusLabel());
        assertEquals("Manual trigger", details.executionIntervalLabel());
        assertEquals("WAITING", details.health().healthLabel());
        assertEquals("status-waiting", details.health().healthBadgeClass());
        assertEquals("Manual", details.health().schedulerStateLabel());
        assertEquals("No successful runs", details.health().latestSuccessfulRunLabel());
        assertEquals("No failed runs", details.health().latestFailedRunLabel());
        assertEquals("No runs", details.successRateLabel());
        assertEquals("No samples", details.averageResponseTimeLabel());
        assertEquals("No runs", details.analytics().recentSuccessRateLabel());
        assertTrue(details.executionHistory().isEmpty());
    }

    @Test
    void returnsPausedHealthForInactiveTask() {
        ScrapingTask task = task(3L, "Paused monitor", false, 60);
        when(scrapingTaskRepository.findById(3L)).thenReturn(Optional.of(task));
        when(scrapeExecutionRepository.findByTaskOrderByExecutedAtDescIdDesc(task)).thenReturn(List.of());

        TaskDetailsDto details = taskDetailsService.getTaskDetails(3L);

        assertEquals("PAUSED", details.health().healthLabel());
        assertEquals("status-inactive", details.health().healthBadgeClass());
        assertEquals("Paused", details.health().schedulerStateLabel());
    }

    private ScrapingTask task(Long id, String name, boolean active, Integer executionIntervalSeconds) {
        ScrapingTask task = new ScrapingTask(name, "https://example.com/" + id, executionIntervalSeconds);
        task.update(task.getName(), task.getUrl(), active, executionIntervalSeconds);
        ReflectionTestUtils.setField(task, "id", id);
        ReflectionTestUtils.setField(task, "createdAt", Instant.parse("2026-05-13T09:00:00Z"));
        return task;
    }

    private ScrapeExecution execution(
            Long id,
            ScrapingTask task,
            ScrapeExecutionStatus status,
            String title,
            String firstH1,
            String errorMessage,
            long responseTimeMillis,
            Instant executedAt
    ) {
        ScrapeExecution execution = new ScrapeExecution(
                task,
                title,
                null,
                firstH1,
                7,
                status,
                errorMessage,
                responseTimeMillis
        );
        ReflectionTestUtils.setField(execution, "id", id);
        ReflectionTestUtils.setField(execution, "executedAt", executedAt);
        return execution;
    }
}
