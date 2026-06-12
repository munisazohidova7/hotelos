package com.hotelos.housekeeping.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cleaning_tasks")
public class CleaningTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_number", nullable = false)
    private int roomNumber;

    @Column(name = "floor")
    private int floor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TaskStatus status = TaskStatus.PENDING;

    @Column(name = "assigned_to")
    private String assignedTo;

    @Column(name = "added_at")
    private LocalDateTime addedAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public enum TaskStatus { PENDING, IN_PROGRESS, DONE }

    public CleaningTask() {}
    public CleaningTask(int roomNumber, int floor) {
        this.roomNumber = roomNumber;
        this.floor = floor;
        this.status = TaskStatus.PENDING;
        this.addedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public int getRoomNumber() { return roomNumber; }
    public void setRoomNumber(int r) { this.roomNumber = r; }
    public int getFloor() { return floor; }
    public void setFloor(int f) { this.floor = f; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus s) { this.status = s; }
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String a) { this.assignedTo = a; }
    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime a) { this.addedAt = a; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime c) { this.completedAt = c; }
}
