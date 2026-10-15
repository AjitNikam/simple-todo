package com.example.simpletodo.task.repository;

import com.example.simpletodo.task.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
}
