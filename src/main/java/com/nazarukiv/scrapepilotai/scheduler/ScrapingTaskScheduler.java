package com.nazarukiv.scrapepilotai.scheduler;

import com.nazarukiv.scrapepilotai.entity.ScrapingTask;
import com.nazarukiv.scrapepilotai.service.ScrapeExecutionService;
import com.nazarukiv.scrapepilotai.service.ScrapingTaskService;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "scrapepilot.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class ScrapingTaskScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScrapingTaskScheduler.class);

    private final ScrapingTaskService scrapingTaskService;
    private final ScrapeExecutionService scrapeExecutionService;

    public ScrapingTaskScheduler(
            ScrapingTaskService scrapingTaskService,
            ScrapeExecutionService scrapeExecutionService
    ) {
        this.scrapingTaskService = scrapingTaskService;
        this.scrapeExecutionService = scrapeExecutionService;
    }

    @Scheduled(fixedDelay = 30_000)
    public void runScheduledTasks() {
        Instant now = Instant.now();

        for (ScrapingTask task : scrapingTaskService.getActiveTasksForScheduling()) {
            try {
                runTaskIfDue(task, now);
            } catch (Exception exception) {
                LOGGER.warn("Scheduled task {} failed: {}", task.getId(), safeMessage(exception));
            }
        }
    }

    private void runTaskIfDue(ScrapingTask task, Instant now) {
        Integer intervalSeconds = task.getExecutionIntervalSeconds();
        if (intervalSeconds == null) {
            return;
        }

        if (!isDue(task, now, intervalSeconds)) {
            LOGGER.info("Skipping task {} - interval not reached", task.getId());
            return;
        }

        LOGGER.info("Executing scheduled task: {}", task.getId());
        scrapeExecutionService.executeTask(task.getId());
    }

    private boolean isDue(ScrapingTask task, Instant now, int intervalSeconds) {
        Instant lastExecutedAt = task.getLastExecutedAt();
        if (lastExecutedAt == null) {
            return true;
        }

        return !lastExecutedAt.plus(Duration.ofSeconds(intervalSeconds)).isAfter(now);
    }

    private String safeMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName() : message;
    }
}
