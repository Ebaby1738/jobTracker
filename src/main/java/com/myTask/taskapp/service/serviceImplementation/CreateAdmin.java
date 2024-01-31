package com.myTask.taskapp.service.serviceImplementation;

import com.myTask.taskapp.entity.Admin;
import com.myTask.taskapp.enums.Role;
import com.myTask.taskapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Configuration
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateAdmin {
    private UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void runAtStart() {
        log.info("Creating admin");
        if(!userRepository.existsByEmail("ebenirene5@gmail.com")) {
            Admin admin = new Admin();
            admin.setEmail("ebenirene5@gmail.com");
            admin.setPassword(passwordEncoder.encode("OneAdmin246"));
            admin.setRole(Role.ADMIN);
            //admin.setCreationDate(LocalDateTime.now());
            admin.setLastLogin(LocalDateTime.now());
            //admin.setPhoneNumber("09039156872");
            admin.setFullName("Ebenezer Irene");
            admin.setIsVerified(true);
            userRepository.save(admin);
        }
    }
}
