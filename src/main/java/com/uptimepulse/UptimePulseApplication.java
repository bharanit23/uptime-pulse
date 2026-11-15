package com.uptimepulse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UptimePulseApplication {

    public static void main(String[] args) {
        SpringApplication.run(UptimePulseApplication.class, args);
    }
}
