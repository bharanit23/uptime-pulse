package com.uptimepulse.repository;

import com.uptimepulse.model.Monitor;
import com.uptimepulse.model.PingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface PingLogRepository extends JpaRepository<PingLog, Long> {

    List<PingLog> findTop50ByMonitorOrderByTimestampDesc(Monitor monitor);

    List<PingLog> findByMonitorAndTimestampAfterOrderByTimestampAsc(Monitor monitor, Instant timestamp);

    long countByMonitor(Monitor monitor);

    long countByMonitorAndIsSuccessfulTrue(Monitor monitor);

    @Query("SELECT AVG(p.responseTimeMs) FROM PingLog p WHERE p.monitor = :monitor AND p.isSuccessful = true")
    Double findAvgResponseTimeByMonitor(@Param("monitor") Monitor monitor);

    @Query("SELECT AVG(p.responseTimeMs) FROM PingLog p WHERE p.isSuccessful = true")
    Double findGlobalAvgResponseTime();

    void deleteByTimestampBefore(Instant timestamp);
}
