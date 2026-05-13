package com.nazarukiv.scrapepilotai.exception;

public class InactiveScrapingTaskException extends RuntimeException {

    public InactiveScrapingTaskException() {
        super("Task is inactive and cannot be executed");
    }
}
