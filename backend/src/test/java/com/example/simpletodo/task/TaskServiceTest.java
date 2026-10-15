package com.example.simpletodo.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void createTrimsTitleAndStartsTaskInTodo() {
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.create(new CreateTaskRequest("  Prepare demo  ", "Review the outline"));

        ArgumentCaptor<Task> saved = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(saved.capture());
        assertThat(saved.getValue().getTitle()).isEqualTo("Prepare demo");
        assertThat(saved.getValue().getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(response.notes()).isEqualTo("Review the outline");
        assertThat(saved.getValue().getId()).isNotNull();
        assertThat(saved.getValue().getCreatedAt()).isEqualTo(saved.getValue().getUpdatedAt());
    }

    @Test
    void dashboardQueryRequestsSevenNewestWithStableIdTieBreak() {
        Instant timestamp = Instant.parse("2026-10-13T10:00:00Z");
        List<Task> tasks = new ArrayList<>();
        for (int index = 0; index < 7; index++) {
            tasks.add(new Task(
                    UUID.randomUUID(),
                    "Task " + index,
                    null,
                    TaskStatus.TODO,
                    timestamp,
                    timestamp));
        }
        when(taskRepository.findAll(any(Pageable.class))).thenAnswer(invocation ->
                new PageImpl<>(tasks, invocation.getArgument(0), tasks.size()));

        TaskDashboardResponse dashboard = taskService.getDashboard();

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(taskRepository).findAll(pageable.capture());
        assertThat(pageable.getValue().getPageSize()).isEqualTo(7);
        assertThat(pageable.getValue().getSort().getOrderFor("updatedAt").getDirection())
                .isEqualTo(Sort.Direction.DESC);
        assertThat(pageable.getValue().getSort().getOrderFor("id").getDirection())
                .isEqualTo(Sort.Direction.ASC);
        assertThat(dashboard.items()).hasSize(7);
        assertThat(dashboard.total()).isEqualTo(7);
        assertThat(dashboard.limit()).isEqualTo(7);
    }

    @Test
    void updateReplacesDetailsAndStatusWithFreshRecency() {
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(600);
        Instant previousUpdatedAt = createdAt.plusSeconds(10);
        Task task = new Task(id, "Old title", "Old notes", TaskStatus.TODO, createdAt, previousUpdatedAt);
        when(taskRepository.findById(id)).thenReturn(Optional.of(task));
        when(taskRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(task)));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.update(
                id,
                new UpdateTaskRequest("  New title  ", "New notes", TaskStatus.IN_PROGRESS));

        assertThat(response.title()).isEqualTo("New title");
        assertThat(response.notes()).isEqualTo("New notes");
        assertThat(response.status()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.updatedAt()).isAfter(previousUpdatedAt);
        verify(taskRepository).save(task);
    }

    @Test
    void statusUpdateChangesOnlyStatusAndRefreshesRecency() {
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(600);
        Instant previousUpdatedAt = createdAt.plusSeconds(10);
        Task task = new Task(id, "Keep title", "Keep notes", TaskStatus.TODO, createdAt, previousUpdatedAt);
        when(taskRepository.findById(id)).thenReturn(Optional.of(task));
        when(taskRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(task)));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.updateStatus(id, new UpdateTaskStatusRequest(TaskStatus.COMPLETED));

        assertThat(response.title()).isEqualTo("Keep title");
        assertThat(response.notes()).isEqualTo("Keep notes");
        assertThat(response.status()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(response.updatedAt()).isAfter(previousUpdatedAt);
    }

    @Test
    void historyTaskCannotBeUpdated() {
        UUID historyId = UUID.randomUUID();
        Task historyTask = new Task(
                historyId,
                "History task",
                null,
                TaskStatus.TODO,
                Instant.now().minusSeconds(600),
                Instant.now().minusSeconds(300));
        List<Task> dashboardTasks = java.util.stream.IntStream.range(0, 7)
                .mapToObj(index -> new Task(
                        UUID.randomUUID(),
                        "Dashboard " + index,
                        null,
                        TaskStatus.TODO,
                        historyTask.getCreatedAt(),
                        historyTask.getUpdatedAt().plusSeconds(index + 1L)))
                .toList();
        when(taskRepository.findById(historyId)).thenReturn(Optional.of(historyTask));
        when(taskRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(dashboardTasks));

        assertThatThrownBy(() -> taskService.updateStatus(
                historyId,
                new UpdateTaskStatusRequest(TaskStatus.COMPLETED)))
                .isInstanceOf(TaskInHistoryException.class);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void mutationMovesTaskToFrontOfRefreshedSevenTaskDashboard() {
        Instant base = Instant.parse("2026-10-13T10:00:00Z");
        List<Task> tasks = new ArrayList<>();
        for (int index = 0; index < 8; index++) {
            tasks.add(new Task(
                    new UUID(10L, index + 1L),
                    "Task " + index,
                    null,
                    TaskStatus.TODO,
                    base,
                    base.plusSeconds(8 - index)));
        }
        Task selectedTask = tasks.get(6);
        when(taskRepository.findById(selectedTask.getId())).thenReturn(Optional.of(selectedTask));
        when(taskRepository.findAll(any(Pageable.class))).thenAnswer(invocation -> {
            Pageable pageable = invocation.getArgument(0);
            List<Task> sorted = tasks.stream()
                    .sorted(Comparator.comparing(Task::getUpdatedAt).reversed().thenComparing(Task::getId))
                    .toList();
            int pageEnd = Math.min(pageable.getPageSize(), sorted.size());
            return new PageImpl<>(sorted.subList(0, pageEnd), pageable, sorted.size());
        });
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        taskService.updateStatus(selectedTask.getId(), new UpdateTaskStatusRequest(TaskStatus.COMPLETED));
        TaskDashboardResponse dashboard = taskService.getDashboard();

        assertThat(dashboard.items())
                .extracting(TaskResponse::id)
                .containsExactly(
                        selectedTask.getId(),
                        tasks.get(0).getId(),
                        tasks.get(1).getId(),
                        tasks.get(2).getId(),
                        tasks.get(3).getId(),
                        tasks.get(4).getId(),
                        tasks.get(5).getId());
        assertThat(dashboard.items()).hasSize(7);
        assertThat(dashboard.items().get(0).status()).isEqualTo(TaskStatus.COMPLETED);
    }

    @Test
    void unknownTaskReturnsNotFound() {
        UUID id = UUID.randomUUID();
        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateStatus(id, new UpdateTaskStatusRequest(TaskStatus.COMPLETED)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }
}
