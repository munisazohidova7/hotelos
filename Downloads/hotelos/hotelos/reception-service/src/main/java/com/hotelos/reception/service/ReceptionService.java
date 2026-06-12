package com.hotelos.reception.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelos.reception.model.GuestEntity;
import com.hotelos.reception.model.RoomEntity;
import com.hotelos.reception.repository.GuestJpaRepository;
import com.hotelos.reception.repository.RoomJpaRepository;
import com.hotelos.shared.event.HotelEvent;
import com.hotelos.shared.event.RabbitMQConstants;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * HotelOS - Reception Service with PostgreSQL persistence.
 */
@Service
public class ReceptionService {

    private final RoomJpaRepository roomRepository;
    private final GuestJpaRepository guestRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public ReceptionService(RoomJpaRepository roomRepository,
                            GuestJpaRepository guestRepository,
                            RabbitTemplate rabbitTemplate,
                            ObjectMapper objectMapper) {
        this.roomRepository = roomRepository;
        this.guestRepository = guestRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> checkIn(GuestEntity guest) {
        Map<String, Object> result = new LinkedHashMap<>();

        // Validation
        if (guest.getGuestId() == null || guest.getGuestId().isBlank()) {
            result.put("success", false);
            result.put("error", "Guest ID is required.");
            return result;
        }
        if (guest.getFullName() == null || guest.getFullName().isBlank()) {
            result.put("success", false);
            result.put("error", "Guest name is required.");
            return result;
        }
        if (guest.getFullName().contains("<") || guest.getFullName().contains(">")) {
            result.put("success", false);
            result.put("error", "Guest name contains invalid characters.");
            return result;
        }
        if (guestRepository.existsById(guest.getGuestId())) {
            result.put("success", false);
            result.put("error", "Guest " + guest.getGuestId() + " is already checked in.");
            return result;
        }
        if (!guest.getCheckOutDate().isAfter(guest.getCheckInDate())) {
            result.put("success", false);
            result.put("error", "Check-out date must be after check-in date.");
            return result;
        }

        // Find available room
        List<RoomEntity> eligible = roomRepository.findByTypeAndStatus(
                guest.getRequestedRoomType(), RoomEntity.RoomStatus.CLEAN);

        if (eligible.isEmpty()) {
            result.put("success", false);
            result.put("error", "No rooms available for type: " + guest.getRequestedRoomType());
            return result;
        }

        // Sort by longest-clean first
        eligible.sort(Comparator.comparing(RoomEntity::getLastCleanedAt));

        // Apply floor preference
        if (guest.getPreferredFloor() > 0) {
            int pf = guest.getPreferredFloor();
            List<RoomEntity> floorMatch = eligible.stream()
                    .filter(r -> r.getFloor() == pf).toList();
            if (!floorMatch.isEmpty()) eligible = new ArrayList<>(floorMatch);
        }

        // Apply proximity preference
        RoomEntity assigned = eligible.get(0);
        if (guest.isWantsNearLift()) {
            eligible.stream().filter(RoomEntity::isNearLift).findFirst()
                    .ifPresent(r -> { });
            assigned = eligible.stream().filter(RoomEntity::isNearLift)
                    .findFirst().orElse(eligible.get(0));
        } else if (guest.isWantsNearStairs()) {
            assigned = eligible.stream().filter(RoomEntity::isNearStairs)
                    .findFirst().orElse(eligible.get(0));
        }

        // Save to DB
        assigned.setStatus(RoomEntity.RoomStatus.OCCUPIED);
        assigned.setCurrentGuestId(guest.getGuestId());
        roomRepository.save(assigned);

        guest.setAssignedRoomNumber(assigned.getRoomNumber());
        guest.setRoomCharges(0);
        guestRepository.save(guest);

        // Publish event
        publishEvent(HotelEvent.EventType.GUEST_CHECKED_IN,
                Map.of("guestId", guest.getGuestId(),
                       "guestName", guest.getFullName(),
                       "roomNumber", assigned.getRoomNumber(),
                       "roomType", assigned.getType().toString(),
                       "floor", assigned.getFloor()));

        result.put("success", true);
        result.put("guestId", guest.getGuestId());
        result.put("assignedRoom", assigned.getRoomNumber());
        result.put("floor", assigned.getFloor());
        result.put("roomType", assigned.getType());
        result.put("pricePerNight", assigned.getPricePerNight());
        result.put("message", "Check-in successful. Welcome to GrandStay Hotel!");
        return result;
    }

    public Map<String, Object> checkOut(int roomNumber) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (roomNumber < 100 || roomNumber > 999) {
            result.put("success", false);
            result.put("error", "Invalid room number: " + roomNumber);
            return result;
        }

        Optional<RoomEntity> roomOpt = roomRepository.findById(roomNumber);
        if (roomOpt.isEmpty()) {
            result.put("success", false);
            result.put("error", "Room " + roomNumber + " not found.");
            return result;
        }

        Optional<GuestEntity> guestOpt = guestRepository
                .findByAssignedRoomNumberAndActualCheckOutIsNull(roomNumber);
        if (guestOpt.isEmpty()) {
            result.put("success", false);
            result.put("error", "No active guest in room " + roomNumber);
            return result;
        }

        RoomEntity room = roomOpt.get();
        GuestEntity guest = guestOpt.get();

        // Calculate bill
        long nights = ChronoUnit.DAYS.between(guest.getCheckInDate(), LocalDate.now());
        if (nights <= 0) nights = 1;
        double roomTotal = room.getPricePerNight() * nights;
        double total = Math.max(0.0, roomTotal + guest.getServiceCharges() - guest.getDiscount());

        guest.setRoomCharges(roomTotal);
        guest.setTotalBill(total);
        guest.setActualCheckOut(LocalDateTime.now());
        guestRepository.save(guest);

        room.setStatus(RoomEntity.RoomStatus.DIRTY);
        room.setCurrentGuestId(null);
        roomRepository.save(room);

        publishEvent(HotelEvent.EventType.ROOM_VACATED,
                Map.of("roomNumber", roomNumber, "floor", room.getFloor()));

        publishEvent(HotelEvent.EventType.GUEST_CHECKED_OUT,
                Map.of("guestId", guest.getGuestId(),
                       "guestName", guest.getFullName(),
                       "roomNumber", roomNumber,
                       "totalBill", total));

        result.put("success", true);
        result.put("guestName", guest.getFullName());
        result.put("roomNumber", roomNumber);
        result.put("roomCharges", roomTotal);
        result.put("serviceCharges", guest.getServiceCharges());
        result.put("discount", guest.getDiscount());
        result.put("totalBill", total);
        result.put("message", "Check-out complete. Thank you for staying at GrandStay!");
        return result;
    }

    public List<RoomEntity> getAllRooms() {
        return roomRepository.findAll();
    }

    public List<GuestEntity> getAllGuests() {
        return guestRepository.findAll();
    }

    private void publishEvent(HotelEvent.EventType type, Map<String, Object> payloadMap) {
        try {
            String payload = objectMapper.writeValueAsString(payloadMap);
            HotelEvent event = new HotelEvent(type, "reception-service", payload);
            String routingKey = type.name().toLowerCase().replace("_", ".");
            rabbitTemplate.convertAndSend(RabbitMQConstants.EXCHANGE, routingKey, event);
        } catch (Exception e) {
            System.err.println("[Reception] Failed to publish event: " + e.getMessage());
        }
    }
}
