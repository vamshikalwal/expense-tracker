package com.expenseTracker.service;

import com.expenseTracker.dto.DailyReportDTO;
import com.expenseTracker.entity.Transaction;
import com.expenseTracker.repository.BankRepository;
import com.expenseTracker.repository.CardRepository;
import com.expenseTracker.repository.TransactionRepository;
import com.expenseTracker.util.JwtProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private BankRepository bankRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private BudgetService budgetService;

    @InjectMocks
    private ReportService reportService;

    @Test
    void generateDailyReport_shouldReturnDatesInAscendingOrder() {
        Long userId = 1L;
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 10);

        when(jwtProvider.getCurrentUserId()).thenReturn(userId);
        when(transactionRepository.findByUserIdAndDateBetween(eq(userId), eq(startDate), eq(endDate)))
                .thenReturn(List.of(
                        buildTransaction("Lunch", 50.0, LocalDate.of(2026, 1, 3)),
                        buildTransaction("Rent", 200.0, LocalDate.of(2026, 1, 1)),
                        buildTransaction("Fuel", 30.0, LocalDate.of(2026, 1, 2))
                ));

        List<DailyReportDTO> report = reportService.generateDailyReport(startDate, endDate);

        assertThat(report)
                .extracting(DailyReportDTO::getDate)
                .containsExactly(
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 2),
                        LocalDate.of(2026, 1, 3)
                );
    }

    private Transaction buildTransaction(String category, Double amount, LocalDate date) {
        Transaction transaction = new Transaction();
        transaction.setCategory(category);
        transaction.setAmount(amount);
        transaction.setDate(date);
        transaction.setUserId(1L);
        return transaction;
    }
}
