package com.example.simpletodo.task.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.simpletodo.api.ApiExceptionHandler;
import com.example.simpletodo.config.WebConfig;
import com.example.simpletodo.task.dto.request.CreateTaskRequest;
import com.example.simpletodo.task.dto.request.UpdateTaskRequest;
import com.example.simpletodo.task.dto.request.UpdateTaskStatusRequest;
import com.example.simpletodo.task.dto.response.TaskDashboardResponse;
import com.example.simpletodo.task.dto.response.TaskResponse;
import com.example.simpletodo.task.exception.TaskInHistoryException;
import com.example.simpletodo.task.model.TaskStatus;
import com.example.simpletodo.task.service.TaskService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskController.class)
@Import({ApiExceptionHandler.class, WebConfig.class})
class TaskControllerTest {
    private static final UUID TASK_ID = UUID.fromString("a48bd703-2086-4c31-b7e2-cdfc1844a67a");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Test
    void validCreateReturnsCreatedTask() throws Exception {
        Instant now = Instant.parse("2026-10-13T10:00:00Z");
        when(taskService.create(any(CreateTaskRequest.class))).thenReturn(new TaskResponse(
                UUID.fromString("a48bd703-2086-4c31-b7e2-cdfc1844a67a"),
                "Prepare demo",
                null,
                TaskStatus.TODO,
                now,
                now));

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":" Prepare demo "}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Prepare demo"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.id").value("a48bd703-2086-4c31-b7e2-cdfc1844a67a"));
    }

    @Test
    void blankTitleReturnsProblemDetailsWithoutCreatingTask() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"   "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.detail").value("Title is required."))
                .andExpect(jsonPath("$.instance").value("/api/tasks"));

        verify(taskService, never()).create(any(CreateTaskRequest.class));
    }

    @Test
    void validFullUpdateReturnsUpdatedTask() throws Exception {
        Instant createdAt = Instant.parse("2026-10-13T10:00:00Z");
        Instant updatedAt = createdAt.plusSeconds(60);
        when(taskService.update(any(UUID.class), any(UpdateTaskRequest.class))).thenReturn(new TaskResponse(
                TASK_ID,
                "Updated title",
                "Updated notes",
                TaskStatus.IN_PROGRESS,
                createdAt,
                updatedAt));

        mockMvc.perform(put("/api/tasks/{taskId}", TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":" Updated title ","notes":"Updated notes","status":"IN_PROGRESS"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.notes").value("Updated notes"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.updatedAt").value("2026-10-13T10:01:00Z"));
    }

    @Test
    void blankFullUpdateTitleReturnsValidationProblem() throws Exception {
        mockMvc.perform(put("/api/tasks/{taskId}", TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"   ","notes":null,"status":"TODO"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.detail").value("Title is required."));

        verify(taskService, never()).update(any(UUID.class), any(UpdateTaskRequest.class));
    }

    @Test
    void missingFullUpdateTitleReturnsValidationProblem() throws Exception {
        mockMvc.perform(put("/api/tasks/{taskId}", TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"notes":null,"status":"TODO"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.detail").value("Title is required."));

        verify(taskService, never()).update(any(UUID.class), any(UpdateTaskRequest.class));
    }

    @Test
    void unsupportedStatusReturnsValidationProblem() throws Exception {
        mockMvc.perform(patch("/api/tasks/{taskId}/status", TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"BLOCKED"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verify(taskService, never()).updateStatus(any(UUID.class), any(UpdateTaskStatusRequest.class));
    }

    @Test
    void fullUpdateRejectsUnsupportedProperties() throws Exception {
        mockMvc.perform(put("/api/tasks/{taskId}", TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Updated title","notes":null,"status":"TODO","createdAt":"2026-10-13T10:00:00Z"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verify(taskService, never()).update(any(UUID.class), any(UpdateTaskRequest.class));
    }

    @Test
    void validStatusUpdateReturnsUpdatedTask() throws Exception {
        Instant createdAt = Instant.parse("2026-10-13T10:00:00Z");
        Instant updatedAt = createdAt.plusSeconds(60);
        when(taskService.updateStatus(any(UUID.class), any(UpdateTaskStatusRequest.class)))
                .thenReturn(new TaskResponse(
                        TASK_ID,
                        "Prepare demo",
                        null,
                        TaskStatus.COMPLETED,
                        createdAt,
                        updatedAt));

        mockMvc.perform(patch("/api/tasks/{taskId}/status", TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"COMPLETED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void historyTaskReturnsTaskInHistoryConflict() throws Exception {
        when(taskService.update(any(UUID.class), any(UpdateTaskRequest.class)))
                .thenThrow(new TaskInHistoryException());

        mockMvc.perform(put("/api/tasks/{taskId}", TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Updated title","notes":null,"status":"TODO"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TASK_IN_HISTORY"))
                .andExpect(jsonPath("$.instance").value("/api/tasks/" + TASK_ID));
    }

    @Test
    void missingTaskReturnsNotFoundProblem() throws Exception {
        when(taskService.updateStatus(any(UUID.class), any(UpdateTaskStatusRequest.class)))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Task not found."));

        mockMvc.perform(patch("/api/tasks/{taskId}/status", TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"TODO"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void unknownRouteReturnsNotFoundProblem() throws Exception {
        mockMvc.perform(get("/api/unknown-resource"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.instance").value("/api/unknown-resource"));

        mockMvc.perform(post("/api/unknown-resource"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.instance").value("/api/unknown-resource"));
    }

    @Test
    void malformedTaskIdReturnsValidationProblem() throws Exception {
        mockMvc.perform(put("/api/tasks/not-a-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Updated title","notes":null,"status":"TODO"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void unsupportedCreatePropertyIsRejected() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Prepare demo","status":"COMPLETED"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verify(taskService, never()).create(any(CreateTaskRequest.class));
    }

    @Test
    void unsupportedMethodPreservesStatusAndAllowHeader() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string(HttpHeaders.ALLOW, "POST"))
                .andExpect(jsonPath("$.instance").value("/api/tasks"));
    }

    @Test
    void unsupportedMediaTypePreservesStatus() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Prepare demo"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.instance").value("/api/tasks"));
    }

    @Test
    void dashboardReturnsItemsAndLimit() throws Exception {
        when(taskService.getDashboard()).thenReturn(new TaskDashboardResponse(List.of(), 0, 7));

        mockMvc.perform(get("/api/tasks/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.limit").value(7));
    }

    @Test
    void corsAllowsOnlyConfiguredLocalOrigin() throws Exception {
        mockMvc.perform(options("/api/tasks/dashboard")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header()
                        .string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));

        mockMvc.perform(options("/api/tasks/dashboard")
                        .header(HttpHeaders.ORIGIN, "https://unapproved.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }
}
