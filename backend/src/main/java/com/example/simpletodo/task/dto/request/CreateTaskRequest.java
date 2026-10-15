package com.example.simpletodo.task.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateTaskRequest(
        @NotBlank(message = "Title is required.")
        String title,
        String notes) {
}
