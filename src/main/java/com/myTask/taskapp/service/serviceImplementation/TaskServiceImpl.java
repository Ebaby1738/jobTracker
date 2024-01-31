package com.myTask.taskapp.service.serviceImplementation;

import com.myTask.taskapp.dto.requestDto.TaskRequest;
import com.myTask.taskapp.dto.responseDto.ApiResponse;
import com.myTask.taskapp.dto.responseDto.TaskResponse;
import com.myTask.taskapp.entity.Task;
import com.myTask.taskapp.entity.User;
import com.myTask.taskapp.enums.Role;
import com.myTask.taskapp.exceptions.UserExistException;
import com.myTask.taskapp.repository.TaskRepository;
import com.myTask.taskapp.repository.UserRepository;
import com.myTask.taskapp.service.TaskService;
import com.myTask.taskapp.util.TaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Override
    public ApiResponse<Task> createTask(TaskRequest request, String email){
        checkUserIsAuthorized(email);
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return new ApiResponse<>("10", "User not found", HttpStatus.NOT_FOUND);
        }
        User user = userOptional.get();
        Task newTask = new Task();
        newTask.setDescription(request.getDescription());
        newTask.setName(request.getName());
        newTask.setUser(user);
        newTask.setDueDate(request.getDueDate());
        Task createdTask = taskRepository.save(newTask);
        return new ApiResponse<>("00", "Task created successfully", createdTask, "OK");
    }


    @Override
    public ApiResponse<Task> updateTask(Long taskId, TaskRequest request, String email){
        checkUserIsAuthorized(email);
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return new ApiResponse<>("10", "User not found", HttpStatus.NOT_FOUND);
        }
        User user = userOptional.get();
        Optional<Task> fetchTask = taskRepository.findByUserAndId(user, taskId);
        if (fetchTask.isPresent()) {
            Task task = fetchTask.get();
            task.setDescription(request.getDescription());
            task.setName(request.getName());
            task.setDueDate(request.getDueDate());
            taskRepository.save(task);
            log.info("Task status updated successfully by user");
            return new ApiResponse<>("200", "Task updated successfully", HttpStatus.OK);
        } else {
            log.info("Task not found");
            return new ApiResponse<>("404", "Order not found", HttpStatus.NOT_FOUND);
        }
    }


    @Override
    public ApiResponse<List<TaskResponse>> getAllTaskByUser(String email) {
        checkUserIsAuthorized(email);
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return new ApiResponse<>("10", "User not found", HttpStatus.NOT_FOUND);
        }
        User user = userOptional.get();
        log.info("Checking if user's task exists");
        List<Task> fetchAllTask = taskRepository.findByUser(user);
        if (fetchAllTask.isEmpty()) {
            log.info("User doesn't have any tasks");
            return new ApiResponse<>("10", "Task not found", HttpStatus.NOT_FOUND);
        }
        List<TaskResponse> taskResponses = new ArrayList<>();
        for (Task task : fetchAllTask) {
            TaskResponse taskResponse = new TaskResponse();
            TaskMapper.mapToTaskResponse(task, taskResponse);
            taskResponses.add(taskResponse);
        }
        return new ApiResponse<>("200", "Tasks found", taskResponses, HttpStatus.OK);
    }



    @Override
    public ApiResponse<TaskResponse> getTaskByUser(String email, Long taskId) {
        checkUserIsAuthorized(email);
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return new ApiResponse<>("10", "User not found", HttpStatus.NOT_FOUND);
        }
        User user = userOptional.get();
        log.info("Checking if user's task exists");
        Optional<Task> fetchTask = taskRepository.findByUserAndId(user, taskId);
        if (fetchTask.isEmpty()) {
            log.info("User doesn't have any tasks");
            return new ApiResponse<>("10", "Task not found", HttpStatus.NOT_FOUND);
        }
            Task task = fetchTask.get();
            TaskResponse taskResponse = new TaskResponse();
            TaskMapper.mapToTaskResponse(task, taskResponse);

        return new ApiResponse<>("200", "Tasks found", taskResponse, HttpStatus.OK);
    }



    @Override
    public ApiResponse<String> deleteTask(Long taskId, String userEmail) {
        checkUserIsAuthorized(userEmail);
        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isEmpty()) {
            return new ApiResponse<>("10", "User not found", HttpStatus.NOT_FOUND);
        }
        User user = userOptional.get();
        log.info("Checking if user's task exists");
        Optional<Task> fetchTask = taskRepository.findByUserAndId(user, taskId);

        if (fetchTask.isPresent()) {
            Task task = fetchTask.get();
            log.info("Deleting the order");
            taskRepository.delete(task);
            log.info("Order deleted successfully");
        }else {
            log.error("User is not the creator of this order");
            return new ApiResponse<>("10", "Task not found", HttpStatus.FORBIDDEN);
        }
        return new ApiResponse<>("00", "Task deleted successfully", HttpStatus.OK);
    }



    private void checkUserIsAuthorized(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent() && user.get().getRole() == Role.ADMIN || user.isPresent() && user.get().getRole() == Role.APP_USER) {
            log.info("User with email '{}' has access to make this change..access granted!!.", email);
            return;
        }
        log.info("Access denied. User with email '{}' is not authorized .", email);
        throw new UserExistException("Only authorized personnels can access this functionality.");
    }

}
