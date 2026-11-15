package com.uptimepulse.dto;

import com.uptimepulse.model.Monitor;
import com.uptimepulse.model.MonitorStatus;
import com.uptimepulse.model.PingLog;

import java.time.Instant;
import java.util.List;

public class MonitorResponse {

    private Long id;
    private String name;
    private String url;
    private String httpMethod;
    private int intervalSeconds;
    private int expectedStatusCode;
    private MonitorStatus status;
    private Long lastLatencyMs;
    private Instant lastCheckedAt;
    private Double uptimePercentage;
    private Double avgResponseTimeMs;
    private long totalPings;
    private long successfulPings;
    private boolean active;
    private Instant createdAt;
    private List<PingLog> recentLogs;

    public MonitorResponse() {}

    public MonitorResponse(Monitor monitor, Double avgResponseTimeMs, long totalPings, long successfulPings, List<PingLog> recentLogs) {
        this.id = monitor.getId();
        this.name = monitor.getName();
        this.url = monitor.getUrl();
        this.httpMethod = monitor.getHttpMethod();
        this.intervalSeconds = monitor.getIntervalSeconds();
        this.expectedStatusCode = monitor.getExpectedStatusCode();
        this.status = monitor.getStatus();
        this.lastLatencyMs = monitor.getLastLatencyMs();
        this.lastCheckedAt = monitor.getLastCheckedAt();
        this.uptimePercentage = monitor.getUptimePercentage();
        this.active = monitor.isActive();
        this.createdAt = monitor.getCreatedAt();
        this.avgResponseTimeMs = avgResponseTimeMs;
        this.totalPings = totalPings;
        this.successfulPings = successfulPings;
        this.recentLogs = recentLogs;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }

    public int getIntervalSeconds() { return intervalSeconds; }
    public void setIntervalSeconds(int intervalSeconds) { this.intervalSeconds = intervalSeconds; }

    public int getExpectedStatusCode() { return expectedStatusCode; }
    public void setExpectedStatusCode(int expectedStatusCode) { this.expectedStatusCode = expectedStatusCode; }

    public MonitorStatus getStatus() { return status; }
    public void setStatus(MonitorStatus status) { this.status = status; }

    public Long getLastLatencyMs() { return lastLatencyMs; }
    public void setLastLatencyMs(Long lastLatencyMs) { this.lastLatencyMs = lastLatencyMs; }

    public Instant getLastCheckedAt() { return lastCheckedAt; }
    public void setLastCheckedAt(Instant lastCheckedAt) { this.lastCheckedAt = lastCheckedAt; }

    public Double getUptimePercentage() { return uptimePercentage; }
    public void setUptimePercentage(Double uptimePercentage) { this.uptimePercentage = uptimePercentage; }

    public Double getAvgResponseTimeMs() { return avgResponseTimeMs; }
    public void setAvgResponseTimeMs(Double avgResponseTimeMs) { this.avgResponseTimeMs = avgResponseTimeMs; }

    public long getTotalPings() { return totalPings; }
    public void setTotalPings(long totalPings) { this.totalPings = totalPings; }

    public long getSuccessfulPings() { return successfulPings; }
    public void setSuccessfulPings(long successfulPings) { this.successfulPings = successfulPings; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public List<PingLog> getRecentLogs() { return recentLogs; }
    public void setRecentLogs(List<PingLog> recentLogs) { this.recentLogs = recentLogs; }
}
