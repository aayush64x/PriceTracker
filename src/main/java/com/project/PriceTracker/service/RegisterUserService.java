package com.project.PriceTracker.service;

import com.project.PriceTracker.dto.UserDTO;
import com.project.PriceTracker.dto.UserLoginDTO;
import com.project.PriceTracker.model.Users;
import com.project.PriceTracker.repository.UserRepository;
import com.project.PriceTracker.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegisterUserService {

    @Autowired
    public UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    public RegisterUserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Users registerUser(UserDTO userDTO){
        System.out.println("DTO password received: " + userDTO.getPassword());

        System.out.println(userDTO.getEmail());
        Users users = new Users();
        users.setFirstName(userDTO.getFirstName());
        users.setLastName(userDTO.getLastName());
        users.setEmail(userDTO.getEmail());
        users.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        return userRepository.save(users);

    }

    public String verify(UserLoginDTO userLoginDTO){
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(userLoginDTO.getEmail(), userLoginDTO.getPassword()));
        if(authentication.isAuthenticated()){
            return jwtService.generateToken(userLoginDTO.getEmail());
        }
        return "fail to authenticate";
    }
}
