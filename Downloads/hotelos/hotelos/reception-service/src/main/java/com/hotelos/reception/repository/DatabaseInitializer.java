package com.hotelos.reception.repository;

import com.hotelos.reception.model.RoomEntity;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * HotelOS - Database Initializer.
 * Seeds 10 rooms into PostgreSQL on first startup.
 * Skips if rooms already exist.
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final RoomJpaRepository roomRepository;

    public DatabaseInitializer(RoomJpaRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public void run(String... args) {
        // Only seed if database is empty
        if (roomRepository.count() > 0) {
            System.out.println("[DB] Rooms already exist — skipping seed.");
            return;
        }

        System.out.println("[DB] Seeding initial room data...");

        long offset = 0;

        // Floor 1
        save(101, 1, RoomEntity.RoomType.SINGLE,     80.0,  true,  false, offset += 2);
        save(102, 1, RoomEntity.RoomType.SINGLE,     80.0,  false, true,  offset += 2);
        save(103, 1, RoomEntity.RoomType.DOUBLE,    120.0,  true,  false, offset += 2);
        save(104, 1, RoomEntity.RoomType.DOUBLE,    120.0,  false, false, offset += 2);
        save(105, 1, RoomEntity.RoomType.ACCESSIBLE, 90.0,  true,  true,  offset += 2);

        // Floor 2
        save(201, 2, RoomEntity.RoomType.SINGLE,     80.0,  true,  false, offset += 2);
        save(202, 2, RoomEntity.RoomType.DOUBLE,    120.0,  false, true,  offset += 2);
        save(203, 2, RoomEntity.RoomType.SUITE,     250.0,  true,  false, offset += 2);
        save(204, 2, RoomEntity.RoomType.DOUBLE,    120.0,  false, false, offset += 2);
        save(205, 2, RoomEntity.RoomType.SUITE,     250.0,  true,  false, offset + 2);

        System.out.println("[DB] 10 rooms seeded successfully.");
    }

    private void save(int number, int floor, RoomEntity.RoomType type,
                      double price, boolean nearLift, boolean nearStairs, long hoursAgo) {
        RoomEntity room = new RoomEntity(number, floor, type, price, nearLift, nearStairs);
        room.setLastCleanedAt(LocalDateTime.now().minusHours(hoursAgo));
        roomRepository.save(room);
    }
}
