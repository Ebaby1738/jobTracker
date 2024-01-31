package com.myTask.taskapp.dto.responseDto;

import lombok.*;

import java.time.LocalDate;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class TaskResponse {

    private String name;
    private String description;
    private String dueDate;
}
