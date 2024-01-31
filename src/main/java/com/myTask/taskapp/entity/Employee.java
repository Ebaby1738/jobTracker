package com.myTask.taskapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@DiscriminatorValue("Employee")
public class Employee extends User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


}
