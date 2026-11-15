-- ========================================================
-- UptimePulse Database Schema (PostgreSQL / Supabase)
-- ========================================================

-- 1. Monitors Table
CREATE TABLE IF NOT EXISTS monitors (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    url VARCHAR(1024) NOT NULL,
    http_method VARCHAR(10) NOT NULL DEFAULT 'GET',
    interval_seconds INT NOT NULL DEFAULT 60,
    expected_status_code INT NOT NULL DEFAULT 200,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    last_latency_ms BIGINT,
    last_checked_at TIMESTAMP WITH TIME ZONE,
    uptime_percentage DOUBLE PRECISION DEFAULT 100.0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 2. Ping Logs Table
CREATE TABLE IF NOT EXISTS ping_logs (
    id BIGSERIAL PRIMARY KEY,
    monitor_id BIGINT NOT NULL REFERENCES monitors(id) ON DELETE CASCADE,
    status_code INT NOT NULL,
    response_time_ms BIGINT NOT NULL,
    is_successful BOOLEAN NOT NULL,
    error_message VARCHAR(1000),
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Index for rapid time-series lookups per monitor
CREATE INDEX IF NOT EXISTS idx_ping_monitor_time ON ping_logs(monitor_id, timestamp DESC);

-- 3. Incidents Table (Outage Tracking & MTTR)
CREATE TABLE IF NOT EXISTS incidents (
    id BIGSERIAL PRIMARY KEY,
    monitor_id BIGINT NOT NULL REFERENCES monitors(id) ON DELETE CASCADE,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    resolved_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    root_cause VARCHAR(1000)
);

CREATE INDEX IF NOT EXISTS idx_incident_monitor ON incidents(monitor_id, started_at DESC);

-- ========================================================
-- Helpful Analytical Queries
-- ========================================================

-- Query: 24-Hour Uptime SLA per Monitor
-- SELECT 
--     m.name,
--     COUNT(p.id) AS total_checks,
--     ROUND(100.0 * COUNT(CASE WHEN p.is_successful THEN 1 END) / NULLIF(COUNT(p.id), 0), 2) AS uptime_pct,
--     ROUND(AVG(CASE WHEN p.is_successful THEN p.response_time_ms END), 1) AS avg_latency_ms
-- FROM monitors m
-- LEFT JOIN ping_logs p ON m.id = p.monitor_id AND p.timestamp >= NOW() - INTERVAL '24 HOURS'
-- GROUP BY m.id, m.name;
