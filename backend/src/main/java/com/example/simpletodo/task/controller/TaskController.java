package com.example.simpletodo.task.controller;

import com.example.simpletodo.task.dto.request.CreateTaskRequest;
import com.example.simpletodo.task.dto.request.UpdateTaskRequest;
import com.example.simpletodo.task.dto.request.UpdateTaskStatusRequest;
import com.example.simpletodo.task.dto.response.TaskDashboardResponse;
import com.example.simpletodo.task.dto.response.TaskResponse;
import com.example.simpletodo.task.service.TaskService;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/dashboard")
    public TaskDashboardResponse getDashboard() {
        return taskService.getDashboard();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@Valid @RequestBody CreateTaskRequest request) {
        return taskService.create(request);
    }

    @PutMapping("/{taskId}")
    public TaskResponse update(
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskRequest request) {
        return taskService.update(taskId, request);
    }

    @PatchMapping("/{taskId}/status")
    public TaskResponse updateStatus(
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskStatusRequest request) {
        return taskService.updateStatus(taskId, request);
    }
}
