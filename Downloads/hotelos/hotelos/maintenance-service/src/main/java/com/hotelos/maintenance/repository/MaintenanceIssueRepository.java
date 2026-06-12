package com.hotelos.maintenance.repository;

import com.hotelos.maintenance.model.MaintenanceIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MaintenanceIssueRepository extends JpaRepository<MaintenanceIssue, String> {
    List<MaintenanceIssue> findByStatusNot(MaintenanceIssue.IssueStatus status);
}
