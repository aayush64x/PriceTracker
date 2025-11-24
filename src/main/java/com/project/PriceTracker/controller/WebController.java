package com.project.PriceTracker.controller;

import com.project.PriceTracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
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

}
