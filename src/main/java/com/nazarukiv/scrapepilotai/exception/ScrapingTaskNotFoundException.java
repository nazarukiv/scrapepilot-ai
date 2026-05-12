package com.nazarukiv.scrapepilotai.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ScrapingTaskNotFoundException extends RuntimeException {

    public ScrapingTaskNotFoundException(Long taskId) {
        super("Scraping task not found: " + taskId);
    }
}
