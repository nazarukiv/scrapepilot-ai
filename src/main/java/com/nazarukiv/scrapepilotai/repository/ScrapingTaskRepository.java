package com.nazarukiv.scrapepilotai.repository;

import com.nazarukiv.scrapepilotai.entity.ScrapingTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScrapingTaskRepository extends JpaRepository<ScrapingTask, Long> {
}
