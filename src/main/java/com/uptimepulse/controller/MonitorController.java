package com.uptimepulse.controller;

import com.uptimepulse.dto.MonitorRequest;
import com.uptimepulse.dto.MonitorResponse;
import com.uptimepulse.model.PingLog;
import com.uptimepulse.service.MonitorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitors")
@CrossOrigin(origins = "*")
public class MonitorController {

    private final MonitorService monitorService;

    public MonitorController(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @GetMapping
    public ResponseEntity<List<MonitorResponse>> getAllMonitors() {
        return ResponseEntity.ok(monitorService.getAllMonitors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MonitorResponse> getMonitorById(@PathVariable Long id) {
        return ResponseEntity.ok(monitorService.getMonitorById(id));
    }

    @PostMapping
    public ResponseEntity<MonitorResponse> createMonitor(@Valid @RequestBody MonitorRequest request) {
        MonitorResponse created = monitorService.createMonitor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MonitorResponse> updateMonitor(@PathVariable Long id, @Valid @RequestBody MonitorRequest request) {
        return ResponseEntity.ok(monitorService.updateMonitor(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMonitor(@PathVariable Long id) {
        monitorService.deleteMonitor(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/ping")
    public ResponseEntity<PingLog> triggerManualPing(@PathVariable Long id) {
        return ResponseEntity.ok(monitorService.triggerManualPing(id));
    }

    @PostMapping("/{id}/toggle")
    public ResponseEntity<MonitorResponse> togglePause(@PathVariable Long id) {
        return ResponseEntity.ok(monitorService.togglePause(id));
    }
}
