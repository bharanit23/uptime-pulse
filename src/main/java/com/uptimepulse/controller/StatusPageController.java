package com.uptimepulse.controller;

import com.uptimepulse.dto.MonitorResponse;
import com.uptimepulse.model.Incident;
import com.uptimepulse.repository.IncidentRepository;
import com.uptimepulse.service.MonitorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/status")
@CrossOrigin(origins = "*")
public class StatusPageController {

    private final MonitorService monitorService;
    private final IncidentRepository incidentRepository;

    public StatusPageController(MonitorService monitorService, IncidentRepository incidentRepository) {
        this.monitorService = monitorService;
        this.incidentRepository = incidentRepository;
    }

    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> getPublicStatus() {
        Map<String, Object> response = new HashMap<>();
        List<MonitorResponse> monitors = monitorService.getAllMonitors();
        List<Incident> incidents = incidentRepository.findTop10ByOrderByStartedAtDesc();

        boolean allOperational = monitors.stream()
                .allMatch(m -> m.getStatus() == com.uptimepulse.model.MonitorStatus.OPERATIONAL);

        response.put("systemStatus", allOperational ? "ALL_SYSTEMS_OPERATIONAL" : "SOME_SYSTEMS_DEGRADED");
        response.put("monitors", monitors);
        response.put("recentIncidents", incidents);
        response.put("timestamp", java.time.Instant.now());

        return ResponseEntity.ok(response);
    }
}
