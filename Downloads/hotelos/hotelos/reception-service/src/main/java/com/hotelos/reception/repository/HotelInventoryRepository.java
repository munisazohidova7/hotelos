package com.hotelos.reception.repository;

import com.hotelos.shared.model.Room;
import com.hotelos.shared.model.Guest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HotelOS - In-memory room and guest inventory.
 *
 * Data structures used:
 *   - rooms: List<Room>     → supports index-based access for the assignment algorithm
 *   - guests: Map<String, Guest> → O(1) lookup by guestId (HashMap / dictionary)
 *
 * Thread safety: ConcurrentHashMap for guests; synchronized on rooms list for
 * concurrent check-in (TS-06 scenario: two guests requesting same room type at once).
 *
 * Initial inventory: 10 rooms across 2 floors (as per Task 3 instructions).
 */
@Repository
public class HotelInventoryRepository {

    // Room inventory — synchronized list to prevent double-booking (TS-06)
    private final List<Room> rooms = Collections.synchronizedList(new ArrayList<>());

    // Guest registry — ConcurrentHashMap for thread-safe O(1) access
    private final Map<String, Guest> guests = new ConcurrentHashMap<>();

    public HotelInventoryRepository() {
        initializeRooms();
    }

    /**
     * Initialize 10 rooms across 2 floors.
     * Floor 1: Rooms 101-105 | Floor 2: Rooms 201-205
     */
    private void initializeRooms() {
        // Floor 1
        rooms.add(new Room(101, 1, Room.RoomType.SINGLE,     80.0,  true,  false));
        rooms.add(new Room(102, 1, Room.RoomType.SINGLE,     80.0,  false, true));
        rooms.add(new Room(103, 1, Room.RoomType.DOUBLE,    120.0,  true,  false));
        rooms.add(new Room(104, 1, Room.RoomType.DOUBLE,    120.0,  false, false));
        rooms.add(new Room(105, 1, Room.RoomType.ACCESSIBLE, 90.0,  true,  true));

        // Floor 2
        rooms.add(new Room(201, 2, Room.RoomType.SINGLE,     80.0,  true,  false));
        rooms.add(new Room(202, 2, Room.RoomType.DOUBLE,    120.0,  false, true));
        rooms.add(new Room(203, 2, Room.RoomType.SUITE,     250.0,  true,  false));
        rooms.add(new Room(204, 2, Room.RoomType.DOUBLE,    120.0,  false, false));
        rooms.add(new Room(205, 2, Room.RoomType.SUITE,     250.0,  true,  false));

        // Stagger lastCleanedAt so the rotation algorithm has distinct values
        long offset = 0;
        for (Room room : rooms) {
            room.setLastCleanedAt(LocalDateTime.now().minusHours(offset));
            offset += 2;
        }
    }

    // --- Room operations ---

    public List<Room> getAllRooms() {
        return Collections.unmodifiableList(rooms);
    }

    public Optional<Room> getRoomByNumber(int roomNumber) {
        return rooms.stream()
                .filter(r -> r.getRoomNumber() == roomNumber)
                .findFirst();
    }

    /**
     * Update a room's status. Synchronized to prevent race conditions (TS-06).
     */
    public synchronized boolean updateRoomStatus(int roomNumber, Room.RoomStatus newStatus) {
        Optional<Room> room = getRoomByNumber(roomNumber);
        room.ifPresent(r -> {
            r.setStatus(newStatus);
            if (newStatus == Room.RoomStatus.CLEAN) {
                r.setLastCleanedAt(LocalDateTime.now());
            }
        });
        return room.isPresent();
    }

    public synchronized boolean assignGuestToRoom(int roomNumber, String guestId) {
        Optional<Room> room = getRoomByNumber(roomNumber);
        room.ifPresent(r -> {
            r.setStatus(Room.RoomStatus.OCCUPIED);
            r.setCurrentGuestId(guestId);
        });
        return room.isPresent();
    }

    public synchronized boolean vacateRoom(int roomNumber) {
        Optional<Room> room = getRoomByNumber(roomNumber);
        room.ifPresent(r -> {
            r.setStatus(Room.RoomStatus.DIRTY);
            r.setCurrentGuestId(null);
        });
        return room.isPresent();
    }

    // --- Guest operations ---

    public void saveGuest(Guest guest) {
        guests.put(guest.getGuestId(), guest);
    }

    public Optional<Guest> findGuestById(String guestId) {
        return Optional.ofNullable(guests.get(guestId));
    }

    public Optional<Guest> findGuestByRoom(int roomNumber) {
        return guests.values().stream()
                .filter(g -> g.getAssignedRoomNumber() == roomNumber
                        && g.getActualCheckOut() == null)
                .findFirst();
    }

    public void removeGuest(String guestId) {
        guests.remove(guestId);
    }

    public Collection<Guest> getAllActiveGuests() {
        return guests.values().stream()
                .filter(g -> g.getActualCheckOut() == null)
                .toList();
    }
}
