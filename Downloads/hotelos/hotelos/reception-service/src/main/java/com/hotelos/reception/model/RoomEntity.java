package com.hotelos.reception.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * HotelOS - Room JPA Entity.
 * Mapped to 'rooms' table in PostgreSQL.
 */
@Entity
@Table(name = "rooms")
public class RoomEntity {

    @Id
    @Column(name = "room_number")
    private int roomNumber;

    @Column(name = "floor", nullable = false)
    private int floor;

    @Column(name = "room_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private RoomType type;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private RoomStatus status = RoomStatus.CLEAN;

    @Column(name = "price_per_night", nullable = false)
    private double pricePerNight;

    @Column(name = "near_lift")
    private boolean nearLift;

    @Column(name = "near_stairs")
    private boolean nearStairs;

    @Column(name = "last_cleaned_at")
    private LocalDateTime lastCleanedAt;

    @Column(name = "current_guest_id")
    private String currentGuestId;

    public enum RoomType {
        SINGLE, DOUBLE, SUITE, ACCESSIBLE
    }

    public enum RoomStatus {
        CLEAN, DIRTY, CLEANING, MAINTENANCE, OCCUPIED
    }

    public RoomEntity() {}

    public RoomEntity(int roomNumber, int floor, RoomType type,
                      double pricePerNight, boolean nearLift, boolean nearStairs) {
        this.roomNumber = roomNumber;
        this.floor = floor;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.nearLift = nearLift;
        this.nearStairs = nearStairs;
        this.status = RoomStatus.CLEAN;
        this.lastCleanedAt = LocalDateTime.now();
    }

    // Getters & Setters
    public int getRoomNumber() { return roomNumber; }
    public void setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }
    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }
    public RoomType getType() { return type; }
    public void setType(RoomType type) { this.type = type; }
    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }
    public double getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(double pricePerNight) { this.pricePerNight = pricePerNight; }
    public boolean isNearLift() { return nearLift; }
    public void setNearLift(boolean nearLift) { this.nearLift = nearLift; }
    public boolean isNearStairs() { return nearStairs; }
    public void setNearStairs(boolean nearStairs) { this.nearStairs = nearStairs; }
    public LocalDateTime getLastCleanedAt() { return lastCleanedAt; }
    public void setLastCleanedAt(LocalDateTime lastCleanedAt) { this.lastCleanedAt = lastCleanedAt; }
    public String getCurrentGuestId() { return currentGuestId; }
    public void setCurrentGuestId(String currentGuestId) { this.currentGuestId = currentGuestId; }
}
