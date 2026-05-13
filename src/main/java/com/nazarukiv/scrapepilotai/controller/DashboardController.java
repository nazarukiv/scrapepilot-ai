package com.nazarukiv.scrapepilotai.controller;

import com.nazarukiv.scrapepilotai.dto.ScrapeExecutionResponseDto;
import com.nazarukiv.scrapepilotai.entity.ScrapeExecutionStatus;
import com.nazarukiv.scrapepilotai.exception.InactiveScrapingTaskException;
import com.nazarukiv.scrapepilotai.exception.ScrapingTaskNotFoundException;
import com.nazarukiv.scrapepilotai.service.DashboardService;
import com.nazarukiv.scrapepilotai.service.ScrapeExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DashboardController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardController.class);

    private final DashboardService dashboardService;
    private final ScrapeExecutionService scrapeExecutionService;

    public DashboardController(
            DashboardService dashboardService,
            ScrapeExecutionService scrapeExecutionService
    ) {
        this.dashboardService = dashboardService;
        this.scrapeExecutionService = scrapeExecutionService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) Long taskId, Model model) {
        model.addAttribute("dashboard", dashboardService.getDashboard(taskId));
        return "dashboard";
    }

    @PostMapping("/dashboard/tasks/{id}/execute")
    public String executeTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ScrapeExecutionResponseDto execution = scrapeExecutionService.executeTask(id);
            if (execution.status() == ScrapeExecutionStatus.SUCCESS) {
                redirectAttributes.addFlashAttribute(
                        "dashboardNotice",
                        "Execution " + execution.id() + " completed"
                );
            } else {
                redirectAttributes.addFlashAttribute(
                        "dashboardError",
                        "Execution " + execution.id() + " failed and was recorded"
                );
            }
            return redirectToTaskHistory(id);
        } catch (InactiveScrapingTaskException exception) {
            redirectAttributes.addFlashAttribute("dashboardError", "Task is inactive and cannot be executed");
        } catch (ScrapingTaskNotFoundException exception) {
            redirectAttributes.addFlashAttribute("dashboardError", "Task was not found");
        } catch (Exception exception) {
            LOGGER.warn("Dashboard task execution failed for task {}", id, exception);
            redirectAttributes.addFlashAttribute("dashboardError", "Task execution failed");
        }

        return "redirect:/dashboard";
    }

    private String redirectToTaskHistory(Long taskId) {
        return "redirect:/dashboard?taskId=" + taskId + "#execution-history";
    }
}
