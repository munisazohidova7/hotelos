package com.hotelos.reception.repository;

import com.hotelos.reception.model.GuestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * HotelOS - Guest JPA Repository.
 */
@Repository
public interface GuestJpaRepository extends JpaRepository<GuestEntity, String> {

    // Find active guest by room number (not checked out)
    Optional<GuestEntity> findByAssignedRoomNumberAndActualCheckOutIsNull(int roomNumber);

    // Find all active guests
    List<GuestEntity> findByActualCheckOutIsNull();
}
