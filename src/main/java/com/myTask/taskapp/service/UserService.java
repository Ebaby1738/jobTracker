package com.myTask.taskapp.service;

import com.myTask.taskapp.dto.requestDto.ResetPasswordRequest;
import com.myTask.taskapp.dto.requestDto.UserLoginRequest;
import com.myTask.taskapp.dto.requestDto.UserRegisterRequest;
import com.myTask.taskapp.dto.responseDto.ApiResponse;
import com.myTask.taskapp.dto.responseDto.LoginResponse;
import com.myTask.taskapp.exceptions.CommonApplicationException;
import jakarta.servlet.http.HttpServletRequest;

public interface UserService {

    ApiResponse registerUser(UserRegisterRequest request);

    ApiResponse<LoginResponse> login(UserLoginRequest loginDTO);

    ApiResponse regenerateVerificationTokenAndSendEmail(String email);

    ApiResponse resetPassword(ResetPasswordRequest request, String email);

    ApiResponse<String> logout(HttpServletRequest request, String authorizationHeader) throws CommonApplicationException;
}
