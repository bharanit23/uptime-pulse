package com.uptimepulse.config;

import com.uptimepulse.dto.MonitorRequest;
import com.uptimepulse.repository.MonitorRepository;
import com.uptimepulse.service.MonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final MonitorRepository monitorRepository;
    private final MonitorService monitorService;

    public DataInitializer(MonitorRepository monitorRepository, MonitorService monitorService) {
        this.monitorRepository = monitorRepository;
        this.monitorService = monitorService;
    }

    @Override
    public void run(String... args) {
        if (monitorRepository.count() == 0) {
            log.info("No monitors found. Seeding initial production service monitors...");

            try {
                monitorService.createMonitor(new MonitorRequest(
                        "Google Search Gateway",
                        "https://www.google.com",
                        "GET",
                        60,
                        200
                ));

                monitorService.createMonitor(new MonitorRequest(
                        "Cloudflare Network",
                        "https://cloudflare.com",
                        "GET",
                        45,
                        200
                ));

                monitorService.createMonitor(new MonitorRequest(
                        "Wikipedia REST Services",
                        "https://en.wikipedia.org/api/rest_v1/",
                        "GET",
                        30,
                        200
                ));

                log.info("Initial monitors seeded successfully.");
            } catch (Exception e) {
                log.warn("Failed to seed demo monitors: {}", e.getMessage());
            }
        }
    }
}
