package com.example.simpletodo.task.exception;

public class TaskInHistoryException extends RuntimeException {
    public TaskInHistoryException() {
        super("Task must be restored to the dashboard before it can be changed.");
    }
}
