package com.nazarukiv.scrapepilotai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "scrapepilot.scheduler.enabled=false")
class ScrapepilotAiApplicationTests {

    @Test
    void contextLoads() {
    }

}
