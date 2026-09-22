package com.expenseTracker.service;
import com.expenseTracker.dto.BudgetDTO;
import com.expenseTracker.dto.CardWiseDTO;
import com.expenseTracker.dto.CategoryWiseDTO;
import com.expenseTracker.dto.DailyReportDTO;
import com.expenseTracker.entity.Bank;
import com.expenseTracker.entity.Card;
import com.expenseTracker.entity.Transaction;
import com.expenseTracker.repository.BankRepository;
import com.expenseTracker.repository.CardRepository;
import com.expenseTracker.repository.TransactionRepository;
import com.expenseTracker.util.JwtProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private static final Logger log =
            LoggerFactory.getLogger(ReportService.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private BudgetService budgetService;

    public Map<String, Object> generateSummaryReport(String period) {

        Long userId = jwtProvider.getCurrentUserId();

        LocalDate startDate;
        LocalDate endDate = LocalDate.now();

        String effectivePeriod = period != null ? period.toLowerCase() : "monthly";

        switch (effectivePeriod) {
            case "daily":
                startDate = LocalDate.now();
                break;
            case "weekly":
                startDate = LocalDate.now().minusWeeks(1);
                break;
            case "monthly":
                startDate = LocalDate.now().minusMonths(1);
                break;
            case "yearly":
                startDate = LocalDate.now().minusYears(1);
                break;
            default:
                startDate = LocalDate.now().minusMonths(1);
        }

        Double totalAmount = transactionRepository.getTotalAmountByUserIdAndDateRange(
                userId,
                startDate,
                endDate
        );

        List<Transaction> transactions = transactionRepository.findByUserIdAndDateBetween(
                userId,
                startDate,
                endDate
        );

        List<Card> cards = cardRepository.findByUserId(userId);
        List<Bank> banks = bankRepository.findByUserId(userId);

        List<Map<String, Object>> cardSummary = cards.stream()
                .map(card -> {
                    Double totalCardAmount = transactions.stream()
                            .filter(t -> card.getId().equals(t.getCardId()))
                            .mapToDouble(Transaction::getAmount)
                            .sum();

                    Map<String, Object> cardMap = new HashMap<>();
                    cardMap.put("id", card.getId());
                    cardMap.put("name", card.getName());
                    cardMap.put("total", totalCardAmount);

                    return cardMap;
                })
                .collect(Collectors.toList());

        List<Map<String, Object>> bankSummary = banks.stream()
                .map(bank -> {
                    Double totalBankAmount = transactions.stream()
                            .filter(t -> bank.getId().equals(t.getBankId()))
                            .mapToDouble(Transaction::getAmount)
                            .sum();

                    Map<String, Object> bankMap = new HashMap<>();
                    bankMap.put("id", bank.getId());
                    bankMap.put("name", bank.getName());
                    bankMap.put("total", totalBankAmount);

                    return bankMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> report = new HashMap<>();
        report.put("cardSummary", cardSummary);
        report.put("bankSummary", bankSummary);
        report.put("totalCards", cardSummary.stream()
                .mapToDouble(item -> ((Number) item.get("total")).doubleValue())
                .sum());
        report.put("totalBanks", bankSummary.stream()
                .mapToDouble(item -> ((Number) item.get("total")).doubleValue())
                .sum());
        report.put("totalExpense", totalAmount != null ? totalAmount : 0D);
        report.put("transactionCount", (long) transactions.size());

        return report;
    }

    public List<CategoryWiseDTO> generateCategoryWiseReport() {

        Long userId = jwtProvider.getCurrentUserId();

        List<Transaction> transactions = transactionRepository.findByUserId(userId);

        return transactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)
                ))
                .entrySet()
                .stream()
                .map(entry -> {
                    CategoryWiseDTO dto = new CategoryWiseDTO();
                    dto.setCategory(entry.getKey());
                    dto.setTotalAmount(entry.getValue());
                    dto.setCount(
                            transactions.stream()
                                    .filter(t -> t.getCategory().equals(entry.getKey()))
                                    .count()
                    );
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<CardWiseDTO> generateCardWiseReport() {

        Long userId = jwtProvider.getCurrentUserId();

        List<Card> cards = cardRepository.findByUserId(userId);
        List<Transaction> transactions = transactionRepository.findByUserId(userId);

        return cards.stream()
                .map(card -> {
                    Double totalAmount = transactions.stream()
                            .filter(t -> card.getId().equals(t.getCardId()))
                            .mapToDouble(Transaction::getAmount)
                            .sum();

                    long count = transactions.stream()
                            .filter(t -> card.getId().equals(t.getCardId()))
                            .count();

                    CardWiseDTO dto = new CardWiseDTO();
                    dto.setCardId(card.getId());
                    dto.setCardName(card.getName());
                    dto.setTotalAmount(totalAmount);
                    dto.setTransactionCount(count);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<DailyReportDTO> generateDailyReport(LocalDate startDate, LocalDate endDate) {

        Long userId = jwtProvider.getCurrentUserId();

        List<Transaction> transactions = transactionRepository.findByUserIdAndDateBetween(
                userId,
                startDate,
                endDate
        );

        return transactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getDate,
                        Collectors.summingDouble(Transaction::getAmount)
                ))
                .entrySet()
                .stream()
                .map(entry -> {
                    DailyReportDTO dto = new DailyReportDTO();
                    dto.setDate(entry.getKey());
                    dto.setTotalAmount(entry.getValue());
                    dto.setTransactionCount(
                            transactions.stream()
                                    .filter(t -> t.getDate().equals(entry.getKey()))
                                    .count()
                    );
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<BudgetDTO> generateBudgetSummary(LocalDate budgetMonth) {
        return budgetService.getBudgets(budgetMonth);
    }
}
