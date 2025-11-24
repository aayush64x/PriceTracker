package com.project.PriceTracker.controller;

import com.project.PriceTracker.dto.UserDTO;
import com.project.PriceTracker.dto.UserLoginDTO;
import com.project.PriceTracker.service.RegisterUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping
public class RegisterUserController{
    private RegisterUserService registerUserService;

    public RegisterUserController(RegisterUserService registerUserService){
        this.registerUserService = registerUserService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserDTO userDTO) {
        try {
            registerUserService.registerUser(userDTO);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User Registered Successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDTO userLoginDTO) {
        try {
            String token = registerUserService.verify(userLoginDTO);

            if ("fail to authenticate".equals(token)) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid email or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            // Return just the token string (frontend expects response.data to be the token)
            return ResponseEntity.ok(token);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Authentication failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
}
