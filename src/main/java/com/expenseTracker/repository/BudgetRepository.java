package com.expenseTracker.repository;

import com.expenseTracker.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUserIdOrderByBudgetMonthDesc(Long userId);

    List<Budget> findByUserIdAndBudgetMonth(Long userId, LocalDate budgetMonth);

    Optional<Budget> findByUserIdAndId(Long userId, Long id);

    boolean existsByUserIdAndCategoryAndBudgetMonth(Long userId, String category, LocalDate budgetMonth);
}
