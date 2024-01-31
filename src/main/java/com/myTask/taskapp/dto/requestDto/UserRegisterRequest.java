package com.myTask.taskapp.dto.requestDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegisterRequest {


    private String fullName;


    @Email
    @NotBlank(message = "email cannot be empty")
    private String email;


    @NotBlank(message = " password cannot be empty")
    @Size(message = "Password must be greater than 6 and less than 20", min = 6, max = 20)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]).{6,20}$", message = "Password must contain at least one lowercase letter, one uppercase letter, and one number")
    private String password;


    @NotBlank(message = "Confirm password cannot be empty")
    private String confirmPassword;


    @NotBlank(message = " phoneNumber cannot be empty")
    @Size(message = "phoneNumber must be atLeast 11",max = 14)
    private String phoneNumber;


}
