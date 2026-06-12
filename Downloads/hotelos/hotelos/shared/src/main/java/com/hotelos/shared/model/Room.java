package com.hotelos.shared.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * HotelOS - Room entity shared across all microservices.
 * Represents a hotel room with its current state.
 */
public class Room {

    // --- Room Types ---
    public enum RoomType {
        SINGLE, DOUBLE, SUITE, ACCESSIBLE
    }

    // --- Room Status ---
    public enum RoomStatus {
        CLEAN,           // Ready for check-in
        DIRTY,           // Needs cleaning after checkout
        CLEANING,        // Currently being cleaned
        MAINTENANCE,     // Under maintenance
        OCCUPIED         // Guest checked in
    }

    private int roomNumber;
    private int floor;
    private RoomType type;
    private RoomStatus status;
    private double pricePerNight;
    private boolean nearLift;
    private boolean nearStairs;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastCleanedAt;

    private String currentGuestId;

    // --- Constructors ---
    public Room() {}

    public Room(int roomNumber, int floor, RoomType type, double pricePerNight,
                boolean nearLift, boolean nearStairs) {
        this.roomNumber = roomNumber;
        this.floor = floor;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.nearLift = nearLift;
        this.nearStairs = nearStairs;
        this.status = RoomStatus.CLEAN;
        this.lastCleanedAt = LocalDateTime.now();
    }

    // --- Getters & Setters ---
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

    @Override
    public String toString() {
        return "Room{" +
                "roomNumber=" + roomNumber +
                ", floor=" + floor +
                ", type=" + type +
                ", status=" + status +
                '}';
    }
}
