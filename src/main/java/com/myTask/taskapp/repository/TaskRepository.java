package com.myTask.taskapp.repository;

import com.myTask.taskapp.entity.Task;
import com.myTask.taskapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findByUserAndId(User user, Long taskId);
    List<Task> findByUser(User user);


}
