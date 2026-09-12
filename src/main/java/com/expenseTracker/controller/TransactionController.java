package com.expenseTracker.controller;

import com.expenseTracker.dto.PaginatedResponse;
import com.expenseTracker.dto.TransactionDTO;
import com.expenseTracker.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/transactions")
@Tag(name = "Transaction Management", description = "APIs for managing transactions")
@SecurityRequirement(name = "Bearer Authentication")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Create a new transaction")
    public ResponseEntity<TransactionDTO> createTransaction(@Valid @RequestBody TransactionDTO transactionDTO) {
        TransactionDTO createdTransaction = transactionService.createTransaction(transactionDTO);
        return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all transactions with pagination")
    public ResponseEntity<PaginatedResponse<TransactionDTO>> getAllTransactions(
            @PageableDefault(size = 10, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TransactionDTO> transactionPage = transactionService.getAllTransactionsPaginated(pageable);

        PaginatedResponse<TransactionDTO> response = new PaginatedResponse<>();

        response.setContent(transactionPage.getContent());
        response.setPageNumber(transactionPage.getNumber());
        response.setPageSize(transactionPage.getSize());
        response.setTotalElements(transactionPage.getTotalElements());
        response.setTotalPages(transactionPage.getTotalPages());
        response.setHasNext(transactionPage.hasNext());
        response.setHasPrevious(transactionPage.hasPrevious());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/filter")
    @Operation(summary = "Get transactions by date range, card, or bank")
    public ResponseEntity<List<TransactionDTO>> filterTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long cardId,
            @RequestParam(required = false) Long bankId) {

        List<TransactionDTO> transactions;

        if (startDate != null && endDate != null) {
            transactions = transactionService.getTransactionsByDateRange(startDate, endDate);
        } else if (cardId != null) {
            // Get transactions for specific card
            transactions = transactionService.getAllTransactions();
            transactions = transactions.stream()
                    .filter(t -> cardId.equals(t.getCardId()))
                    .toList();
        } else if (bankId != null) {
            // Get transactions for specific bank
            transactions = transactionService.getAllTransactions();
            transactions = transactions.stream()
                    .filter(t -> bankId.equals(t.getBankId()))
                    .toList();
        } else {
            transactions = transactionService.getAllTransactions();
        }

        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID")
    public ResponseEntity<TransactionDTO> getTransactionById(@PathVariable Long id) {
        TransactionDTO transaction = transactionService.getTransactionById(id);
        return new ResponseEntity<>(transaction, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update transaction details")
    public ResponseEntity<TransactionDTO> updateTransaction(@PathVariable Long id, 
                                                            @Valid @RequestBody TransactionDTO transactionDTO) {
        TransactionDTO updatedTransaction = transactionService.updateTransaction(id, transactionDTO);
        return new ResponseEntity<>(updatedTransaction, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transaction")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
