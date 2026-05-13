package com.nazarukiv.scrapepilotai.service;

import com.nazarukiv.scrapepilotai.dto.ScrapeExecutionResponseDto;
import com.nazarukiv.scrapepilotai.dto.ScrapeTitleResponseDto;
import com.nazarukiv.scrapepilotai.entity.ScrapeExecution;
import com.nazarukiv.scrapepilotai.entity.ScrapeExecutionStatus;
import com.nazarukiv.scrapepilotai.entity.ScrapingTask;
import com.nazarukiv.scrapepilotai.exception.InactiveScrapingTaskException;
import com.nazarukiv.scrapepilotai.exception.ScrapingTaskNotFoundException;
import com.nazarukiv.scrapepilotai.repository.ScrapeExecutionRepository;
import com.nazarukiv.scrapepilotai.repository.ScrapingTaskRepository;
import com.nazarukiv.scrapepilotai.scraper.JsoupScraperService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScrapeExecutionService {

    private static final int ERROR_MESSAGE_MAX_LENGTH = 2_000;

    private final ScrapingTaskRepository scrapingTaskRepository;
    private final ScrapeExecutionRepository scrapeExecutionRepository;
    private final JsoupScraperService jsoupScraperService;

    public ScrapeExecutionService(
            ScrapingTaskRepository scrapingTaskRepository,
            ScrapeExecutionRepository scrapeExecutionRepository,
            JsoupScraperService jsoupScraperService
    ) {
        this.scrapingTaskRepository = scrapingTaskRepository;
        this.scrapeExecutionRepository = scrapeExecutionRepository;
        this.jsoupScraperService = jsoupScraperService;
    }

    @Transactional
    public ScrapeExecutionResponseDto executeTask(Long taskId) {
        ScrapingTask task = getTaskOrThrow(taskId);
        ensureTaskIsActive(task);

        long startedAt = System.nanoTime();

        try {
            ScrapeTitleResponseDto scrapeResult = jsoupScraperService.scrapeTitle(task.getUrl());
            ScrapeExecution execution = new ScrapeExecution(
                    task,
                    scrapeResult.title(),
                    scrapeResult.metaDescription(),
                    scrapeResult.firstH1(),
                    scrapeResult.totalLinks(),
                    ScrapeExecutionStatus.SUCCESS,
                    null,
                    elapsedMillis(startedAt)
            );

            ScrapeExecution savedExecution = scrapeExecutionRepository.save(execution);
            task.markExecutedAt(lastExecutedAt(savedExecution));

            return toResponse(savedExecution);
        } catch (Exception exception) {
            ScrapeExecution execution = new ScrapeExecution(
                    task,
                    null,
                    null,
                    null,
                    0,
                    ScrapeExecutionStatus.FAILED,
                    safeErrorMessage(exception),
                    elapsedMillis(startedAt)
            );

            return toResponse(scrapeExecutionRepository.save(execution));
        }
    }

    @Transactional(readOnly = true)
    public List<ScrapeExecutionResponseDto> getExecutionHistory(Long taskId) {
        ScrapingTask task = getTaskOrThrow(taskId);
        return scrapeExecutionRepository.findByTaskOrderByExecutedAtDescIdDesc(task)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ScrapingTask getTaskOrThrow(Long taskId) {
        return scrapingTaskRepository.findById(taskId)
                .orElseThrow(() -> new ScrapingTaskNotFoundException(taskId));
    }

    private void ensureTaskIsActive(ScrapingTask task) {
        if (!task.isActive()) {
            throw new InactiveScrapingTaskException();
        }
    }

    private long elapsedMillis(long startedAt) {
        return Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
    }

    private Instant lastExecutedAt(ScrapeExecution execution) {
        return execution.getExecutedAt() == null ? Instant.now() : execution.getExecutedAt();
    }

    private String safeErrorMessage(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            message = exception.getClass().getSimpleName();
        }

        return message.length() > ERROR_MESSAGE_MAX_LENGTH
                ? message.substring(0, ERROR_MESSAGE_MAX_LENGTH)
                : message;
    }

    private ScrapeExecutionResponseDto toResponse(ScrapeExecution execution) {
        return new ScrapeExecutionResponseDto(
                execution.getId(),
                execution.getTask().getId(),
                execution.getTitle(),
                execution.getMetaDescription(),
                execution.getFirstH1(),
                execution.getTotalLinks(),
                execution.getStatus(),
                execution.getErrorMessage(),
                execution.getExecutedAt(),
                execution.getResponseTimeMillis()
        );
    }
}
