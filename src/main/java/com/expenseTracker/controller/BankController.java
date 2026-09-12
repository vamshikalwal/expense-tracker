package com.expenseTracker.controller;

import com.expenseTracker.dto.BankDTO;
import com.expenseTracker.entity.Transaction;
import com.expenseTracker.service.BankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/banks")
@Tag(name = "Bank Management", description = "APIs for managing user banks")
@SecurityRequirement(name = "Bearer Authentication")
public class BankController {

    @Autowired
    private BankService bankService;

    @PostMapping
    @Operation(summary = "Create a new bank")
    public ResponseEntity<BankDTO> createBank(@Valid @RequestBody BankDTO bankDTO) {
        BankDTO createdBank = bankService.createBank(bankDTO);
        return new ResponseEntity<>(createdBank, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all banks for logged-in user")
    public ResponseEntity<List<BankDTO>> getAllBanks() {
        List<BankDTO> banks = bankService.getAllBanks();
        return new ResponseEntity<>(banks, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bank by ID")
    public ResponseEntity<BankDTO> getBankById(@PathVariable Long id) {
        BankDTO bank = bankService.getBankById(id);
        return new ResponseEntity<>(bank, HttpStatus.OK);
    }

    @GetMapping("/{id}/transactions")
    @Operation(summary = "Get all transactions for a specific bank")
    public ResponseEntity<List<Transaction>> getBankTransactions(@PathVariable Long id) {
        List<Transaction> transactions = bankService.getBankTransactions(id);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update bank details")
    public ResponseEntity<BankDTO> updateBank(@PathVariable Long id, @Valid @RequestBody BankDTO bankDTO) {
        BankDTO updatedBank = bankService.updateBank(id, bankDTO);
        return new ResponseEntity<>(updatedBank, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a bank")
    public ResponseEntity<Void> deleteBank(@PathVariable Long id) {
        bankService.deleteBank(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
