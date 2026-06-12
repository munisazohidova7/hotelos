package com.hotelos.roomservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelos.roomservice.model.RoomServiceOrder;
import com.hotelos.roomservice.repository.RoomServiceOrderRepository;
import com.hotelos.shared.event.HotelEvent;
import com.hotelos.shared.event.RabbitMQConstants;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class RoomOrderService {

    private final RoomServiceOrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public RoomOrderService(RoomServiceOrderRepository orderRepository,
                             RabbitTemplate rabbitTemplate,
                             ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> placeOrder(int roomNumber, List<Map<String, Object>> items) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (roomNumber < 100 || roomNumber > 999) {
            result.put("success", false);
            result.put("error", "Invalid room number: " + roomNumber);
            return result;
        }
        if (items == null || items.isEmpty()) {
            result.put("success", false);
            result.put("error", "Order must contain at least one item.");
            return result;
        }

        String orderId = "ORD-" + System.currentTimeMillis();
        RoomServiceOrder order = new RoomServiceOrder(orderId, roomNumber);

        double total = 0;
        List<String> itemNames = new ArrayList<>();
        for (Map<String, Object> item : items) {
            String name = (String) item.getOrDefault("name", "");
            int qty = ((Number) item.getOrDefault("quantity", 1)).intValue();
            double price = ((Number) item.getOrDefault("price", 0.0)).doubleValue();
            if (name.isBlank() || qty <= 0 || price < 0) {
                result.put("success", false);
                result.put("error", "Invalid item: " + name);
                return result;
            }
            total += qty * price;
            itemNames.add(qty + "x " + name);
        }

        order.setTotalAmount(total);
        order.setItemsSummary(String.join(", ", itemNames));
        orderRepository.save(order);

        publishOrderStatusChange(order, itemNames);

        result.put("success", true);
        result.put("orderId", orderId);
        result.put("roomNumber", roomNumber);
        result.put("status", order.getStatus());
        result.put("totalAmount", total);
        return result;
    }

    public Map<String, Object> advanceOrderStatus(String orderId) {
        Map<String, Object> result = new LinkedHashMap<>();
        Optional<RoomServiceOrder> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            result.put("success", false);
            result.put("error", "Order not found: " + orderId);
            return result;
        }
        RoomServiceOrder order = orderOpt.get();
        switch (order.getStatus()) {
            case RECEIVED   -> order.setStatus(RoomServiceOrder.OrderStatus.PREPARING);
            case PREPARING  -> order.setStatus(RoomServiceOrder.OrderStatus.DELIVERING);
            case DELIVERING -> {
                order.setStatus(RoomServiceOrder.OrderStatus.DELIVERED);
                order.setDeliveredAt(LocalDateTime.now());
            }
            case DELIVERED -> {
                result.put("success", false);
                result.put("error", "Order already delivered.");
                return result;
            }
        }
        orderRepository.save(order);
        publishOrderStatusChange(order, List.of(order.getItemsSummary()));
        result.put("success", true);
        result.put("orderId", orderId);
        result.put("newStatus", order.getStatus());
        return result;
    }

    public List<RoomServiceOrder> getActiveOrders() {
        return orderRepository.findByStatusNot(RoomServiceOrder.OrderStatus.DELIVERED);
    }

    public List<RoomServiceOrder> getOrdersByRoom(int roomNumber) {
        return orderRepository.findByRoomNumber(roomNumber);
    }

    public List<RoomServiceOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    private void publishOrderStatusChange(RoomServiceOrder order, List<String> items) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("orderId", order.getOrderId());
            payload.put("roomNumber", order.getRoomNumber());
            payload.put("status", order.getStatus().toString());
            payload.put("totalAmount", order.getTotalAmount());
            payload.put("items", items);
            String json = objectMapper.writeValueAsString(payload);
            HotelEvent event = new HotelEvent(HotelEvent.EventType.ORDER_STATUS_CHANGED,
                    "room-service", json);
            rabbitTemplate.convertAndSend(RabbitMQConstants.EXCHANGE,
                    RabbitMQConstants.ORDER_STATUS_CHANGED, event);
        } catch (Exception e) {
            System.err.println("[RoomService] Failed to publish: " + e.getMessage());
        }
    }
}
