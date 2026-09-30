package com.expenseTracker.service;

import com.expenseTracker.dto.ProfileUpdateRequest;
import com.expenseTracker.entity.User;
import com.expenseTracker.exception.BadRequestException;
import com.expenseTracker.exception.DuplicateResourceException;
import com.expenseTracker.exception.ResourceNotFoundException;
import com.expenseTracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public User updateProfile(String email, ProfileUpdateRequest request) {
        User user = getCurrentUser(email);

        if (request == null) {
            return user;
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String nextEmail = request.getEmail().trim();
            if (!nextEmail.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(nextEmail)) {
                throw new DuplicateResourceException("Email already in use");
            }
            user.setEmail(nextEmail);
        }

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
                throw new BadRequestException("Current password is required to change the password");
            }
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new BadRequestException("Current password is incorrect");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(String email, String password) {
        User user = getCurrentUser(email);

        if (password == null || password.isBlank() || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("Password is incorrect");
        }

        userRepository.delete(user);
    }
}
