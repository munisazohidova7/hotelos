package com.hotelos.reception.controller;

import com.hotelos.reception.model.GuestEntity;
import com.hotelos.reception.model.RoomEntity;
import com.hotelos.reception.service.ReceptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reception")
@CrossOrigin(origins = "*")
public class ReceptionController {

    private final ReceptionService receptionService;

    public ReceptionController(ReceptionService receptionService) {
        this.receptionService = receptionService;
    }

    @PostMapping("/checkin")
    public ResponseEntity<Map<String, Object>> checkIn(@RequestBody GuestEntity guest) {
        Map<String, Object> result = receptionService.checkIn(guest);
        return (Boolean) result.get("success")
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/checkout/{roomNumber}")
    public ResponseEntity<Map<String, Object>> checkOut(@PathVariable int roomNumber) {
        Map<String, Object> result = receptionService.checkOut(roomNumber);
        return (Boolean) result.get("success")
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<RoomEntity>> getAllRooms() {
        return ResponseEntity.ok(receptionService.getAllRooms());
    }

    @GetMapping("/guests")
    public ResponseEntity<List<GuestEntity>> getAllGuests() {
        return ResponseEntity.ok(receptionService.getAllGuests());
    }
}
