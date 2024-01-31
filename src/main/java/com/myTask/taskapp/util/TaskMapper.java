package com.myTask.taskapp.util;


import com.myTask.taskapp.dto.responseDto.TaskResponse;
import com.myTask.taskapp.entity.Task;

public class TaskMapper {

    public static TaskResponse mapToTaskResponse(Task task, TaskResponse taskResponse){
        taskResponse.setDescription(task.getDescription());
        taskResponse.setDescription(task.getName());
        taskResponse.setDueDate(task.getDueDate());
        return taskResponse;
    }
}
