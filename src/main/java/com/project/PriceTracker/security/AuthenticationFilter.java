/*package com.project.PriceTracker.security;

import com.project.PriceTracker.config.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {

    private JWTService jwtService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
       final String authHeader = request.getHeader("Authorization");
       final String jwtToken;
       final String userEmail;
       if (authHeader == null|| !authHeader.startsWith("Bearer ")){
           filterChain.doFilter(request, response);
           return;
       }
       jwtToken = authHeader.substring(7);
       //userEmail = jwtService.extractEmail(authHeader); //extract the userEmail from JWT Token

    }
}
*/