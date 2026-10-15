package com.example.simpletodo.task.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.simpletodo.task.dto.request.CreateTaskRequest;
import com.example.simpletodo.task.dto.request.UpdateTaskRequest;
import com.example.simpletodo.task.dto.request.UpdateTaskStatusRequest;
import com.example.simpletodo.task.dto.response.TaskDashboardResponse;
import com.example.simpletodo.task.dto.response.TaskResponse;
import com.example.simpletodo.task.model.Task;
import com.example.simpletodo.task.model.TaskStatus;
import com.example.simpletodo.task.repository.TaskRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Tag("postgres-integration")
@SpringBootTest
class TaskPersistenceIntegrationTest {
    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void createdTaskCanBeReadBackFromPostgres() {
        String title = "Persistence check " + UUID.randomUUID();
        TaskResponse created = taskService.create(new CreateTaskRequest(title, "Persisted task notes"));

        try {
            Task reloaded = taskRepository.findById(created.id()).orElseThrow();
            assertThat(reloaded.getTitle()).isEqualTo(title);
            assertThat(reloaded.getNotes()).isEqualTo("Persisted task notes");
            assertThat(reloaded.getStatus()).isEqualTo(TaskStatus.TODO);
        } finally {
            taskRepository.deleteById(created.id());
        }
    }

    @Test
    void dashboardReturnsNewestSevenInDeterministicOrder() {
        Instant base = Instant.parse("2026-10-13T10:00:00Z");
        long uuidPrefix = UUID.randomUUID().getMostSignificantBits();
        long[] updateOffsets = {5, 4, 4, 3, 2, 1, 1, 1, 1};
        List<Task> tasks = new ArrayList<>();
        List<UUID> ids = new ArrayList<>();
        for (int index = 0; index < updateOffsets.length; index++) {
            UUID id = new UUID(uuidPrefix, index + 1L);
            ids.add(id);
            tasks.add(new Task(
                    id,
                    "Dashboard order " + (index + 1),
                    null,
                    TaskStatus.TODO,
                    base,
                    base.plusSeconds(updateOffsets[index])));
        }

        try {
            taskRepository.saveAll(tasks);
            TaskDashboardResponse dashboard = taskService.getDashboard();

            assertThat(dashboard.items())
                    .extracting(TaskResponse::id)
                    .containsExactlyElementsOf(ids.subList(0, TaskDashboardResponse.DASHBOARD_LIMIT));
            assertThat(dashboard.total()).isEqualTo(TaskDashboardResponse.DASHBOARD_LIMIT);
            assertThat(dashboard.limit()).isEqualTo(TaskDashboardResponse.DASHBOARD_LIMIT);
        } finally {
            taskRepository.deleteAllById(ids);
        }
    }

    @Test
    void updatingDashboardTaskPersistsFieldsRecencyAndDashboardCutoff() {
        Instant base = Instant.parse("2026-10-13T10:00:00Z");
        long uuidPrefix = UUID.randomUUID().getMostSignificantBits();
        List<Task> tasks = new ArrayList<>();
        List<UUID> ids = new ArrayList<>();
        for (int index = 0; index < 8; index++) {
            UUID id = new UUID(uuidPrefix, index + 1L);
            ids.add(id);
            tasks.add(new Task(
                    id,
                    "Mutable task " + (index + 1),
                    null,
                    TaskStatus.TODO,
                    base,
                    base.plusSeconds(8 - index)));
        }

        try {
            taskRepository.saveAll(tasks);
            Instant previousUpdatedAt = tasks.get(0).getUpdatedAt();
            TaskResponse replaced = taskService.update(
                    ids.get(0),
                    new UpdateTaskRequest("Updated task title", "Updated task notes", TaskStatus.IN_PROGRESS));
            TaskResponse statusChanged = taskService.updateStatus(
                    ids.get(0),
                    new UpdateTaskStatusRequest(TaskStatus.COMPLETED));
            Task persisted = taskRepository.findById(ids.get(0)).orElseThrow();
            TaskDashboardResponse dashboard = taskService.getDashboard();

            assertThat(replaced.updatedAt()).isAfter(previousUpdatedAt);
            assertThat(persisted.getTitle()).isEqualTo("Updated task title");
            assertThat(persisted.getNotes()).isEqualTo("Updated task notes");
            assertThat(persisted.getStatus()).isEqualTo(TaskStatus.COMPLETED);
            assertThat(statusChanged.updatedAt()).isAfter(replaced.updatedAt());
            assertThat(dashboard.items())
                    .extracting(TaskResponse::id)
                    .containsExactly(ids.get(0), ids.get(1), ids.get(2), ids.get(3), ids.get(4), ids.get(5), ids.get(6));
            assertThat(dashboard.items().get(0).status()).isEqualTo(TaskStatus.COMPLETED);
        } finally {
            taskRepository.deleteAllById(ids);
        }
    }
}
