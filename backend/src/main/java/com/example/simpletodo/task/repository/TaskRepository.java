package com.example.simpletodo.task.repository;

import com.example.simpletodo.task.model.Task;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select task from Task task")
    List<Task> findDashboardTasksForUpdate(Pageable pageable);
}
