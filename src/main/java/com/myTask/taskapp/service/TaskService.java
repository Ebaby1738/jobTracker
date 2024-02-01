package com.myTask.taskapp.service;

import com.myTask.taskapp.dto.requestDto.TaskRequest;
import com.myTask.taskapp.dto.responseDto.ApiResponse;
import com.myTask.taskapp.dto.responseDto.TaskResponse;
import com.myTask.taskapp.entity.Task;

import java.util.List;

public interface TaskService {


    ApiResponse<Task> createTask(TaskRequest request, String email);

    ApiResponse<Task> updateTask(Long taskId, TaskRequest request, String email);

    ApiResponse<TaskResponse> assignTaskToEmployee(String email, Long taskId, TaskRequest request);

    ApiResponse<List<TaskResponse>> getAllTaskByEmployer(String email);

    ApiResponse<List<TaskResponse>> getAllAssignedTaskByEmployee(String email);

    ApiResponse<String> deleteTask(Long taskId, String userEmail);
}
