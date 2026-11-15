package com.uptimepulse.controller;

import com.uptimepulse.dto.DashboardStatsResponse;
import com.uptimepulse.model.Incident;
import com.uptimepulse.repository.IncidentRepository;
import com.uptimepulse.service.MonitorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final MonitorService monitorService;
    private final IncidentRepository incidentRepository;

    public DashboardController(MonitorService monitorService, IncidentRepository incidentRepository) {
        this.monitorService = monitorService;
        this.incidentRepository = incidentRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getStats() {
        return ResponseEntity.ok(monitorService.getDashboardStats());
    }

    @GetMapping("/incidents")
    public ResponseEntity<List<Incident>> getIncidents() {
        return ResponseEntity.ok(incidentRepository.findTop10ByOrderByStartedAtDesc());
    }
}
