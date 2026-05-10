package com.nazarukiv.scrapepilotai.controller;

import com.nazarukiv.scrapepilotai.dto.ScrapeRequestDto;
import com.nazarukiv.scrapepilotai.dto.ScrapeTitleResponseDto;
import com.nazarukiv.scrapepilotai.scraper.JsoupScraperService;
import jakarta.validation.Valid;
import java.io.IOException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scrape")
public class ScrapeController {

    private final JsoupScraperService jsoupScraperService;

    public ScrapeController(JsoupScraperService jsoupScraperService) {
        this.jsoupScraperService = jsoupScraperService;
    }

    @PostMapping("/title")
    public ScrapeTitleResponseDto scrapeTitle(@Valid @RequestBody ScrapeRequestDto request) throws IOException {
        return jsoupScraperService.scrapeTitle(request);
    }
}
