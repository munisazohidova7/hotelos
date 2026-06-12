package com.hotelos.reception.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * HotelOS - Guest JPA Entity.
 * Mapped to 'guests' table in PostgreSQL.
 */
@Entity
@Table(name = "guests")
public class GuestEntity {

    @Id
    @Column(name = "guest_id")
    private String guestId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "assigned_room_number")
    private int assignedRoomNumber;

    @Column(name = "requested_room_type")
    @Enumerated(EnumType.STRING)
    private RoomEntity.RoomType requestedRoomType;

    @Column(name = "preferred_floor")
    private int preferredFloor;

    @Column(name = "wants_near_lift")
    private boolean wantsNearLift;

    @Column(name = "wants_near_stairs")
    private boolean wantsNearStairs;

    @Column(name = "check_in_date")
    private LocalDate checkInDate;

    @Column(name = "check_out_date")
    private LocalDate checkOutDate;

    @Column(name = "room_charges")
    private double roomCharges;

    @Column(name = "service_charges")
    private double serviceCharges;

    @Column(name = "discount")
    private double discount;

    @Column(name = "actual_check_out")
    private LocalDateTime actualCheckOut;

    @Column(name = "total_bill")
    private double totalBill;

    public GuestEntity() {}

    // Getters & Setters
    public String getGuestId() { return guestId; }
    public void setGuestId(String guestId) { this.guestId = guestId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public int getAssignedRoomNumber() { return assignedRoomNumber; }
    public void setAssignedRoomNumber(int assignedRoomNumber) { this.assignedRoomNumber = assignedRoomNumber; }
    public RoomEntity.RoomType getRequestedRoomType() { return requestedRoomType; }
    public void setRequestedRoomType(RoomEntity.RoomType requestedRoomType) { this.requestedRoomType = requestedRoomType; }
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
    public double getServiceCharges() { return serviceCharges; }
    public void setServiceCharges(double serviceCharges) { this.serviceCharges = serviceCharges; }
    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }
    public LocalDateTime getActualCheckOut() { return actualCheckOut; }
    public void setActualCheckOut(LocalDateTime actualCheckOut) { this.actualCheckOut = actualCheckOut; }
    public double getTotalBill() { return totalBill; }
    public void setTotalBill(double totalBill) { this.totalBill = totalBill; }
}
