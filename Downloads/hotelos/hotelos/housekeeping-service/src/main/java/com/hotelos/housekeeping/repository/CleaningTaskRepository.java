package com.hotelos.housekeeping.repository;

import com.hotelos.housekeeping.model.CleaningTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CleaningTaskRepository extends JpaRepository<CleaningTask, Long> {
    List<CleaningTask> findByStatusNot(CleaningTask.TaskStatus status);
    Optional<CleaningTask> findByRoomNumberAndStatusNot(int roomNumber, CleaningTask.TaskStatus status);
}
