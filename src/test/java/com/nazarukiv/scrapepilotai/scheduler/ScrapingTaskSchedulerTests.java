package com.nazarukiv.scrapepilotai.scheduler;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nazarukiv.scrapepilotai.entity.ScrapingTask;
import com.nazarukiv.scrapepilotai.service.ScrapeExecutionService;
import com.nazarukiv.scrapepilotai.service.ScrapingTaskService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ScrapingTaskSchedulerTests {

    @Mock
    private ScrapingTaskService scrapingTaskService;

    @Mock
    private ScrapeExecutionService scrapeExecutionService;

    @InjectMocks
    private ScrapingTaskScheduler scrapingTaskScheduler;

    @Test
    void runsTaskImmediatelyWhenItHasNeverExecuted() {
        ScrapingTask task = task(1L, 30);
        when(scrapingTaskService.getActiveTasksForScheduling()).thenReturn(List.of(task));

        scrapingTaskScheduler.runScheduledTasks();

        verify(scrapeExecutionService).executeTask(1L);
    }

    @Test
    void skipsTaskWhenIntervalHasNotBeenReached() {
        ScrapingTask task = task(1L, 3_600);
        task.markExecutedAt(Instant.now());
        when(scrapingTaskService.getActiveTasksForScheduling()).thenReturn(List.of(task));

        scrapingTaskScheduler.runScheduledTasks();

        verify(scrapeExecutionService, never()).executeTask(1L);
    }

    @Test
    void ignoresTaskWithoutExecutionInterval() {
        ScrapingTask task = task(1L, null);
        when(scrapingTaskService.getActiveTasksForScheduling()).thenReturn(List.of(task));

        scrapingTaskScheduler.runScheduledTasks();

        verify(scrapeExecutionService, never()).executeTask(1L);
    }

    @Test
    void continuesAfterOneScheduledTaskFails() {
        ScrapingTask firstTask = task(1L, 30);
        ScrapingTask secondTask = task(2L, 30);
        when(scrapingTaskService.getActiveTasksForScheduling()).thenReturn(List.of(firstTask, secondTask));
        when(scrapeExecutionService.executeTask(1L)).thenThrow(new RuntimeException("Scrape failed"));

        scrapingTaskScheduler.runScheduledTasks();

        verify(scrapeExecutionService).executeTask(1L);
        verify(scrapeExecutionService).executeTask(2L);
    }

    private ScrapingTask task(Long id, Integer executionIntervalSeconds) {
        ScrapingTask task = new ScrapingTask("Task " + id, "https://example.com", executionIntervalSeconds);
        ReflectionTestUtils.setField(task, "id", id);
        return task;
    }
}
