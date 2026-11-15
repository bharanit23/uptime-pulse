package com.uptimepulse.service;

import com.uptimepulse.dto.DashboardStatsResponse;
import com.uptimepulse.dto.MonitorRequest;
import com.uptimepulse.dto.MonitorResponse;
import com.uptimepulse.model.Monitor;
import com.uptimepulse.model.MonitorStatus;
import com.uptimepulse.model.PingLog;
import com.uptimepulse.repository.IncidentRepository;
import com.uptimepulse.repository.MonitorRepository;
import com.uptimepulse.repository.PingLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MonitorService {

    private final MonitorRepository monitorRepository;
    private final PingLogRepository pingLogRepository;
    private final IncidentRepository incidentRepository;
    private final HealthCheckService healthCheckService;

    public MonitorService(MonitorRepository monitorRepository,
                          PingLogRepository pingLogRepository,
                          IncidentRepository incidentRepository,
                          HealthCheckService healthCheckService) {
        this.monitorRepository = monitorRepository;
        this.pingLogRepository = pingLogRepository;
        this.incidentRepository = incidentRepository;
        this.healthCheckService = healthCheckService;
    }

    public List<MonitorResponse> getAllMonitors() {
        return monitorRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public MonitorResponse getMonitorById(Long id) {
        Monitor monitor = monitorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Monitor not found with id: " + id));
        return toResponse(monitor);
    }

    @Transactional
    public MonitorResponse createMonitor(MonitorRequest request) {
        Monitor monitor = new Monitor(
                request.getName(),
                request.getUrl().trim(),
                request.getHttpMethod(),
                request.getIntervalSeconds(),
                request.getExpectedStatusCode()
        );
        Monitor saved = monitorRepository.save(monitor);

        // Run initial health check immediately!
        try {
            healthCheckService.executePing(saved);
        } catch (Exception ignored) {}

        return toResponse(saved);
    }

    @Transactional
    public MonitorResponse updateMonitor(Long id, MonitorRequest request) {
        Monitor monitor = monitorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Monitor not found with id: " + id));

        monitor.setName(request.getName());
        monitor.setUrl(request.getUrl().trim());
        monitor.setHttpMethod(request.getHttpMethod());
        monitor.setIntervalSeconds(request.getIntervalSeconds());
        monitor.setExpectedStatusCode(request.getExpectedStatusCode());

        Monitor updated = monitorRepository.save(monitor);
        return toResponse(updated);
    }

    @Transactional
    public void deleteMonitor(Long id) {
        if (!monitorRepository.existsById(id)) {
            throw new IllegalArgumentException("Monitor not found with id: " + id);
        }
        monitorRepository.deleteById(id);
    }

    @Transactional
    public MonitorResponse togglePause(Long id) {
        Monitor monitor = monitorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Monitor not found with id: " + id));

        if (monitor.getStatus() == MonitorStatus.PAUSED) {
            monitor.setStatus(MonitorStatus.PENDING);
            monitor.setActive(true);
            monitorRepository.save(monitor);
            healthCheckService.executePing(monitor);
        } else {
            monitor.setStatus(MonitorStatus.PAUSED);
            monitorRepository.save(monitor);
        }

        return toResponse(monitor);
    }

    @Transactional
    public PingLog triggerManualPing(Long id) {
        Monitor monitor = monitorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Monitor not found with id: " + id));
        return healthCheckService.executePing(monitor);
    }

    public DashboardStatsResponse getDashboardStats() {
        long total = monitorRepository.count();
        long operational = monitorRepository.countByStatus(MonitorStatus.OPERATIONAL);
        long degraded = monitorRepository.countByStatus(MonitorStatus.DEGRADED);
        long down = monitorRepository.countByStatus(MonitorStatus.DOWN);
        long paused = monitorRepository.countByStatus(MonitorStatus.PAUSED);

        List<Monitor> all = monitorRepository.findAll();
        double overallUptime = 100.0;
        if (!all.isEmpty()) {
            double sumUptime = all.stream().mapToDouble(m -> m.getUptimePercentage() != null ? m.getUptimePercentage() : 100.0).sum();
            overallUptime = Math.round((sumUptime / all.size()) * 100.0) / 100.0;
        }

        Double avgLatency = pingLogRepository.findGlobalAvgResponseTime();
        double globalAvgLatency = avgLatency != null ? Math.round(avgLatency * 10.0) / 10.0 : 0.0;

        long activeIncidents = incidentRepository.findTop10ByOrderByStartedAtDesc().stream()
                .filter(i -> "OPEN".equals(i.getStatus()))
                .count();

        return new DashboardStatsResponse(total, operational, degraded, down, paused, overallUptime, globalAvgLatency, activeIncidents);
    }

    private MonitorResponse toResponse(Monitor monitor) {
        Double avgLatency = pingLogRepository.findAvgResponseTimeByMonitor(monitor);
        double avg = avgLatency != null ? Math.round(avgLatency * 10.0) / 10.0 : (monitor.getLastLatencyMs() != null ? monitor.getLastLatencyMs() : 0.0);
        long totalPings = pingLogRepository.countByMonitor(monitor);
        long successPings = pingLogRepository.countByMonitorAndIsSuccessfulTrue(monitor);
        List<PingLog> recentLogs = pingLogRepository.findTop50ByMonitorOrderByTimestampDesc(monitor);

        return new MonitorResponse(monitor, avg, totalPings, successPings, recentLogs);
    }
}
