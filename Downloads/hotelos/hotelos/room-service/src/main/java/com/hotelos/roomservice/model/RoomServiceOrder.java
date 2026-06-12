package com.hotelos.roomservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "room_service_orders")
public class RoomServiceOrder {

    @Id
    @Column(name = "order_id")
    private String orderId;

    @Column(name = "room_number", nullable = false)
    private int roomNumber;

    @Column(name = "items_summary")
    private String itemsSummary;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.RECEIVED;

    @Column(name = "total_amount")
    private double totalAmount;

    @Column(name = "placed_at")
    private LocalDateTime placedAt = LocalDateTime.now();

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    public enum OrderStatus { RECEIVED, PREPARING, DELIVERING, DELIVERED }

    public RoomServiceOrder() {}
    public RoomServiceOrder(String orderId, int roomNumber) {
        this.orderId = orderId;
        this.roomNumber = roomNumber;
        this.status = OrderStatus.RECEIVED;
        this.placedAt = LocalDateTime.now();
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String o) { this.orderId = o; }
    public int getRoomNumber() { return roomNumber; }
    public void setRoomNumber(int r) { this.roomNumber = r; }
    public String getItemsSummary() { return itemsSummary; }
    public void setItemsSummary(String i) { this.itemsSummary = i; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus s) { this.status = s; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double t) { this.totalAmount = t; }
    public LocalDateTime getPlacedAt() { return placedAt; }
    public void setPlacedAt(LocalDateTime p) { this.placedAt = p; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime d) { this.deliveredAt = d; }
}
