package com.nazarukiv.scrapepilotai.dto;

public record ScrapeTitleResponseDto(
        String url,
        String title,
        String metaDescription,
        String firstH1,
        int totalLinks
) {
}
