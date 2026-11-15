package com.uptimepulse.service;

import com.uptimepulse.model.Incident;
import com.uptimepulse.model.Monitor;
import com.uptimepulse.model.MonitorStatus;
import com.uptimepulse.model.PingLog;
import com.uptimepulse.repository.IncidentRepository;
import com.uptimepulse.repository.MonitorRepository;
import com.uptimepulse.repository.PingLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class HealthCheckService {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckService.class);

    private final MonitorRepository monitorRepository;
    private final PingLogRepository pingLogRepository;
    private final IncidentRepository incidentRepository;
    private final HttpClient httpClient;

    public HealthCheckService(MonitorRepository monitorRepository,
                              PingLogRepository pingLogRepository,
                              IncidentRepository incidentRepository) {
        this.monitorRepository = monitorRepository;
        this.pingLogRepository = pingLogRepository;
        this.incidentRepository = incidentRepository;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Transactional
    public PingLog executePing(Monitor monitor) {
        if (!monitor.isActive() || monitor.getStatus() == MonitorStatus.PAUSED) {
            return null;
        }

        long startTime = System.currentTimeMillis();
        int statusCode = 0;
        boolean isSuccess = false;
        String errorMessage = null;

        try {
            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(monitor.getUrl()))
                    .timeout(Duration.ofSeconds(8))
                    .header("User-Agent", "UptimePulse/1.0 (HealthCheck; +https://github.com/bharanit23/uptime-pulse)");

            if ("HEAD".equalsIgnoreCase(monitor.getHttpMethod())) {
                reqBuilder.method("HEAD", HttpRequest.BodyPublishers.noBody());
            } else if ("POST".equalsIgnoreCase(monitor.getHttpMethod())) {
                reqBuilder.POST(HttpRequest.BodyPublishers.noBody());
            } else {
                reqBuilder.GET();
            }

            HttpResponse<Void> response = httpClient.send(reqBuilder.build(), HttpResponse.BodyHandlers.discarding());
            statusCode = response.statusCode();
            long latency = System.currentTimeMillis() - startTime;

            isSuccess = (statusCode == monitor.getExpectedStatusCode()) ||
                        (monitor.getExpectedStatusCode() == 200 && statusCode >= 200 && statusCode < 300);

            if (isSuccess) {
                if (latency > 1500) {
                    monitor.setStatus(MonitorStatus.DEGRADED);
                } else {
                    monitor.setStatus(MonitorStatus.OPERATIONAL);
                }
                resolveActiveIncidentIfExists(monitor);
            } else {
                monitor.setStatus(MonitorStatus.DOWN);
                errorMessage = "Expected HTTP " + monitor.getExpectedStatusCode() + ", got " + statusCode;
                recordIncident(monitor, errorMessage);
            }

            monitor.setLastLatencyMs(latency);

        } catch (Exception ex) {
            long latency = System.currentTimeMillis() - startTime;
            isSuccess = false;
            statusCode = 0;
            errorMessage = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
            monitor.setStatus(MonitorStatus.DOWN);
            monitor.setLastLatencyMs(latency);
            recordIncident(monitor, errorMessage);
            log.warn("Health check failed for {} ({}): {}", monitor.getName(), monitor.getUrl(), errorMessage);
        }

        long totalLatency = System.currentTimeMillis() - startTime;
        monitor.setLastCheckedAt(Instant.now());

        // Create and save log
        PingLog pingLog = new PingLog(monitor, statusCode, totalLatency, isSuccess, errorMessage);
        pingLogRepository.save(pingLog);

        // Recalculate uptime percentage
        long totalPings = pingLogRepository.countByMonitor(monitor);
        long successfulPings = pingLogRepository.countByMonitorAndIsSuccessfulTrue(monitor);
        if (totalPings > 0) {
            double uptime = ((double) successfulPings / totalPings) * 100.0;
            monitor.setUptimePercentage(Math.round(uptime * 100.0) / 100.0);
        }

        monitorRepository.save(monitor);
        return pingLog;
    }

    private void recordIncident(Monitor monitor, String cause) {
        Optional<Incident> openIncident = incidentRepository.findFirstByMonitorAndStatusOrderByStartedAtDesc(monitor, "OPEN");
        if (openIncident.isEmpty()) {
            Incident incident = new Incident(monitor, cause);
            incidentRepository.save(incident);
            log.info("Opened new incident for monitor '{}': {}", monitor.getName(), cause);
        }
    }

    private void resolveActiveIncidentIfExists(Monitor monitor) {
        Optional<Incident> openIncident = incidentRepository.findFirstByMonitorAndStatusOrderByStartedAtDesc(monitor, "OPEN");
        if (openIncident.isPresent()) {
            Incident incident = openIncident.get();
            incident.setStatus("RESOLVED");
            incident.setResolvedAt(Instant.now());
            incidentRepository.save(incident);
            log.info("Resolved incident #{} for monitor '{}'", incident.getId(), monitor.getName());
        }
    }
}
