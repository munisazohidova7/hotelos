package com.hotelos.housekeeping.controller;

import com.hotelos.housekeeping.model.CleaningTask;
import com.hotelos.housekeeping.service.HousekeepingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * HotelOS - Housekeeping REST API
 *
 * Endpoints:
 *   GET  /api/housekeeping/queue           → View cleaning queue
 *   POST /api/housekeeping/start/{room}    → Start cleaning a room
 *   POST /api/housekeeping/clean/{room}    → Mark room as clean (TS-03)
 */
@RestController
@RequestMapping("/api/housekeeping")
@CrossOrigin(origins = "*")
public class HousekeepingController {

    private final HousekeepingService housekeepingService;

    public HousekeepingController(HousekeepingService housekeepingService) {
        this.housekeepingService = housekeepingService;
    }

    @GetMapping("/queue")
    public ResponseEntity<List<CleaningTask>> getCleaningQueue() {
        return ResponseEntity.ok(housekeepingService.getCleaningQueue());
    }

    @PostMapping("/start/{roomNumber}")
    public ResponseEntity<Map<String, Object>> startCleaning(
            @PathVariable int roomNumber,
            @RequestParam(defaultValue = "Staff") String cleanerName) {
        Map<String, Object> result = housekeepingService.startCleaning(roomNumber, cleanerName);
        return (Boolean) result.get("success")
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }

    /**
     * Mark room as clean — triggers WebSocket dashboard update (TS-03).
     */
    @PostMapping("/clean/{roomNumber}")
    public ResponseEntity<Map<String, Object>> markClean(@PathVariable int roomNumber) {
        Map<String, Object> result = housekeepingService.markClean(roomNumber);
        return (Boolean) result.get("success")
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }
}
