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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    private static final Long USER_ID = 12L;
    private static final Long BUDGET_ID = 34L;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private BudgetService budgetService;

    @Test
    void createBudget_shouldNormalizeAndEnrichBudget() {
        LocalDate requestedMonth = LocalDate.of(2026, 1, 20);
        LocalDate normalizedMonth = LocalDate.of(2026, 1, 1);
        BudgetDTO request = buildBudgetDTO("  Food  ", 100.0, requestedMonth);
        Budget budget = new Budget();
        budget.setId(BUDGET_ID);
        BudgetDTO response = new BudgetDTO();

        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(modelMapper.map(request, Budget.class)).thenReturn(budget);
        when(budgetRepository.save(budget)).thenReturn(budget);
        when(transactionRepository.findByUserIdAndDateBetween(
                USER_ID, normalizedMonth, LocalDate.of(2026, 1, 31)))
                .thenReturn(List.of(
                        buildTransaction("food", 95.0, LocalDate.of(2026, 1, 5)),
                        buildTransaction("Transport", 40.0, LocalDate.of(2026, 1, 6))
                ));
        when(modelMapper.map(budget, BudgetDTO.class)).thenReturn(response);

        BudgetDTO result = budgetService.createBudget(request);

        assertThat(budget.getCategory()).isEqualTo("Food");
        assertThat(budget.getBudgetMonth()).isEqualTo(normalizedMonth);
        assertThat(budget.getMonthlyLimit()).isEqualTo(100.0);
        assertThat(budget.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getSpentAmount()).isEqualTo(95.0);
        assertThat(result.getRemainingAmount()).isEqualTo(5.0);
        assertThat(result.getUsagePercentage()).isEqualTo(95.0);
        assertThat(result.getStatus()).isEqualTo("WARNING");
        assertThat(result.getAlertLevel()).isEqualTo("THRESHOLD_90");
    }

    @Test
    void createBudget_shouldRejectDuplicateCategoryAndMonth() {
        BudgetDTO request = buildBudgetDTO("Food", 100.0, LocalDate.of(2026, 1, 15));
        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(budgetRepository.existsByUserIdAndCategoryAndBudgetMonth(
                USER_ID, "Food", LocalDate.of(2026, 1, 1)))
                .thenReturn(true);

        assertThatThrownBy(() -> budgetService.createBudget(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void createBudget_shouldRejectBlankCategory() {
        BudgetDTO request = buildBudgetDTO("  ", 100.0, LocalDate.of(2026, 1, 1));
        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);

        assertThatThrownBy(() -> budgetService.createBudget(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Category is required");

        verify(budgetRepository, never()).existsByUserIdAndCategoryAndBudgetMonth(
                any(), any(), any());
    }

    @Test
    void getBudgets_shouldFilterByNormalizedMonth() {
        LocalDate requestedMonth = LocalDate.of(2026, 2, 20);
        LocalDate normalizedMonth = LocalDate.of(2026, 2, 1);
        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(budgetRepository.findByUserIdAndBudgetMonth(USER_ID, normalizedMonth))
                .thenReturn(List.of());

        assertThat(budgetService.getBudgets(requestedMonth)).isEmpty();

        verify(budgetRepository).findByUserIdAndBudgetMonth(USER_ID, normalizedMonth);
    }

    @Test
    void getBudgetsWithoutMonth_shouldSortBudgetsByMonthDescending() {
        Budget januaryBudget = buildBudget(BUDGET_ID, "Food", 100.0, LocalDate.of(2026, 1, 1));
        Budget februaryBudget = buildBudget(35L, "Food", 100.0, LocalDate.of(2026, 2, 1));
        BudgetDTO januaryResponse = new BudgetDTO();
        januaryResponse.setId(BUDGET_ID);
        BudgetDTO februaryResponse = new BudgetDTO();
        februaryResponse.setId(35L);

        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(budgetRepository.findByUserIdOrderByBudgetMonthDesc(USER_ID))
                .thenReturn(List.of(januaryBudget, februaryBudget));
        when(transactionRepository.findByUserIdAndDateBetween(
                USER_ID, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)))
                .thenReturn(List.of());
        when(transactionRepository.findByUserIdAndDateBetween(
                USER_ID, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28)))
                .thenReturn(List.of());
        when(modelMapper.map(januaryBudget, BudgetDTO.class)).thenReturn(januaryResponse);
        when(modelMapper.map(februaryBudget, BudgetDTO.class)).thenReturn(februaryResponse);

        assertThat(budgetService.getBudgets(null))
                .extracting(BudgetDTO::getId)
                .containsExactly(35L, BUDGET_ID);
    }

    @Test
    void getBudgetById_shouldRejectBudgetNotOwnedByCurrentUser() {
        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(budgetRepository.findByUserIdAndId(USER_ID, BUDGET_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.getBudgetById(BUDGET_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(transactionRepository, never()).findByUserIdAndDateBetween(any(), any(), any());
    }

    @Test
    void updateBudget_shouldRejectDuplicateCategoryAndMonth() {
        Budget existingBudget = buildBudget(BUDGET_ID, "Food", 100.0, LocalDate.of(2026, 1, 1));
        BudgetDTO request = buildBudgetDTO("Travel", 150.0, LocalDate.of(2026, 2, 15));
        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(budgetRepository.findByUserIdAndId(USER_ID, BUDGET_ID))
                .thenReturn(Optional.of(existingBudget));
        when(budgetRepository.existsByUserIdAndCategoryAndBudgetMonth(
                USER_ID, "Travel", LocalDate.of(2026, 2, 1)))
                .thenReturn(true);

        assertThatThrownBy(() -> budgetService.updateBudget(BUDGET_ID, request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void deleteBudget_shouldDeleteOwnedBudget() {
        Budget budget = buildBudget(BUDGET_ID, "Food", 100.0, LocalDate.of(2026, 1, 1));
        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(budgetRepository.findByUserIdAndId(USER_ID, BUDGET_ID)).thenReturn(Optional.of(budget));

        budgetService.deleteBudget(BUDGET_ID);

        verify(budgetRepository).delete(budget);
    }

    private BudgetDTO buildBudgetDTO(String category, Double monthlyLimit, LocalDate budgetMonth) {
        BudgetDTO budgetDTO = new BudgetDTO();
        budgetDTO.setCategory(category);
        budgetDTO.setMonthlyLimit(monthlyLimit);
        budgetDTO.setBudgetMonth(budgetMonth);
        return budgetDTO;
    }

    private Budget buildBudget(Long id, String category, Double monthlyLimit, LocalDate budgetMonth) {
        Budget budget = new Budget();
        budget.setId(id);
        budget.setCategory(category);
        budget.setMonthlyLimit(monthlyLimit);
        budget.setBudgetMonth(budgetMonth);
        budget.setUserId(USER_ID);
        return budget;
    }

    private Transaction buildTransaction(String category, Double amount, LocalDate date) {
        Transaction transaction = new Transaction();
        transaction.setCategory(category);
        transaction.setAmount(amount);
        transaction.setDate(date);
        transaction.setUserId(USER_ID);
        return transaction;
    }
}