package com.hotelos.roomservice.repository;

import com.hotelos.roomservice.model.RoomServiceOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoomServiceOrderRepository extends JpaRepository<RoomServiceOrder, String> {
    List<RoomServiceOrder> findByRoomNumber(int roomNumber);
    List<RoomServiceOrder> findByStatusNot(RoomServiceOrder.OrderStatus status);
}
