package com.expenseTracker.service;

import com.expenseTracker.dto.CardDTO;
import com.expenseTracker.entity.Card;
import com.expenseTracker.entity.Transaction;
import com.expenseTracker.exception.ResourceNotFoundException;
import com.expenseTracker.repository.CardRepository;
import com.expenseTracker.repository.TransactionRepository;
import com.expenseTracker.util.JwtProvider;

import org.modelmapper.ModelMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardService {

    private static final Logger log =
            LoggerFactory.getLogger(CardService.class);

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private ModelMapper modelMapper;

    public CardDTO createCard(CardDTO cardDTO) {

        Long userId = jwtProvider.getCurrentUserId();

        Card card = modelMapper.map(cardDTO, Card.class);

        card.setUserId(userId);

        Card savedCard = cardRepository.save(card);

        log.info(
                "Card created successfully: {}",
                savedCard.getId()
        );

        return modelMapper.map(savedCard, CardDTO.class);
    }

    public List<CardDTO> getAllCards() {

        Long userId = jwtProvider.getCurrentUserId();

        List<Card> cards =
                cardRepository.findByUserId(userId);

        return cards.stream()
                .map(card ->
                        modelMapper.map(card, CardDTO.class)
                )
                .toList();
    }

    public CardDTO getCardById(Long cardId) {

        Long userId = jwtProvider.getCurrentUserId();

        Card card =
                cardRepository.findByIdAndUserId(
                                cardId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Card",
                                        "id",
                                        cardId
                                )
                        );

        return modelMapper.map(card, CardDTO.class);
    }

    public List<Transaction> getCardTransactions(Long cardId) {

        Long userId = jwtProvider.getCurrentUserId();

        cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Card",
                                "id",
                                cardId
                        )
                );

        return transactionRepository
                .findByUserIdAndCardId(userId, cardId);
    }

    public void deleteCard(Long cardId) {

        Long userId = jwtProvider.getCurrentUserId();

        Card card =
                cardRepository.findByIdAndUserId(
                                cardId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Card",
                                        "id",
                                        cardId
                                )
                        );

        cardRepository.delete(card);

        log.info(
                "Card deleted successfully: {}",
                cardId
        );
    }

    public CardDTO updateCard(Long cardId, CardDTO cardDTO) {

        Long userId = jwtProvider.getCurrentUserId();

        Card card =
                cardRepository.findByIdAndUserId(
                                cardId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Card",
                                        "id",
                                        cardId
                                )
                        );

        card.setName(cardDTO.getName());

        Card updatedCard = cardRepository.save(card);

        log.info(
                "Card updated successfully: {}",
                updatedCard.getId()
        );

        return modelMapper.map(updatedCard, CardDTO.class);
    }
}