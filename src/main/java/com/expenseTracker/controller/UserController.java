package com.expenseTracker.controller;

import com.expenseTracker.dto.DeleteAccountRequest;
import com.expenseTracker.dto.ProfileUpdateRequest;
import com.expenseTracker.entity.User;
import com.expenseTracker.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "Current user profile management APIs")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get the current authenticated user")
    public ResponseEntity<User> getCurrentUser() {
        String email = getAuthenticatedEmail();
        return ResponseEntity.ok(userService.getCurrentUser(email));
    }

    @PutMapping("/me")
    @Operation(summary = "Update the authenticated user profile")
    public ResponseEntity<User> updateCurrentUser(@Valid @RequestBody ProfileUpdateRequest request) {
        String email = getAuthenticatedEmail();
        return ResponseEntity.ok(userService.updateProfile(email, request));
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete the authenticated user account")
    public ResponseEntity<Map<String, String>> deleteCurrentUser(@Valid @RequestBody DeleteAccountRequest request) {
        String email = getAuthenticatedEmail();
        userService.deleteAccount(email, request.getPassword());
        return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
    }

    private String getAuthenticatedEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }
        return authentication.getName();
    }
}
