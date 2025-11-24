package com.project.PriceTracker.security;

import com.project.PriceTracker.model.Users;
import com.project.PriceTracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MyUserDetailService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    private MyUserPrincipal userPrincipal;

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = userRepository.findByEmail(username);
        if (user == null){
            throw new UsernameNotFoundException("User not found with username "+username);
        }
        return new MyUserPrincipal(user);
    }


}
