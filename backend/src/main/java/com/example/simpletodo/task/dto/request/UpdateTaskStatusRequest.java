package com.example.simpletodo.task.dto.request;

import com.example.simpletodo.task.model.TaskStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(
        @NotNull(message = "Status is required.")
        TaskStatus status) {
}
