package com.uptimepulse.dto;

public class DashboardStatsResponse {

    private long totalMonitors;
    private long operationalCount;
    private long degradedCount;
    private long downCount;
    private long pausedCount;
    private double overallUptimePercentage;
    private double globalAvgLatencyMs;
    private long activeIncidents;

    public DashboardStatsResponse() {}

    public DashboardStatsResponse(long totalMonitors, long operationalCount, long degradedCount, long downCount,
                                  long pausedCount, double overallUptimePercentage, double globalAvgLatencyMs, long activeIncidents) {
        this.totalMonitors = totalMonitors;
        this.operationalCount = operationalCount;
        this.degradedCount = degradedCount;
        this.downCount = downCount;
        this.pausedCount = pausedCount;
        this.overallUptimePercentage = overallUptimePercentage;
        this.globalAvgLatencyMs = globalAvgLatencyMs;
        this.activeIncidents = activeIncidents;
    }

    public long getTotalMonitors() { return totalMonitors; }
    public void setTotalMonitors(long totalMonitors) { this.totalMonitors = totalMonitors; }

    public long getOperationalCount() { return operationalCount; }
    public void setOperationalCount(long operationalCount) { this.operationalCount = operationalCount; }

    public long getDegradedCount() { return degradedCount; }
    public void setDegradedCount(long degradedCount) { this.degradedCount = degradedCount; }

    public long getDownCount() { return downCount; }
    public void setDownCount(long downCount) { this.downCount = downCount; }

    public long getPausedCount() { return pausedCount; }
    public void setPausedCount(long pausedCount) { this.pausedCount = pausedCount; }

    public double getOverallUptimePercentage() { return overallUptimePercentage; }
    public void setOverallUptimePercentage(double overallUptimePercentage) { this.overallUptimePercentage = overallUptimePercentage; }

    public double getGlobalAvgLatencyMs() { return globalAvgLatencyMs; }
    public void setGlobalAvgLatencyMs(double globalAvgLatencyMs) { this.globalAvgLatencyMs = globalAvgLatencyMs; }

    public long getActiveIncidents() { return activeIncidents; }
    public void setActiveIncidents(long activeIncidents) { this.activeIncidents = activeIncidents; }
}
