package com.uptimepulse.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ping_logs", indexes = {
    @Index(name = "idx_ping_monitor_time", columnList = "monitor_id, timestamp")
})
public class PingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monitor_id", nullable = false)
    @JsonIgnore
    private Monitor monitor;

    private int statusCode;

    private long responseTimeMs;

    private boolean isSuccessful;

    @Column(length = 1000)
    private String errorMessage;

    @Column(nullable = false)
    private Instant timestamp = Instant.now();

    public PingLog() {}

    public PingLog(Monitor monitor, int statusCode, long responseTimeMs, boolean isSuccessful, String errorMessage) {
        this.monitor = monitor;
        this.statusCode = statusCode;
        this.responseTimeMs = responseTimeMs;
        this.isSuccessful = isSuccessful;
        this.errorMessage = errorMessage;
        this.timestamp = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Monitor getMonitor() { return monitor; }
    public void setMonitor(Monitor monitor) { this.monitor = monitor; }

    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }

    public long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(long responseTimeMs) { this.responseTimeMs = responseTimeMs; }

    public boolean isSuccessful() { return isSuccessful; }
    public void setSuccessful(boolean successful) { isSuccessful = successful; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
