package com.project.PriceTracker.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = 7;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ApplicationContext context;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        // Exclude public endpoints from JWT validation
        return path.equals("/login") ||
                path.equals("/register") ||
                path.startsWith("/public/") ||
                path.equals("/error"); // Important: exclude error endpoint
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // No Authorization header - let Spring Security handle it (will return 401/403)
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(BEARER_PREFIX_LENGTH);

            // Extract email - this will throw ExpiredJwtException if token is expired
            String email = jwtService.extractEmail(token);

            // Only proceed if:
            // 1. Email was successfully extracted
            // 2. No authentication is already set (avoid redundant processing)
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Validate token BEFORE loading user details
                if (jwtService.isTokenValid(token, email)) {

                    try {
                        UserDetails userDetails = context.getBean(UserDetailsService.class)
                                .loadUserByUsername(email);

                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                    } catch (UsernameNotFoundException e) {
                        // Token is valid but user no longer exists in database
                        logger.warn("Valid JWT token for non-existent user: {}", email);
                        // Don't set authentication - request will be rejected by Spring Security
                    }
                } else {
                    // Token is invalid (signature mismatch, tampered, etc.)
                    logger.debug("Invalid JWT token for user: {}", email);
                }
            }

        } catch (ExpiredJwtException e) {
            logger.debug("Expired JWT token: {}", e.getMessage());
            // Don't set authentication - Spring Security will reject with 401/403

        } catch (JwtException e) {
            logger.warn("JWT processing error: {}", e.getMessage());
            // Malformed token, invalid signature, etc.
            // Don't set authentication - Spring Security will reject

        } catch (Exception e) {
            logger.error("Unexpected error processing JWT: {}", e.getMessage(), e);
            // Don't set authentication - fail securely
        }

        // Always continue the filter chain
        // If authentication wasn't set, Spring Security will handle authorization failure
        filterChain.doFilter(request, response);
    }
}