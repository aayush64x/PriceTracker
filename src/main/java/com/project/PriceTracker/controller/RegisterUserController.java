package com.project.PriceTracker.controller;

import com.project.PriceTracker.dto.UserDTO;
import com.project.PriceTracker.service.RegisterUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/register")
public class RegisterUserController{
    private RegisterUserService registerUserService;

    public RegisterUserController(RegisterUserService registerUserService){
        this.registerUserService = registerUserService;
    }

    @PostMapping
    public ResponseEntity<String> registerUser(@RequestBody UserDTO userDTO){
        registerUserService.registerUser(userDTO);
        return ResponseEntity.ok("User Registered Successfully");
    }

}
