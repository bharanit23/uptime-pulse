package com.uptimepulse.repository;

import com.uptimepulse.model.Incident;
import com.uptimepulse.model.Monitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findTop10ByOrderByStartedAtDesc();
    Optional<Incident> findFirstByMonitorAndStatusOrderByStartedAtDesc(Monitor monitor, String status);
    List<Incident> findByMonitorOrderByStartedAtDesc(Monitor monitor);
}
