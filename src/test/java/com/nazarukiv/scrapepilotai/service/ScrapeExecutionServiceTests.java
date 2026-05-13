package com.nazarukiv.scrapepilotai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nazarukiv.scrapepilotai.dto.ScrapeExecutionResponseDto;
import com.nazarukiv.scrapepilotai.dto.ScrapeTitleResponseDto;
import com.nazarukiv.scrapepilotai.entity.ScrapeExecution;
import com.nazarukiv.scrapepilotai.entity.ScrapeExecutionStatus;
import com.nazarukiv.scrapepilotai.entity.ScrapingTask;
import com.nazarukiv.scrapepilotai.exception.InactiveScrapingTaskException;
import com.nazarukiv.scrapepilotai.repository.ScrapeExecutionRepository;
import com.nazarukiv.scrapepilotai.repository.ScrapingTaskRepository;
import com.nazarukiv.scrapepilotai.scraper.JsoupScraperService;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScrapeExecutionServiceTests {

    @Mock
    private ScrapingTaskRepository scrapingTaskRepository;

    @Mock
    private ScrapeExecutionRepository scrapeExecutionRepository;

    @Mock
    private JsoupScraperService jsoupScraperService;

    @InjectMocks
    private ScrapeExecutionService scrapeExecutionService;

    @Test
    void executeTaskRunsActiveTask() throws IOException {
        ScrapingTask task = new ScrapingTask("Active task", "https://example.com");
        when(scrapingTaskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(jsoupScraperService.scrapeTitle(task.getUrl())).thenReturn(new ScrapeTitleResponseDto(
                task.getUrl(),
                "Example Domain",
                null,
                "Example Domain",
                1
        ));
        when(scrapeExecutionRepository.save(any(ScrapeExecution.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ScrapeExecutionResponseDto response = scrapeExecutionService.executeTask(1L);

        assertEquals(ScrapeExecutionStatus.SUCCESS, response.status());
        assertNotNull(task.getLastExecutedAt());
        verify(jsoupScraperService).scrapeTitle(task.getUrl());
        verify(scrapeExecutionRepository).save(any(ScrapeExecution.class));
    }

    @Test
    void executeTaskBlocksInactiveTaskBeforeScraping() throws IOException {
        ScrapingTask task = new ScrapingTask("Inactive task", "https://example.com");
        task.update(task.getName(), task.getUrl(), false, null);
        when(scrapingTaskRepository.findById(1L)).thenReturn(Optional.of(task));

        InactiveScrapingTaskException exception = assertThrows(
                InactiveScrapingTaskException.class,
                () -> scrapeExecutionService.executeTask(1L)
        );

        assertEquals("Task is inactive and cannot be executed", exception.getMessage());
        verify(jsoupScraperService, never()).scrapeTitle(anyString());
        verify(scrapeExecutionRepository, never()).save(any(ScrapeExecution.class));
    }
}
