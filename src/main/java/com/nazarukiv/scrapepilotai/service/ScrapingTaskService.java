package com.nazarukiv.scrapepilotai.service;

import com.nazarukiv.scrapepilotai.dto.CreateScrapingTaskRequestDto;
import com.nazarukiv.scrapepilotai.dto.ScrapingTaskResponseDto;
import com.nazarukiv.scrapepilotai.dto.UpdateScrapingTaskRequestDto;
import com.nazarukiv.scrapepilotai.entity.ScrapingTask;
import com.nazarukiv.scrapepilotai.exception.ScrapingTaskNotFoundException;
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
                request.url().trim(),
                request.executionIntervalSeconds()
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

    @Transactional
    public ScrapingTaskResponseDto updateTask(Long taskId, UpdateScrapingTaskRequestDto request) {
        ScrapingTask task = getTaskOrThrow(taskId);
        task.update(
                request.name().trim(),
                request.url().trim(),
                request.active(),
                request.executionIntervalSeconds()
        );

        return toResponse(task);
    }

    @Transactional(readOnly = true)
    public List<ScrapingTask> getActiveTasksForScheduling() {
        return scrapingTaskRepository.findByActiveTrue();
    }

    @Transactional
    public void deleteTask(Long taskId) {
        ScrapingTask task = getTaskOrThrow(taskId);
        scrapingTaskRepository.delete(task);
    }

    private ScrapingTask getTaskOrThrow(Long taskId) {
        return scrapingTaskRepository.findById(taskId)
                .orElseThrow(() -> new ScrapingTaskNotFoundException(taskId));
    }

    private ScrapingTaskResponseDto toResponse(ScrapingTask task) {
        return new ScrapingTaskResponseDto(
                task.getId(),
                task.getName(),
                task.getUrl(),
                task.isActive(),
                task.getExecutionIntervalSeconds(),
                task.getLastExecutedAt(),
                task.getCreatedAt()
        );
    }
}
