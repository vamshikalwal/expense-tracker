package com.expenseTracker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteAccountRequest {
    @NotBlank(message = "Password is required to delete the account")
    private String password;
}
