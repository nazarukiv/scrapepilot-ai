package com.nazarukiv.scrapepilotai.scraper;

import com.nazarukiv.scrapepilotai.dto.ScrapeRequestDto;
import com.nazarukiv.scrapepilotai.dto.ScrapeTitleResponseDto;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

@Service
public class JsoupScraperService {

    private static final int TIMEOUT_MILLIS = 10_000;
    private static final String USER_AGENT = "ScrapepilotAI/0.1";

    public ScrapeTitleResponseDto scrapeTitle(ScrapeRequestDto request) throws IOException {
        return scrapeTitle(request.url());
    }

    public ScrapeTitleResponseDto scrapeTitle(String url) throws IOException {
        Document document = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MILLIS)
                .get();

        return new ScrapeTitleResponseDto(
                url,
                document.title(),
                getMetaDescription(document),
                getFirstH1Text(document),
                document.select("a[href]").size()
        );
    }

    private String getMetaDescription(Document document) {
        Element metaDescription = document.selectFirst("meta[name=description]");
        return metaDescription == null ? null : metaDescription.attr("content").trim();
    }

    private String getFirstH1Text(Document document) {
        Element firstH1 = document.selectFirst("h1");
        return firstH1 == null ? null : firstH1.text().trim();
    }
}
