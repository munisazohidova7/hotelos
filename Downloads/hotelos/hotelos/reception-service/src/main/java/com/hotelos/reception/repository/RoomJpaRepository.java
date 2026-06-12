package com.hotelos.reception.repository;

import com.hotelos.reception.model.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * HotelOS - Room JPA Repository.
 * Spring Data JPA automatically generates SQL queries.
 */
@Repository
public interface RoomJpaRepository extends JpaRepository<RoomEntity, Integer> {

    // Find all rooms by status
    List<RoomEntity> findByStatus(RoomEntity.RoomStatus status);

    // Find all rooms by type and status
    List<RoomEntity> findByTypeAndStatus(RoomEntity.RoomType type,
                                          RoomEntity.RoomStatus status);

    // Find room by current guest
    RoomEntity findByCurrentGuestId(String guestId);
}
