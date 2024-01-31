package com.myTask.taskapp.service.serviceImplementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myTask.taskapp.dto.requestDto.ResetPasswordRequest;
import com.myTask.taskapp.dto.requestDto.UserLoginRequest;
import com.myTask.taskapp.dto.requestDto.UserRegisterRequest;
import com.myTask.taskapp.dto.responseDto.ApiResponse;
import com.myTask.taskapp.dto.responseDto.LoginResponse;
import com.myTask.taskapp.emails.EmailService;
import com.myTask.taskapp.entity.User;
import com.myTask.taskapp.entity.VerificationToken;
import com.myTask.taskapp.enums.Role;
import com.myTask.taskapp.exceptions.CommonApplicationException;
import com.myTask.taskapp.exceptions.UserExistException;
import com.myTask.taskapp.repository.TokenRepository;
import com.myTask.taskapp.repository.UserRepository;
import com.myTask.taskapp.security.JWTService;
import com.myTask.taskapp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final JWTService jwtService;
    private final RabbitTemplate rabbitTemplate;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final EmailService emailService;




    @Override
    public ApiResponse registerUser(UserRegisterRequest request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
        if (optionalUser.isPresent()) {
            return new ApiResponse("00", "User Already Exist", HttpStatus.ALREADY_REPORTED, "success");
        }
        User newUser = new User();
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setFullName(request.getFullName());
        newUser.setPhoneNumber(request.getPhoneNumber());
        newUser.setRole(Role.APP_USER);
        newUser.setIsVerified(false);
        newUser.setEmail(request.getEmail());

        User savedUser = userRepository.save(newUser);
        log.info("user saved to database... about generating email");
        VerificationToken confirmationToken = new VerificationToken(savedUser);
        tokenRepository.save(confirmationToken);
        log.info("verification token generated...");
        String confirmationLink = confirmationToken.getConfirmationToken();

        emailService.sendConfirmationEmail(savedUser, confirmationLink);
        //todo: push to queue..
        log.info("email sent successfully " + confirmationLink);


        ApiResponse genericResponse = new ApiResponse<>();
        genericResponse.setMessage("Registration Successful, Please check your email to verify your account");
        genericResponse.setStatus("Success");
        genericResponse.setCode("00");
        return genericResponse;
    }



    @Override
    public ApiResponse<LoginResponse> login(UserLoginRequest loginDTO) {
        log.info("Request to login at the service layer");

        Authentication authenticationUser;
        try {
            authenticationUser = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword())
            );
            log.info("Authenticated the User by the Authentication manager");
        } catch (DisabledException es) {
            return Stream.of(
                            new AbstractMap.SimpleEntry<>("message", "Disabled exception occurred"),
                            new AbstractMap.SimpleEntry<>("status", "failure"),
                            new AbstractMap.SimpleEntry<>("httpStatus", HttpStatus.BAD_REQUEST)
                    )
                    .collect(
                            Collectors.collectingAndThen(
                                    Collectors.toMap(AbstractMap.SimpleEntry::getKey, entry -> entry.getValue()),
                                    map -> new ApiResponse<>((Map<String, String>) map)
                            )
                    );
        } catch (BadCredentialsException e) {
            throw new UserExistException("Invalid email or password", HttpStatus.BAD_REQUEST);
        }
        // Tell securityContext that this user is in the context
        SecurityContextHolder.getContext().setAuthentication(authenticationUser);
        // Retrieve the user from the repository
        User appUser = userRepository.findByEmail(loginDTO.getEmail()).orElseThrow(() ->
                new UserExistException("User not found", HttpStatus.BAD_REQUEST));
        // Update the lastLoginDate field
        appUser.setLastLogin(LocalDateTime.now());
        log.info("last-login date updated");
        // Save the updated user entity
        User user = userRepository.save(appUser);
        log.info("user saved back to database");
        // Generate and send token
        String tokenGenerated = "Bearer " + jwtService.generateToken(authenticationUser, user.getRole());
        log.info("Jwt token generated for the user " + tokenGenerated);
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(tokenGenerated);
        ApiResponse<LoginResponse> apiResponse = new ApiResponse<>("00", "Success", loginResponse, "Successfully logged in", HttpStatus.OK);
        apiResponse.setData(loginResponse);
        return apiResponse;
    }



    @Override
    public ApiResponse regenerateVerificationTokenAndSendEmail(String email) {
        // Find the user by email
        log.info("Regeneration started");
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            log.info("User not in database");
        }
        User existingUser = optionalUser.get();
        log.info("user found in database" + existingUser.getEmail());
        verifyOTPGenerate(existingUser);
        ApiResponse genericResponse = new ApiResponse();
        genericResponse.setMessage("Token resent successfully, Please check your email to verify your account");
        genericResponse.setStatus("Success");
        genericResponse.setCode("00");
        genericResponse.setHttpStatus(HttpStatus.OK);
        return genericResponse;
    }

    @Override
    public ApiResponse resetPassword(ResetPasswordRequest request, String email) {
        log.info("Resetting Password");
        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user = optionalUser.get();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        ApiResponse response = new ApiResponse();
        response.setMessage("Password changed successfully");
        response.setStatus("Success");
        response.setCode("200");
        response.setHttpStatus(HttpStatus.OK);
        return response;
    }



    @Override
    public ApiResponse<String> logout(HttpServletRequest request, String authorizationHeader) throws CommonApplicationException {
        String token = authorizationHeader.substring(7);
        Map<String, String> userDetails = jwtService.validateTokenAndReturnDetail(token);
        log.info("Request to logout");
        try {
            SecurityContextHolder.getContext().setAuthentication(null);
            return new ApiResponse<>("00", "Success", "Successfully logged out", "You have been logged out", HttpStatus.OK);
        } catch (Exception e) {
            return new ApiResponse<>("01", "Failure", "Logout failed", "An error occurred while logging out", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    public void verifyOTPGenerate(User user) {
        Optional<VerificationToken> optionalToken = tokenRepository.findTokenByEmail(user.getEmail());
        if (optionalToken.isEmpty()) {
            log.info("No existing token found for the user");
        } else {
            VerificationToken existingToken = optionalToken.get();
            log.info("Existing token retrieved: " + existingToken.getConfirmationToken());
            tokenRepository.delete(existingToken);
            log.info("Existing token deleted");
            VerificationToken newToken = new VerificationToken(user);
            tokenRepository.save(newToken);
            log.info("New token generated: " + newToken);
            user.setVerificationToken(newToken);
            String confirmationLink = user.getVerificationToken().getConfirmationToken();

            emailService.sendConfirmationEmail(user, confirmationLink);
            log.info("Email sent successfully" + confirmationLink);
        }
    }



}
