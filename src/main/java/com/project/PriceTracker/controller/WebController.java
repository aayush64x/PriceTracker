package com.project.PriceTracker.controller;

import com.project.PriceTracker.dto.UserDTO;
import com.project.PriceTracker.model.User;
import com.project.PriceTracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class WebController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping({"/", "home"})
    public String index(){
        return "index";
    }


    @GetMapping("/register")
    public String register(){
        return "register";
    }
    @GetMapping("/api/watchlist")
    public String watchlist(){
        return "watchlist";
    }
    @GetMapping("/hello World")
    public String hello(){
        return "hello world";
    }





}
