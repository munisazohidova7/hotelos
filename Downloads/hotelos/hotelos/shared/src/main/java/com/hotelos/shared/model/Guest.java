package com.hotelos.shared.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * HotelOS - Guest entity.
 * Stores guest information and billing details.
 */
public class Guest {

    private String guestId;
    private String fullName;          // No passport/sensitive data in WebSocket broadcasts
    private String email;
    private int assignedRoomNumber;
    private Room.RoomType requestedRoomType;
    private int preferredFloor;       // 0 = no preference
    private boolean wantsNearLift;
    private boolean wantsNearStairs;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate;

    // Billing
    private double roomCharges;
    private List<ServiceCharge> serviceCharges = new ArrayList<>();
    private double discount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualCheckOut;

    // --- Constructors ---
    public Guest() {}

    public Guest(String guestId, String fullName, String email,
                 Room.RoomType requestedRoomType, LocalDate checkInDate,
                 LocalDate checkOutDate) {
        this.guestId = guestId;
        this.fullName = fullName;
        this.email = email;
        this.requestedRoomType = requestedRoomType;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.preferredFloor = 0;
    }

    /**
     * Calculate total bill: room charges + service charges - discount.
     * Handles edge cases: early checkout uses actual nights stayed.
     */
    public double calculateTotalBill() {
        long nights = java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (nights <= 0) nights = 1; // minimum 1 night

        double total = roomCharges + getServiceChargesTotal() - discount;
        return Math.max(total, 0.0); // never negative
    }

    public double getServiceChargesTotal() {
        return serviceCharges.stream()
                .mapToDouble(ServiceCharge::getAmount)
                .sum();
    }

    public void addServiceCharge(String description, double amount) {
        serviceCharges.add(new ServiceCharge(description, amount));
    }

    // --- Getters & Setters ---
    public String getGuestId() { return guestId; }
    public void setGuestId(String guestId) { this.guestId = guestId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getAssignedRoomNumber() { return assignedRoomNumber; }
    public void setAssignedRoomNumber(int assignedRoomNumber) { this.assignedRoomNumber = assignedRoomNumber; }

    public Room.RoomType getRequestedRoomType() { return requestedRoomType; }
    public void setRequestedRoomType(Room.RoomType requestedRoomType) { this.requestedRoomType = requestedRoomType; }

    public int getPreferredFloor() { return preferredFloor; }
    public void setPreferredFloor(int preferredFloor) { this.preferredFloor = preferredFloor; }

    public boolean isWantsNearLift() { return wantsNearLift; }
    public void setWantsNearLift(boolean wantsNearLift) { this.wantsNearLift = wantsNearLift; }

    public boolean isWantsNearStairs() { return wantsNearStairs; }
    public void setWantsNearStairs(boolean wantsNearStairs) { this.wantsNearStairs = wantsNearStairs; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public double getRoomCharges() { return roomCharges; }
    public void setRoomCharges(double roomCharges) { this.roomCharges = roomCharges; }

    public List<ServiceCharge> getServiceCharges() { return serviceCharges; }
    public void setServiceCharges(List<ServiceCharge> serviceCharges) { this.serviceCharges = serviceCharges; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public LocalDateTime getActualCheckOut() { return actualCheckOut; }
    public void setActualCheckOut(LocalDateTime actualCheckOut) { this.actualCheckOut = actualCheckOut; }

    // --- Inner class for service charges ---
    public static class ServiceCharge {
        private String description;
        private double amount;

        public ServiceCharge() {}
        public ServiceCharge(String description, double amount) {
            this.description = description;
            this.amount = amount;
        }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
    }
}
