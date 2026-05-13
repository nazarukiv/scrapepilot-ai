package com.nazarukiv.scrapepilotai.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "scraping_tasks")
public class ScrapingTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 2048)
    private String url;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "execution_interval_seconds")
    private Integer executionIntervalSeconds;

    @Column(name = "last_executed_at")
    private Instant lastExecutedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "task", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ScrapeExecution> executions = new ArrayList<>();

    protected ScrapingTask() {
    }

    public ScrapingTask(String name, String url) {
        this(name, url, null);
    }

    public ScrapingTask(String name, String url, Integer executionIntervalSeconds) {
        this.name = name;
        this.url = url;
        this.active = true;
        this.executionIntervalSeconds = executionIntervalSeconds;
    }

    public void update(String name, String url, boolean active, Integer executionIntervalSeconds) {
        this.name = name;
        this.url = url;
        this.active = active;
        this.executionIntervalSeconds = executionIntervalSeconds;
    }

    public void markExecutedAt(Instant executedAt) {
        this.lastExecutedAt = executedAt;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public boolean isActive() {
        return active;
    }

    public Integer getExecutionIntervalSeconds() {
        return executionIntervalSeconds;
    }

    public Instant getLastExecutedAt() {
        return lastExecutedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
