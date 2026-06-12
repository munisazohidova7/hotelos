package com.hotelos.reception.service;

import com.hotelos.shared.model.Room;
import com.hotelos.shared.model.Guest;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * HotelOS - Room Assignment Algorithm (LO1 / Task 1)
 *
 * Algorithm steps (in priority order):
 *   1. Filter by room type (SINGLE / DOUBLE / SUITE / ACCESSIBLE)
 *   2. Filter by status == CLEAN (exclude DIRTY, CLEANING, MAINTENANCE, OCCUPIED)
 *   3. Sort by lastCleanedAt ascending (longest-clean first) for even rotation
 *   4. Apply floor preference as secondary filter (if no match, skip this filter)
 *   5. Apply proximity preference (nearLift / nearStairs) as final tiebreaker
 *
 * Time complexity: O(n log n) where n = number of rooms
 * Space complexity: O(n) for filtered list
 */
@Service
public class RoomAssignmentService {

    /**
     * Main entry point: assign the best available room to a guest.
     *
     * @param guest     The guest requesting a room
     * @param allRooms  Full inventory of all rooms
     * @return          Optional<Room> — empty if no room available
     */
    public Optional<Room> assignRoom(Guest guest, List<Room> allRooms) {

        // --- Step 1 & 2: Filter by type AND clean status ---
        List<Room> eligible = allRooms.stream()
                .filter(room -> room.getType() == guest.getRequestedRoomType())
                .filter(room -> room.getStatus() == Room.RoomStatus.CLEAN)
                .collect(java.util.stream.Collectors.toList());

        // No rooms available at all
        if (eligible.isEmpty()) {
            return Optional.empty();
        }

        // --- Step 3: Sort by longest-clean first (even rotation) ---
        eligible.sort(Comparator.comparing(Room::getLastCleanedAt));

        // --- Step 4: Apply floor preference (optional) ---
        if (guest.getPreferredFloor() > 0) {
            List<Room> floorMatch = eligible.stream()
                    .filter(room -> room.getFloor() == guest.getPreferredFloor())
                    .collect(java.util.stream.Collectors.toList());

            // Only apply floor filter if rooms are actually available on that floor
            if (!floorMatch.isEmpty()) {
                eligible = floorMatch;
            }
            // If no match on preferred floor -> fall through with all eligible rooms
        }

        // --- Step 5: Apply proximity preference as tiebreaker ---
        if (guest.isWantsNearLift()) {
            List<Room> liftMatch = eligible.stream()
                    .filter(Room::isNearLift)
                    .collect(java.util.stream.Collectors.toList());
            if (!liftMatch.isEmpty()) {
                return Optional.of(liftMatch.get(0));
            }
        }

        if (guest.isWantsNearStairs()) {
            List<Room> stairsMatch = eligible.stream()
                    .filter(Room::isNearStairs)
                    .collect(java.util.stream.Collectors.toList());
            if (!stairsMatch.isEmpty()) {
                return Optional.of(stairsMatch.get(0));
            }
        }

        // --- Final: return the longest-clean eligible room ---
        return Optional.of(eligible.get(0));
    }
}
