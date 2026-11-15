package com.uptimepulse.service;

import com.uptimepulse.model.Monitor;
import com.uptimepulse.model.MonitorStatus;
import com.uptimepulse.repository.MonitorRepository;
import com.uptimepulse.repository.PingLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class PingScheduler {

    private static final Logger log = LoggerFactory.getLogger(PingScheduler.class);

    private final MonitorRepository monitorRepository;
    private final PingLogRepository pingLogRepository;
    private final HealthCheckService healthCheckService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public PingScheduler(MonitorRepository monitorRepository,
                         PingLogRepository pingLogRepository,
                         HealthCheckService healthCheckService) {
        this.monitorRepository = monitorRepository;
        this.pingLogRepository = pingLogRepository;
        this.healthCheckService = healthCheckService;
    }

    /**
     * Checks all monitors every 10 seconds.
     * Only pings a monitor if its individual interval has elapsed.
     */
    @Scheduled(fixedDelay = 10000)
    public void scheduleHealthChecks() {
        List<Monitor> activeMonitors = monitorRepository.findByActiveTrue();
        Instant now = Instant.now();

        for (Monitor monitor : activeMonitors) {
            if (monitor.getStatus() == MonitorStatus.PAUSED) {
                continue;
            }

            boolean shouldPing = false;
            if (monitor.getLastCheckedAt() == null) {
                shouldPing = true;
            } else {
                long elapsedSeconds = Duration.between(monitor.getLastCheckedAt(), now).getSeconds();
                if (elapsedSeconds >= monitor.getIntervalSeconds()) {
                    shouldPing = true;
                }
            }

            if (shouldPing) {
                CompletableFuture.runAsync(() -> {
                    try {
                        healthCheckService.executePing(monitor);
                    } catch (Exception e) {
                        log.error("Error executing ping for monitor {}: {}", monitor.getId(), e.getMessage());
                    }
                }, executorService);
            }
        }
    }

    /**
     * Nightly maintenance: prune logs older than 14 days to keep database compact.
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupOldLogs() {
        Instant cutoff = Instant.now().minus(Duration.ofDays(14));
        try {
            pingLogRepository.deleteByTimestampBefore(cutoff);
            log.info("Pruned ping logs older than {}", cutoff);
        } catch (Exception e) {
            log.warn("Log cleanup skipped or failed: {}", e.getMessage());
        }
    }
}
