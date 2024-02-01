package com.myTask.taskapp.dto.responseDto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class TaskResponse {

    private String name;
    private String description;

    private LocalDateTime dueDate;
    private String employeeUsername;
}
