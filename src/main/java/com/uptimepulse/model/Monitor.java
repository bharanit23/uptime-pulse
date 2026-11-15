package com.uptimepulse.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "monitors")
public class Monitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 1024)
    private String url;

    @Column(nullable = false, length = 10)
    private String httpMethod = "GET";

    @Column(nullable = false)
    private int intervalSeconds = 60;

    @Column(nullable = false)
    private int expectedStatusCode = 200;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MonitorStatus status = MonitorStatus.PENDING;

    private Long lastLatencyMs;

    private Instant lastCheckedAt;

    private Double uptimePercentage = 100.0;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Monitor() {}

    public Monitor(String name, String url, String httpMethod, int intervalSeconds, int expectedStatusCode) {
        this.name = name;
        this.url = url;
        this.httpMethod = httpMethod != null ? httpMethod.toUpperCase() : "GET";
        this.intervalSeconds = intervalSeconds > 0 ? intervalSeconds : 60;
        this.expectedStatusCode = expectedStatusCode > 0 ? expectedStatusCode : 200;
        this.status = MonitorStatus.PENDING;
        this.active = true;
        this.createdAt = Instant.now();
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

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
