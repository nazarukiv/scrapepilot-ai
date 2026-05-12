package com.nazarukiv.scrapepilotai.service;

import com.nazarukiv.scrapepilotai.dto.CreateScrapingTaskRequestDto;
import com.nazarukiv.scrapepilotai.dto.ScrapingTaskResponseDto;
import com.nazarukiv.scrapepilotai.entity.ScrapingTask;
import com.nazarukiv.scrapepilotai.repository.ScrapingTaskRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScrapingTaskService {

    private final ScrapingTaskRepository scrapingTaskRepository;

    public ScrapingTaskService(ScrapingTaskRepository scrapingTaskRepository) {
        this.scrapingTaskRepository = scrapingTaskRepository;
    }

    @Transactional
    public ScrapingTaskResponseDto createTask(CreateScrapingTaskRequestDto request) {
        ScrapingTask task = new ScrapingTask(
                request.name().trim(),
                request.url().trim()
        );

        return toResponse(scrapingTaskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public List<ScrapingTaskResponseDto> getAllTasks() {
        return scrapingTaskRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ScrapingTaskResponseDto toResponse(ScrapingTask task) {
        return new ScrapingTaskResponseDto(
                task.getId(),
                task.getName(),
                task.getUrl(),
                task.isActive(),
                task.getCreatedAt()
        );
    }
}
