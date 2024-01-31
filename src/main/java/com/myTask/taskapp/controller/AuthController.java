package com.myTask.taskapp.controller;


import com.myTask.taskapp.dto.requestDto.ResetPasswordRequest;
import com.myTask.taskapp.dto.requestDto.UserLoginRequest;
import com.myTask.taskapp.dto.requestDto.UserRegisterRequest;
import com.myTask.taskapp.dto.responseDto.ApiResponse;
import com.myTask.taskapp.dto.responseDto.LoginResponse;
import com.myTask.taskapp.emails.EmailService;
import com.myTask.taskapp.entity.VerificationToken;
import com.myTask.taskapp.exceptions.CommonApplicationException;
import com.myTask.taskapp.service.UserService;
import com.myTask.taskapp.validation.PasswordValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/")
@Slf4j
public class AuthController {

    private final UserService userService;
    private final PasswordValidator passwordValidator;
    private final EmailService emailService;



    @PostMapping(path = "/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid UserRegisterRequest request) {
        log.info("controller register: register user :: [{}] ::", request.getEmail());
        passwordValidator.isValid(request);
        ApiResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }



    @PostMapping(path = "/login")
    public ResponseEntity<ApiResponse<LoginResponse>> loginUser(@RequestBody @Valid UserLoginRequest request) {
        log.info("request to login user");
        ApiResponse<LoginResponse> response = userService.login(request);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }



    @PostMapping("/confirm-account")
    public ResponseEntity<?> confirmUserAccount(@RequestBody VerificationToken tokenDTO) {
        String confirmationToken = tokenDTO.getConfirmationToken();
        ApiResponse response = emailService.confirmEmail(confirmationToken);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }



    @PostMapping("/regenerate-verification-token")
    public ResponseEntity<ApiResponse> regenerateVerificationTokenAndSendEmail(@RequestParam("email") String email) {
        ApiResponse response = userService.regenerateVerificationTokenAndSendEmail(email);
        return ResponseEntity.ok(response);
    }



    @PostMapping("/resetPassword")
    public ApiResponse resetPassword(@RequestBody @Valid ResetPasswordRequest request, @RequestParam String email) {
        passwordValidator.isValid(request);
        return userService.resetPassword(request, email);
    }



    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request, @RequestHeader("Authorization") String authorizationHeader) throws CommonApplicationException{
        log.info("Received request with Authorization Header: {}", authorizationHeader);
        ApiResponse<String> response = userService.logout(request, authorizationHeader);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }



}
