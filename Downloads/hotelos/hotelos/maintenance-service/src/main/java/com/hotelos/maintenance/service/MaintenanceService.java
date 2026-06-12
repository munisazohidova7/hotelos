package com.hotelos.maintenance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelos.maintenance.model.MaintenanceIssue;
import com.hotelos.maintenance.repository.MaintenanceIssueRepository;
import com.hotelos.shared.event.HotelEvent;
import com.hotelos.shared.event.RabbitMQConstants;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

@Service
public class MaintenanceService {

    private final MaintenanceIssueRepository issueRepository;
    private final PriorityBlockingQueue<MaintenanceIssue> priorityQueue = new PriorityBlockingQueue<>();
    private final List<String> availableTechnicians = new ArrayList<>(
            Arrays.asList("Technician-A", "Technician-B", "Technician-C"));
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public MaintenanceService(MaintenanceIssueRepository issueRepository,
                               RabbitTemplate rabbitTemplate,
                               ObjectMapper objectMapper) {
        this.issueRepository = issueRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> reportIssue(int roomNumber, String description, String urgencyStr) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (roomNumber < 100 || roomNumber > 999) {
            result.put("success", false);
            result.put("error", "Invalid room number: " + roomNumber);
            return result;
        }
        if (description == null || description.isBlank()) {
            result.put("success", false);
            result.put("error", "Description is required.");
            return result;
        }
        MaintenanceIssue.Urgency urgency;
        try {
            urgency = MaintenanceIssue.Urgency.valueOf(urgencyStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("error", "Invalid urgency. Use: CRITICAL, HIGH, NORMAL, LOW");
            return result;
        }

        String issueId = "ISS-" + System.currentTimeMillis();
        MaintenanceIssue issue = new MaintenanceIssue(issueId, roomNumber, description, urgency);
        issueRepository.save(issue);
        priorityQueue.add(issue);

        String tech = tryAssignTechnician(issue);
        if (tech != null) issueRepository.save(issue);

        publishIssueStatusChange(issue);
        result.put("success", true);
        result.put("issueId", issueId);
        result.put("roomNumber", roomNumber);
        result.put("urgency", urgency);
        result.put("status", issue.getStatus());
        result.put("assignedTo", tech != null ? tech : "Queued");
        return result;
    }

    public Map<String, Object> resolveIssue(String issueId) {
        Map<String, Object> result = new LinkedHashMap<>();
        Optional<MaintenanceIssue> issueOpt = issueRepository.findById(issueId);
        if (issueOpt.isEmpty()) {
            result.put("success", false);
            result.put("error", "Issue not found: " + issueId);
            return result;
        }
        MaintenanceIssue issue = issueOpt.get();
        if (issue.getStatus() == MaintenanceIssue.IssueStatus.RESOLVED) {
            result.put("success", false);
            result.put("error", "Issue already resolved.");
            return result;
        }
        if (issue.getAssignedTechnician() != null) {
            availableTechnicians.add(issue.getAssignedTechnician());
        }
        issue.setStatus(MaintenanceIssue.IssueStatus.RESOLVED);
        issue.setResolvedAt(LocalDateTime.now());
        issueRepository.save(issue);
        priorityQueue.remove(issue);
        publishIssueStatusChange(issue);
        result.put("success", true);
        result.put("issueId", issueId);
        result.put("message", "Issue resolved.");
        return result;
    }

    public List<MaintenanceIssue> getOpenIssues() {
        return issueRepository.findByStatusNot(MaintenanceIssue.IssueStatus.RESOLVED)
                .stream().sorted().toList();
    }

    public List<MaintenanceIssue> getAllIssues() {
        return issueRepository.findAll();
    }

    private synchronized String tryAssignTechnician(MaintenanceIssue issue) {
        if (availableTechnicians.isEmpty()) return null;
        String tech = availableTechnicians.remove(0);
        issue.setAssignedTechnician(tech);
        issue.setStatus(MaintenanceIssue.IssueStatus.ASSIGNED);
        return tech;
    }

    private void publishIssueStatusChange(MaintenanceIssue issue) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("issueId", issue.getIssueId());
            payload.put("roomNumber", issue.getRoomNumber());
            payload.put("description", issue.getDescription());
            payload.put("urgency", issue.getUrgency().toString());
            payload.put("status", issue.getStatus().toString());
            payload.put("assignedTo", issue.getAssignedTechnician() != null
                    ? issue.getAssignedTechnician() : "Unassigned");
            String json = objectMapper.writeValueAsString(payload);
            HotelEvent event = new HotelEvent(HotelEvent.EventType.ISSUE_STATUS_CHANGED,
                    "maintenance-service", json);
            rabbitTemplate.convertAndSend(RabbitMQConstants.EXCHANGE,
                    RabbitMQConstants.ISSUE_STATUS_CHANGED, event);
        } catch (Exception e) {
            System.err.println("[Maintenance] Failed to publish: " + e.getMessage());
        }
    }
}
