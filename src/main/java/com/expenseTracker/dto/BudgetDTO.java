package com.expenseTracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetDTO {
    private Long id;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Monthly limit is required")
    @Positive(message = "Monthly limit must be positive")
    private Double monthlyLimit;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate budgetMonth;

    private Double spentAmount;
    private Double remainingAmount;
    private Double usagePercentage;
    private String status;
    private String alertLevel;
    private String recommendation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
