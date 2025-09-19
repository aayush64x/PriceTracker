package com.project.PriceTracker.service;

import com.project.PriceTracker.dto.UserDTO;
import com.project.PriceTracker.model.User;
import com.project.PriceTracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

@Service
public class RegisterUserService {

    @Autowired
    public UserRepository userRepository;

    public RegisterUserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User registerUser(UserDTO userDTO){
        System.out.println("DTO password received: " + userDTO.getPassword());

        System.out.println(userDTO.getEmail());
        User user = new User();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        return userRepository.save(user);

    }
}
