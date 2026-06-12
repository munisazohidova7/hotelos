package com.hotelos.housekeeping.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelos.housekeeping.model.CleaningTask;
import com.hotelos.housekeeping.repository.CleaningTaskRepository;
import com.hotelos.shared.event.HotelEvent;
import com.hotelos.shared.event.RabbitMQConstants;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class HousekeepingService {

    private final CleaningTaskRepository taskRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public HousekeepingService(CleaningTaskRepository taskRepository,
                                RabbitTemplate rabbitTemplate,
                                ObjectMapper objectMapper) {
        this.taskRepository = taskRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public CleaningTask addToCleaningQueue(int roomNumber, int floor) {
        CleaningTask task = new CleaningTask(roomNumber, floor);
        return taskRepository.save(task);
    }

    public Map<String, Object> startCleaning(int roomNumber, String cleanerName) {
        Map<String, Object> result = new LinkedHashMap<>();
        Optional<CleaningTask> taskOpt = taskRepository
                .findByRoomNumberAndStatusNot(roomNumber, CleaningTask.TaskStatus.DONE);
        if (taskOpt.isEmpty()) {
            result.put("success", false);
            result.put("error", "Room " + roomNumber + " is not in the cleaning queue.");
            return result;
        }
        CleaningTask task = taskOpt.get();
        task.setStatus(CleaningTask.TaskStatus.IN_PROGRESS);
        task.setAssignedTo(cleanerName);
        taskRepository.save(task);
        publishRoomStatusChange(roomNumber, "CLEANING", cleanerName);
        result.put("success", true);
        result.put("message", "Room " + roomNumber + " cleaning started by " + cleanerName);
        return result;
    }

    public Map<String, Object> markClean(int roomNumber) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (roomNumber < 100 || roomNumber > 999) {
            result.put("success", false);
            result.put("error", "Invalid room number: " + roomNumber);
            return result;
        }
        Optional<CleaningTask> taskOpt = taskRepository
                .findByRoomNumberAndStatusNot(roomNumber, CleaningTask.TaskStatus.DONE);
        if (taskOpt.isEmpty()) {
            result.put("success", false);
            result.put("error", "Room " + roomNumber + " has no active cleaning task.");
            return result;
        }
        CleaningTask task = taskOpt.get();
        task.setStatus(CleaningTask.TaskStatus.DONE);
        task.setCompletedAt(LocalDateTime.now());
        taskRepository.save(task);
        publishRoomStatusChange(roomNumber, "CLEAN", null);
        result.put("success", true);
        result.put("roomNumber", roomNumber);
        result.put("status", "CLEAN");
        result.put("message", "Room " + roomNumber + " is now clean and available.");
        return result;
    }

    public List<CleaningTask> getCleaningQueue() {
        return taskRepository.findByStatusNot(CleaningTask.TaskStatus.DONE);
    }

    public List<CleaningTask> getAllTasks() {
        return taskRepository.findAll();
    }

    private void publishRoomStatusChange(int roomNumber, String newStatus, String assignedTo) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("roomNumber", roomNumber);
            payload.put("newStatus", newStatus);
            payload.put("assignedTo", assignedTo != null ? assignedTo : "");
            payload.put("timestamp", LocalDateTime.now().toString());
            String json = objectMapper.writeValueAsString(payload);
            HotelEvent event = new HotelEvent(HotelEvent.EventType.ROOM_STATUS_CHANGED,
                    "housekeeping-service", json);
            rabbitTemplate.convertAndSend(RabbitMQConstants.EXCHANGE,
                    RabbitMQConstants.ROOM_STATUS_CHANGED, event);
        } catch (Exception e) {
            System.err.println("[Housekeeping] Failed to publish: " + e.getMessage());
        }
    }
}
