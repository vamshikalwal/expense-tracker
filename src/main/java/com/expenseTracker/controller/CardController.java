package com.expenseTracker.controller;

import com.expenseTracker.dto.CardDTO;
import com.expenseTracker.entity.Transaction;
import com.expenseTracker.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cards")
@Tag(name = "Card Management", description = "APIs for managing user cards")
@SecurityRequirement(name = "Bearer Authentication")
public class CardController {

    @Autowired
    private CardService cardService;

    @PostMapping
    @Operation(summary = "Create a new card")
    public ResponseEntity<CardDTO> createCard(@Valid @RequestBody CardDTO cardDTO) {
        CardDTO createdCard = cardService.createCard(cardDTO);
        return new ResponseEntity<>(createdCard, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all cards for logged-in user")
    public ResponseEntity<List<CardDTO>> getAllCards() {
        List<CardDTO> cards = cardService.getAllCards();
        return new ResponseEntity<>(cards, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get card by ID")
    public ResponseEntity<CardDTO> getCardById(@PathVariable Long id) {
        CardDTO card = cardService.getCardById(id);
        return new ResponseEntity<>(card, HttpStatus.OK);
    }

    @GetMapping("/{id}/transactions")
    @Operation(summary = "Get all transactions for a specific card")
    public ResponseEntity<List<Transaction>> getCardTransactions(@PathVariable Long id) {
        List<Transaction> transactions = cardService.getCardTransactions(id);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update card details")
    public ResponseEntity<CardDTO> updateCard(@PathVariable Long id, @Valid @RequestBody CardDTO cardDTO) {
        CardDTO updatedCard = cardService.updateCard(id, cardDTO);
        return new ResponseEntity<>(updatedCard, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a card")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
