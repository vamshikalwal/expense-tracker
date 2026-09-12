package com.expenseTracker.service;

import com.expenseTracker.dto.AuthResponse;
import com.expenseTracker.dto.LoginRequest;
import com.expenseTracker.dto.RegisterRequest;
import com.expenseTracker.entity.User;
import com.expenseTracker.exception.DuplicateResourceException;
import com.expenseTracker.exception.UnauthorizedException;
import com.expenseTracker.repository.UserRepository;
import com.expenseTracker.security.JwtUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log =
            LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email already in use"
            );
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        User savedUser = userRepository.save(user);

        log.info(
                "User registered successfully: {}",
                savedUser.getEmail()
        );

        AuthResponse response = new AuthResponse();

        response.setToken(
                "User registered successfully. Please login."
        );

        response.setId(savedUser.getId());
        response.setName(savedUser.getName());
        response.setEmail(savedUser.getEmail());

        return response;
    }

    public AuthResponse login(LoginRequest request) {

        try {

            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.getEmail(),
                                    request.getPassword()
                            )
                    );

            String token =
                    jwtUtil.generateToken(authentication);

            User user =
                    userRepository.findByEmail(
                                    request.getEmail()
                            )
                            .orElseThrow(() ->
                                    new UnauthorizedException(
                                            "User not found"
                                    )
                            );

            log.info(
                    "User logged in successfully: {}",
                    request.getEmail()
            );

            AuthResponse response = new AuthResponse();

            response.setToken(token);
            response.setId(user.getId());
            response.setName(user.getName());
            response.setEmail(user.getEmail());

            return response;

        } catch (Exception e) {

            throw new UnauthorizedException(
                    "Invalid email or password"
            );
        }
    }
}
