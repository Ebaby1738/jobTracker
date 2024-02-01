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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
        newTask.setTaskName(request.getTaskName());
        newTask.setUser(user);
        LocalDate localDate = LocalDate.parse(request.getDueDate(), formatter);
        LocalTime localTime = LocalTime.of(0, 0); // midnight (00:00:00)
        // Combine LocalDate and LocalTime to create LocalDateTime
        LocalDateTime dueDate = LocalDateTime.of(localDate, localTime);
        newTask.setDueDate(dueDate);
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
            task.setTaskName(request.getTaskName());
            LocalDate localDate = LocalDate.parse(request.getDueDate(), formatter);
            LocalTime localTime = LocalTime.of(0, 0); // midnight (00:00:00)
            // Combine LocalDate and LocalTime to create LocalDateTime
            LocalDateTime dueDate = LocalDateTime.of(localDate, localTime);
            task.setDueDate(dueDate);
            taskRepository.save(task);
            log.info("Task status updated successfully by user");
            return new ApiResponse<>("200", "Task updated successfully", HttpStatus.OK);
        } else {
            log.info("Task not found");
            return new ApiResponse<>("404", "Order not found", HttpStatus.NOT_FOUND);
        }
    }



    @Override
    public ApiResponse<TaskResponse> assignTaskToEmployee(String email, Long taskId, TaskRequest request) {
        Optional<Task> taskOptional = taskRepository.findById(taskId);
        Optional<User> employee = userRepository.findByUsername(request.getEmployeeUsername());
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user = userOptional.get();
        if (user.getRole().equals(Role.ADMIN)) {
            if (taskOptional.isPresent()) {
                Task task = taskOptional.get();
                if (employee.isPresent()) {
                    User selectedEmployee = employee.get();
                    task.setEmployeeUsername(selectedEmployee.getUserName());
                    taskRepository.save(task);
                    log.info("Task with ID:: " + taskId + " has been successfully assigned to user with username:: " + request.getEmployeeUsername());
                    Task assignedTask = task;
                    TaskResponse response = new TaskResponse();
                    response.setName(task.getTaskName());
                    response.setDescription(task.getDescription());
                    response.setDueDate(task.getDueDate());
                    response.setEmployeeUsername(task.getEmployeeUsername());

                    return new ApiResponse<>("00", "Success", response, "Ok", HttpStatus.OK);
                } else {
                    log.error("An error occurred because the user with the username:: " + request.getEmployeeUsername() + " Does not exist in the database");
                    return new ApiResponse<>("04", "user with the username:: " + request.getEmployeeUsername() + " cannot be found, please confirm the employess username", HttpStatus.NOT_FOUND);
                }
            } else {
                log.error("An error occurred because the task with ID:: " + taskId + "  Does not exist in the database");
                return new ApiResponse<>("04", "task with ID:: " + taskId + " cannot be found, please confirm the order ID before assigning order", HttpStatus.NOT_FOUND);
            }
        } else {
            log.error("A person Which is not an Admin is trying to use this service");
            return new ApiResponse<>("03", "You are not authorized to perform this task", HttpStatus.FORBIDDEN);
        }
    }


    @Override
    public ApiResponse<List<TaskResponse>> getAllTaskByEmployer (String email){
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
    public ApiResponse<List<TaskResponse>> getAllAssignedTaskByEmployee (String email){
        checkUserIsAuthorized(email);
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return new ApiResponse<>("10", "User not found", HttpStatus.NOT_FOUND);
        }
        User user = userOptional.get();
        log.info("Checking if user's task exists");
        List<Task> fetchAllTask = taskRepository.findByEmployeeUsername(user.getUserName());
        if (fetchAllTask.isEmpty()) {
            log.info("User doesn't have any tasks assigned to him yet");
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
    public ApiResponse<String> deleteTask (Long taskId, String userEmail){
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
        } else {
            log.error("User is not the creator of this order");
            return new ApiResponse<>("10", "Task not found", HttpStatus.FORBIDDEN);
        }
        return new ApiResponse<>("00", "Task deleted successfully", HttpStatus.OK);
    }


    private void checkUserIsAuthorized (String email){
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent() && user.get().getRole() == Role.ADMIN || user.isPresent() && user.get().getRole() == Role.EMPLOYEE) {
            log.info("User with email '{}' has access to make this change..access granted!!.", email);
            return;
        }
        log.info("Access denied. User with email '{}' is not authorized .", email);
        throw new UserExistException("Only authorized personnels can access this functionality.");
    }




    /*public ApiResponse<Task>updateTaskStatus(Long taskId, Status status, String email) {
        checkUserIsAuthorized(email);
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return new ApiResponse<>("10", "User not found", HttpStatus.NOT_FOUND);
        }
        User user = userOptional.get();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        // Check if the task status transition is valid, handle any other business logic validations
        task.setStatus(status);
        taskRepository.save(task);

        // Notify the employer about the task status update
        //messagingTemplate.convertAndSendToUser(task.getEmployerId().toString(), "/topic/task", "Task status updated.");
    }*/

    /*@Override
    public ApiResponse<TaskResponse> getAllTaskByEmployee (String email, Long taskId){
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
    }*/

}
