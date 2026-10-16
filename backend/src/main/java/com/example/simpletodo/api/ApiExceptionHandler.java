package com.example.simpletodo.api;

import java.net.URI;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.ProblemDetail;
import com.example.simpletodo.task.exception.TaskInHistoryException;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        List<Map<String, String>> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(ApiExceptionHandler::toFieldDetail)
                .toList();
        String detail = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Request validation failed.");
        return problem(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                detail,
                "VALIDATION_ERROR",
                details,
                request.getRequestURI(),
                HttpHeaders.EMPTY);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadableRequest(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                "Request body is invalid or contains unsupported properties.",
                "VALIDATION_ERROR",
                List.of(),
                request.getRequestURI(),
                HttpHeaders.EMPTY);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleInvalidArgument(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                "A request path or query value is invalid.",
                "VALIDATION_ERROR",
                List.of(),
                request.getRequestURI(),
                HttpHeaders.EMPTY);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> handleResponseStatus(
            ResponseStatusException exception,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        String code = status == HttpStatus.NOT_FOUND ? "NOT_FOUND" : "VALIDATION_ERROR";
        return problem(
                status,
                status.getReasonPhrase(),
                exception.getReason(),
                code,
                List.of(),
                request.getRequestURI(),
                HttpHeaders.EMPTY);
    }

    @ExceptionHandler(TaskInHistoryException.class)
    public ResponseEntity<ProblemDetail> handleTaskInHistory(
            TaskInHistoryException exception,
            HttpServletRequest request) {
        return problem(
                HttpStatus.CONFLICT,
                "Task is in History",
                exception.getMessage(),
                "TASK_IN_HISTORY",
                List.of(),
                request.getRequestURI(),
                HttpHeaders.EMPTY);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request) {
        return problem(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Method not allowed",
                "The HTTP method is not supported for this endpoint.",
                "VALIDATION_ERROR",
                List.of(),
                request.getRequestURI(),
                exception.getHeaders());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException exception,
            HttpServletRequest request) {
        return problem(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Unsupported media type",
                "The request Content-Type is not supported.",
                "VALIDATION_ERROR",
                List.of(),
                request.getRequestURI(),
                exception.getHeaders());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoResourceFound(
            NoResourceFoundException exception,
            HttpServletRequest request) {
        return problem(
                HttpStatus.NOT_FOUND,
                "Resource not found",
                "The requested resource was not found.",
                "NOT_FOUND",
                List.of(),
                request.getRequestURI(),
                HttpHeaders.EMPTY);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception exception, HttpServletRequest request) {
        LOGGER.error("Unexpected API failure", exception);
        return problem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                "The request could not be completed.",
                "INTERNAL_ERROR",
                List.of(),
                request.getRequestURI(),
                HttpHeaders.EMPTY);
    }

    private static Map<String, String> toFieldDetail(FieldError fieldError) {
        return Map.of(
                "field", fieldError.getField(),
                "issue", fieldError.getDefaultMessage() == null ? "is invalid" : fieldError.getDefaultMessage());
    }

    private static ResponseEntity<ProblemDetail> problem(
            HttpStatus status,
            String title,
            String detail,
            String code,
            List<Map<String, String>> details,
            String instance,
            HttpHeaders headers) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail);
        body.setType(URI.create("urn:simple-todo:problem:" + code.toLowerCase().replace('_', '-')));
        body.setTitle(title);
        body.setInstance(URI.create(instance));
        body.setProperty("code", code);
        body.setProperty("details", details);
        return ResponseEntity.status(status)
                .headers(headers)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }
}
