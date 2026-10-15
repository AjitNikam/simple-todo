package com.example.simpletodo.task;

import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(
        @NotNull(message = "Status is required.")
        TaskStatus status) {
}
