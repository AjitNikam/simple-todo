package com.example.simpletodo.task;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class TaskService {
    private static final Sort RECENCY_ORDER = Sort.by(
            Sort.Order.desc("updatedAt"),
            Sort.Order.asc("id"));

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    public TaskResponse create(CreateTaskRequest request) {
        Instant now = Instant.now();
        Task task = new Task(
                UUID.randomUUID(),
                request.title().strip(),
                request.notes(),
                TaskStatus.TODO,
                now,
                now);
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse update(UUID taskId, UpdateTaskRequest request) {
        Task task = findDashboardTask(taskId);
        task.replace(request.title().strip(), request.notes(), request.status(), nextUpdatedAt(task));
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateStatus(UUID taskId, UpdateTaskStatusRequest request) {
        Task task = findDashboardTask(taskId);
        task.changeStatus(request.status(), nextUpdatedAt(task));
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public TaskDashboardResponse getDashboard() {
        List<TaskResponse> items = taskRepository
                .findAll(PageRequest.of(0, TaskDashboardResponse.DASHBOARD_LIMIT, RECENCY_ORDER))
                .map(TaskResponse::from)
                .getContent();
                
        return new TaskDashboardResponse(items, items.size(), TaskDashboardResponse.DASHBOARD_LIMIT);
    }

    private Task findDashboardTask(UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found."));
        boolean onDashboard = taskRepository
                .findAll(PageRequest.of(0, TaskDashboardResponse.DASHBOARD_LIMIT, RECENCY_ORDER))
                .getContent()
                .stream()
                .anyMatch(dashboardTask -> dashboardTask.getId().equals(taskId));
        if (!onDashboard) {
            throw new TaskInHistoryException();
        }
        return task;
    }

    private static Instant nextUpdatedAt(Task task) {
        Instant now = Instant.now();
        Instant minimum = task.getUpdatedAt().plusMillis(1);
        return now.isAfter(minimum) ? now : minimum;
    }
}
