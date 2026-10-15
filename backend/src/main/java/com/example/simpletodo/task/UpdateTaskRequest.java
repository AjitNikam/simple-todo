package com.example.simpletodo.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskRequest(
        @NotBlank(message = "Title is required.")
        String title,
        String notes,
        @NotNull(message = "Status is required.")
        TaskStatus status) {
}
