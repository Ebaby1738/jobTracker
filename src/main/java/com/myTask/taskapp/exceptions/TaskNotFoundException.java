package com.myTask.taskapp.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Setter
@Getter
public class TaskNotFoundException extends RuntimeException {

    private String message;
    private HttpStatus httpStatus;

    public TaskNotFoundException(String message) {
        super(message);
    }

    public TaskNotFoundException(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public TaskNotFoundException() {

    }
}
