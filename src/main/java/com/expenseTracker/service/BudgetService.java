package com.expenseTracker.service;

import com.expenseTracker.dto.BudgetDTO;
import com.expenseTracker.entity.Budget;
import com.expenseTracker.entity.Transaction;
import com.expenseTracker.exception.BadRequestException;
import com.expenseTracker.exception.DuplicateResourceException;
import com.expenseTracker.exception.ResourceNotFoundException;
import com.expenseTracker.repository.BudgetRepository;
import com.expenseTracker.repository.TransactionRepository;
import com.expenseTracker.util.JwtProvider;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private ModelMapper modelMapper;

    public BudgetDTO createBudget(BudgetDTO budgetDTO) {
        Long userId = jwtProvider.getCurrentUserId();
        String category = normalizeCategory(budgetDTO.getCategory());
        LocalDate budgetMonth = normalizeMonth(budgetDTO.getBudgetMonth());

        if (budgetRepository.existsByUserIdAndCategoryAndBudgetMonth(userId, category, budgetMonth)) {
            throw new DuplicateResourceException(
                    "Budget already exists for category '" + category + "' in month " + budgetMonth
            );
        }

        Budget budget = modelMapper.map(budgetDTO, Budget.class);
        budget.setCategory(category);
        budget.setMonthlyLimit(budgetDTO.getMonthlyLimit());
        budget.setBudgetMonth(budgetMonth);
        budget.setUserId(userId);

        Budget savedBudget = budgetRepository.save(budget);
        return enrichBudget(savedBudget);
    }

    public List<BudgetDTO> getBudgets(LocalDate month) {
        Long userId = jwtProvider.getCurrentUserId();

        List<Budget> budgets;
        if (month != null) {
            LocalDate normalizedMonth = normalizeMonth(month);
            budgets = budgetRepository.findByUserIdAndBudgetMonth(userId, normalizedMonth);
        } else {
            budgets = budgetRepository.findByUserIdOrderByBudgetMonthDesc(userId);
        }

        return budgets.stream()
                .sorted(Comparator.comparing(Budget::getBudgetMonth).reversed())
                .map(this::enrichBudget)
                .toList();
    }

    public BudgetDTO getBudgetById(Long budgetId) {
        Long userId = jwtProvider.getCurrentUserId();

        Budget budget = budgetRepository.findByUserIdAndId(userId, budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", budgetId));

        return enrichBudget(budget);
    }

    public BudgetDTO updateBudget(Long budgetId, BudgetDTO budgetDTO) {
        Long userId = jwtProvider.getCurrentUserId();

        Budget budget = budgetRepository.findByUserIdAndId(userId, budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", budgetId));

        String category = normalizeCategory(budgetDTO.getCategory());
        LocalDate budgetMonth = normalizeMonth(budgetDTO.getBudgetMonth());

        if (!budget.getCategory().equals(category) || !budget.getBudgetMonth().equals(budgetMonth)) {
            boolean duplicateExists = budgetRepository.existsByUserIdAndCategoryAndBudgetMonth(userId, category, budgetMonth);
            if (duplicateExists) {
                throw new DuplicateResourceException(
                        "Budget already exists for category '" + category + "' in month " + budgetMonth
                );
            }
        }

        budget.setCategory(category);
        budget.setMonthlyLimit(budgetDTO.getMonthlyLimit());
        budget.setBudgetMonth(budgetMonth);

        Budget updatedBudget = budgetRepository.save(budget);
        return enrichBudget(updatedBudget);
    }

    public void deleteBudget(Long budgetId) {
        Long userId = jwtProvider.getCurrentUserId();

        Budget budget = budgetRepository.findByUserIdAndId(userId, budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", budgetId));

        budgetRepository.delete(budget);
    }

    private BudgetDTO enrichBudget(Budget budget) {
        Long userId = budget.getUserId();
        LocalDate startOfMonth = budget.getBudgetMonth().withDayOfMonth(1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        List<Transaction> transactions = transactionRepository.findByUserIdAndDateBetween(
                userId,
                startOfMonth,
                endOfMonth
        );

        double spentAmount = transactions.stream()
                .filter(transaction -> budget.getCategory().equalsIgnoreCase(transaction.getCategory()))
                .mapToDouble(Transaction::getAmount)
                .sum();

        double limit = budget.getMonthlyLimit();
        double remainingAmount = limit - spentAmount;
        double usagePercentage = limit > 0 ? (spentAmount / limit) * 100 : 0;
        String status = resolveStatus(usagePercentage);
        String alertLevel = resolveAlertLevel(usagePercentage);

        BudgetDTO budgetDTO = modelMapper.map(budget, BudgetDTO.class);
        budgetDTO.setSpentAmount(spentAmount);
        budgetDTO.setRemainingAmount(remainingAmount);
        budgetDTO.setUsagePercentage(Math.round(usagePercentage * 100.0) / 100.0);
        budgetDTO.setStatus(status);
        budgetDTO.setAlertLevel(alertLevel);
        budgetDTO.setRecommendation(buildRecommendation(budget.getCategory(), spentAmount, limit, usagePercentage));

        return budgetDTO;
    }

    private String resolveStatus(double usagePercentage) {
        if (usagePercentage >= 100.0) {
            return "OVER_BUDGET";
        }
        if (usagePercentage >= 70.0) {
            return "WARNING";
        }
        return "SAFE";
    }

    private String resolveAlertLevel(double usagePercentage) {
        if (usagePercentage >= 100.0) {
            return "OVER_BUDGET";
        }
        if (usagePercentage >= 90.0) {
            return "THRESHOLD_90";
        }
        if (usagePercentage >= 70.0) {
            return "THRESHOLD_70";
        }
        return "NONE";
    }

    private String buildRecommendation(String category, double spentAmount, double limit, double usagePercentage) {
        if (usagePercentage >= 100.0) {
            double overage = spentAmount - limit;
            double reductionNeeded = (overage / Math.max(limit, 1.0)) * 100;
            return String.format(
                    "You are over your %s budget by %.2f. Reduce spending by %.0f%% for the rest of the month.",
                    category,
                    overage,
                    Math.max(10, reductionNeeded + 10)
            );
        }
        if (usagePercentage >= 90.0) {
            return String.format(
                    "You are close to your %s limit. Reduce %s spending by 15%% for the remaining days of the month.",
                    category,
                    category
            );
        }
        if (usagePercentage >= 70.0) {
            return String.format(
                    "You are approaching your %s budget. Consider reducing non-essential spending this month.",
                    category
            );
        }
        double remaining = Math.max(limit - spentAmount, 0.0);
        return String.format(
                "You still have %.2f left in your %s budget. Keep spending under control.",
                remaining,
                category
        );
    }

    private String normalizeCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new BadRequestException("Category is required");
        }
        return category.trim();
    }

    private LocalDate normalizeMonth(LocalDate month) {
        if (month == null) {
            return LocalDate.now().withDayOfMonth(1);
        }
        return month.withDayOfMonth(1);
    }
}
