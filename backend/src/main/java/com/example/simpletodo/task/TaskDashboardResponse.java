package com.example.simpletodo.task;

import java.util.List;

public record TaskDashboardResponse(List<TaskResponse> items, int total, int limit) {
    public static final int DASHBOARD_LIMIT = 7;
}
