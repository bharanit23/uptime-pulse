package com.uptimepulse.repository;

import com.uptimepulse.model.Monitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonitorRepository extends JpaRepository<Monitor, Long> {
    List<Monitor> findByActiveTrue();
    long countByStatus(com.uptimepulse.model.MonitorStatus status);
}
