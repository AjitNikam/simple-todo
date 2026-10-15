package com.example.simpletodo.task.dto.response;

import com.example.simpletodo.task.model.Task;
import com.example.simpletodo.task.model.TaskStatus;
import java.time.Instant;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String notes,
        TaskStatus status,
        Instant createdAt,
        Instant updatedAt) {

    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getNotes(),
                task.getStatus(),
                task.getCreatedAt(),
                task.getUpdatedAt());
    }
}
