package com.nazarukiv.scrapepilotai.controller;

import com.nazarukiv.scrapepilotai.dto.CreateScrapingTaskRequestDto;
import com.nazarukiv.scrapepilotai.dto.ScrapeExecutionResponseDto;
import com.nazarukiv.scrapepilotai.dto.ScrapingTaskResponseDto;
import com.nazarukiv.scrapepilotai.service.ScrapeExecutionService;
import com.nazarukiv.scrapepilotai.service.ScrapingTaskService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class ScrapingTaskController {

    private final ScrapingTaskService scrapingTaskService;
    private final ScrapeExecutionService scrapeExecutionService;

    public ScrapingTaskController(
            ScrapingTaskService scrapingTaskService,
            ScrapeExecutionService scrapeExecutionService
    ) {
        this.scrapingTaskService = scrapingTaskService;
        this.scrapeExecutionService = scrapeExecutionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScrapingTaskResponseDto createTask(@Valid @RequestBody CreateScrapingTaskRequestDto request) {
        return scrapingTaskService.createTask(request);
    }

    @GetMapping
    public List<ScrapingTaskResponseDto> getAllTasks() {
        return scrapingTaskService.getAllTasks();
    }

    @PostMapping("/{id}/execute")
    public ScrapeExecutionResponseDto executeTask(@PathVariable Long id) {
        return scrapeExecutionService.executeTask(id);
    }

    @GetMapping("/{id}/executions")
    public List<ScrapeExecutionResponseDto> getExecutionHistory(@PathVariable Long id) {
        return scrapeExecutionService.getExecutionHistory(id);
    }
}
