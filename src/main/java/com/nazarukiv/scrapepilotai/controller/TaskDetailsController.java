package com.nazarukiv.scrapepilotai.controller;

import com.nazarukiv.scrapepilotai.dto.ScrapeExecutionResponseDto;
import com.nazarukiv.scrapepilotai.entity.ScrapeExecutionStatus;
import com.nazarukiv.scrapepilotai.exception.InactiveScrapingTaskException;
import com.nazarukiv.scrapepilotai.exception.ScrapingTaskNotFoundException;
import com.nazarukiv.scrapepilotai.service.ScrapeExecutionService;
import com.nazarukiv.scrapepilotai.service.ScrapingTaskService;
import com.nazarukiv.scrapepilotai.service.TaskDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TaskDetailsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskDetailsController.class);

    private final TaskDetailsService taskDetailsService;
    private final ScrapeExecutionService scrapeExecutionService;
    private final ScrapingTaskService scrapingTaskService;

    public TaskDetailsController(
            TaskDetailsService taskDetailsService,
            ScrapeExecutionService scrapeExecutionService,
            ScrapingTaskService scrapingTaskService
    ) {
        this.taskDetailsService = taskDetailsService;
        this.scrapeExecutionService = scrapeExecutionService;
        this.scrapingTaskService = scrapingTaskService;
    }

    @GetMapping("/tasks/{id}")
    public String taskDetails(@PathVariable Long id, Model model) {
        model.addAttribute("taskDetails", taskDetailsService.getTaskDetails(id));
        return "task-details";
    }

    @PostMapping("/tasks/{id}/execute")
    public String executeTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ScrapeExecutionResponseDto execution = scrapeExecutionService.executeTask(id);
            if (execution.status() == ScrapeExecutionStatus.SUCCESS) {
                redirectAttributes.addFlashAttribute(
                        "taskNotice",
                        "Execution " + execution.id() + " completed"
                );
            } else {
                redirectAttributes.addFlashAttribute(
                        "taskError",
                        "Execution " + execution.id() + " failed and was recorded"
                );
            }
            return redirectToTaskHistory(id);
        } catch (InactiveScrapingTaskException exception) {
            redirectAttributes.addFlashAttribute("taskError", "Task is inactive and cannot be executed");
        } catch (ScrapingTaskNotFoundException exception) {
            redirectAttributes.addFlashAttribute("taskError", "Task was not found");
            return "redirect:/dashboard";
        } catch (Exception exception) {
            LOGGER.warn("Task details execution failed for task {}", id, exception);
            redirectAttributes.addFlashAttribute("taskError", "Task execution failed");
        }

        return "redirect:/tasks/" + id;
    }

    @PostMapping("/tasks/{id}/status")
    public String updateTaskStatus(
            @PathVariable Long id,
            @RequestParam boolean active,
            RedirectAttributes redirectAttributes
    ) {
        try {
            scrapingTaskService.updateTaskActive(id, active);
            redirectAttributes.addFlashAttribute(
                    "taskNotice",
                    active ? "Task activated" : "Task paused"
            );
            return "redirect:/tasks/" + id;
        } catch (ScrapingTaskNotFoundException exception) {
            redirectAttributes.addFlashAttribute("dashboardError", "Task was not found");
            return "redirect:/dashboard";
        }
    }

    @ExceptionHandler(ScrapingTaskNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleTaskNotFound(Model model) {
        model.addAttribute("message", "Task was not found");
        return "task-not-found";
    }

    private String redirectToTaskHistory(Long taskId) {
        return "redirect:/tasks/" + taskId + "#execution-history";
    }
}
