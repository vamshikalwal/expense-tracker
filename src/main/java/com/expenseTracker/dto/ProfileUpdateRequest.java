package com.expenseTracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileUpdateRequest {
    private String name;

    @Email(message = "Email should be valid")
    private String email;

    private String currentPassword;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String newPassword;
}
