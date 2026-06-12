package com.hotelos.maintenance.controller;

import com.hotelos.maintenance.model.MaintenanceIssue;
import com.hotelos.maintenance.service.MaintenanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/maintenance")
@CrossOrigin(origins = "*")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;
    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @PostMapping("/report")
    public ResponseEntity<Map<String, Object>> reportIssue(
            @RequestParam int roomNumber,
            @RequestParam String description,
            @RequestParam(defaultValue = "NORMAL") String urgency) {
        Map<String, Object> result = maintenanceService.reportIssue(roomNumber, description, urgency);
        return (Boolean) result.get("success")
                ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/resolve/{issueId}")
    public ResponseEntity<Map<String, Object>> resolve(@PathVariable String issueId) {
        Map<String, Object> result = maintenanceService.resolveIssue(issueId);
        return (Boolean) result.get("success")
                ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/issues")
    public ResponseEntity<List<MaintenanceIssue>> getOpenIssues() {
        return ResponseEntity.ok(maintenanceService.getOpenIssues());
    }
}
