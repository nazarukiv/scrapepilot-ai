package com.nazarukiv.scrapepilotai.repository;

import com.nazarukiv.scrapepilotai.entity.ScrapeExecution;
import com.nazarukiv.scrapepilotai.entity.ScrapingTask;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScrapeExecutionRepository extends JpaRepository<ScrapeExecution, Long> {

    List<ScrapeExecution> findByTaskOrderByExecutedAtDescIdDesc(ScrapingTask task);

    @EntityGraph(attributePaths = "task")
    List<ScrapeExecution> findAllByOrderByExecutedAtDescIdDesc();
}
