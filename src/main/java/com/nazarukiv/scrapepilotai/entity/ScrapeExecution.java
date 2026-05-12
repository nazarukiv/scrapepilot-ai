package com.nazarukiv.scrapepilotai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(
        name = "scrape_executions",
        indexes = @Index(name = "idx_scrape_executions_task_executed_at", columnList = "task_id, executed_at")
)
public class ScrapeExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private ScrapingTask task;

    @Column(columnDefinition = "text")
    private String title;

    @Column(name = "meta_description", columnDefinition = "text")
    private String metaDescription;

    @Column(name = "first_h1", columnDefinition = "text")
    private String firstH1;

    @Column(name = "total_links", nullable = false)
    private int totalLinks;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScrapeExecutionStatus status;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @Column(name = "executed_at", nullable = false, updatable = false)
    private Instant executedAt;

    @Column(name = "response_time_millis", nullable = false)
    private long responseTimeMillis;

    protected ScrapeExecution() {
    }

    public ScrapeExecution(
            ScrapingTask task,
            String title,
            String metaDescription,
            String firstH1,
            int totalLinks,
            ScrapeExecutionStatus status,
            String errorMessage,
            long responseTimeMillis
    ) {
        this.task = task;
        this.title = title;
        this.metaDescription = metaDescription;
        this.firstH1 = firstH1;
        this.totalLinks = totalLinks;
        this.status = status;
        this.errorMessage = errorMessage;
        this.responseTimeMillis = responseTimeMillis;
    }

    @PrePersist
    void prePersist() {
        if (executedAt == null) {
            executedAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public ScrapingTask getTask() {
        return task;
    }

    public String getTitle() {
        return title;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public String getFirstH1() {
        return firstH1;
    }

    public int getTotalLinks() {
        return totalLinks;
    }

    public ScrapeExecutionStatus getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }

    public long getResponseTimeMillis() {
        return responseTimeMillis;
    }
}
