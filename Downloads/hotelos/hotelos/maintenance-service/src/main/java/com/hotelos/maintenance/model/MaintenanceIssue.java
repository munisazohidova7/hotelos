package com.hotelos.maintenance.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_issues")
public class MaintenanceIssue implements Comparable<MaintenanceIssue> {

    @Id
    @Column(name = "issue_id")
    private String issueId;

    @Column(name = "room_number", nullable = false)
    private int roomNumber;

    @Column(name = "description", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "urgency", nullable = false)
    private Urgency urgency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private IssueStatus status = IssueStatus.OPEN;

    @Column(name = "assigned_technician")
    private String assignedTechnician;

    @Column(name = "reported_at")
    private LocalDateTime reportedAt = LocalDateTime.now();

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public enum Urgency {
        CRITICAL(1), HIGH(2), NORMAL(3), LOW(4);
        private final int level;
        Urgency(int level) { this.level = level; }
        public int getLevel() { return level; }
    }

    public enum IssueStatus { OPEN, ASSIGNED, IN_PROGRESS, RESOLVED }

    public MaintenanceIssue() {}
    public MaintenanceIssue(String issueId, int roomNumber, String description, Urgency urgency) {
        this.issueId = issueId;
        this.roomNumber = roomNumber;
        this.description = description;
        this.urgency = urgency;
        this.status = IssueStatus.OPEN;
        this.reportedAt = LocalDateTime.now();
    }

    @Override
    public int compareTo(MaintenanceIssue other) {
        int c = this.urgency.getLevel() - other.urgency.getLevel();
        if (c != 0) return c;
        return this.reportedAt.compareTo(other.reportedAt);
    }

    public String getIssueId() { return issueId; }
    public void setIssueId(String i) { this.issueId = i; }
    public int getRoomNumber() { return roomNumber; }
    public void setRoomNumber(int r) { this.roomNumber = r; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public Urgency getUrgency() { return urgency; }
    public void setUrgency(Urgency u) { this.urgency = u; }
    public IssueStatus getStatus() { return status; }
    public void setStatus(IssueStatus s) { this.status = s; }
    public String getAssignedTechnician() { return assignedTechnician; }
    public void setAssignedTechnician(String a) { this.assignedTechnician = a; }
    public LocalDateTime getReportedAt() { return reportedAt; }
    public void setReportedAt(LocalDateTime r) { this.reportedAt = r; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime r) { this.resolvedAt = r; }
}
