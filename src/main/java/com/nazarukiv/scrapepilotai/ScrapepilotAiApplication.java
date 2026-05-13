package com.nazarukiv.scrapepilotai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ScrapepilotAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScrapepilotAiApplication.class, args);
    }

}
