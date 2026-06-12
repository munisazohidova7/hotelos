package com.hotelos.roomservice.controller;

import com.hotelos.roomservice.model.RoomServiceOrder;
import com.hotelos.roomservice.service.RoomOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * HotelOS - Room Service REST API
 *
 * POST /api/roomservice/order              → Place food/beverage order
 * POST /api/roomservice/advance/{orderId}  → Advance order status
 * GET  /api/roomservice/orders             → View active orders
 * GET  /api/roomservice/room/{room}        → Orders for specific room
 */
@RestController
@RequestMapping("/api/roomservice")
@CrossOrigin(origins = "*")
public class RoomServiceController {

    private final RoomOrderService orderService;

    public RoomServiceController(RoomOrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/order")
    public ResponseEntity<Map<String, Object>> placeOrder(
            @RequestParam int roomNumber,
            @RequestBody List<Map<String, Object>> items) {
        Map<String, Object> result = orderService.placeOrder(roomNumber, items);
        return (Boolean) result.get("success")
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/advance/{orderId}")
    public ResponseEntity<Map<String, Object>> advance(@PathVariable String orderId) {
        Map<String, Object> result = orderService.advanceOrderStatus(orderId);
        return (Boolean) result.get("success")
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<RoomServiceOrder>> getActiveOrders() {
        return ResponseEntity.ok(orderService.getActiveOrders());
    }

    @GetMapping("/room/{roomNumber}")
    public ResponseEntity<List<RoomServiceOrder>> getOrdersByRoom(@PathVariable int roomNumber) {
        return ResponseEntity.ok(orderService.getOrdersByRoom(roomNumber));
    }
}
