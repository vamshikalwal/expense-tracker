package com.expenseTracker.service;

import com.expenseTracker.dto.CardDTO;
import com.expenseTracker.entity.Card;
import com.expenseTracker.entity.Transaction;
import com.expenseTracker.exception.ResourceNotFoundException;
import com.expenseTracker.repository.CardRepository;
import com.expenseTracker.repository.TransactionRepository;
import com.expenseTracker.util.JwtProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    private static final Long USER_ID = 12L;
    private static final Long CARD_ID = 34L;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CardService cardService;

    @Test
    void createCard_shouldAssignCurrentUserAndReturnSavedCard() {
        CardDTO request = new CardDTO();
        request.setName("Visa Gold");
        Card card = new Card();
        card.setName("Visa Gold");
        Card savedCard = new Card();
        savedCard.setId(CARD_ID);
        savedCard.setName("Visa Gold");
        savedCard.setUserId(USER_ID);
        CardDTO response = new CardDTO();
        response.setId(CARD_ID);
        response.setName("Visa Gold");

        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(modelMapper.map(request, Card.class)).thenReturn(card);
        when(cardRepository.save(card)).thenReturn(savedCard);
        when(modelMapper.map(savedCard, CardDTO.class)).thenReturn(response);

        CardDTO result = cardService.createCard(request);

        assertThat(card.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getId()).isEqualTo(CARD_ID);
        assertThat(result.getName()).isEqualTo("Visa Gold");
        verify(cardRepository).save(card);
    }

    @Test
    void getAllCards_shouldReturnOnlyCardsForCurrentUser() {
        Card card = new Card();
        card.setId(CARD_ID);
        CardDTO response = new CardDTO();
        response.setId(CARD_ID);

        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(cardRepository.findByUserId(USER_ID)).thenReturn(List.of(card));
        when(modelMapper.map(card, CardDTO.class)).thenReturn(response);

        List<CardDTO> result = cardService.getAllCards();

        assertThat(result).containsExactly(response);
        verify(cardRepository).findByUserId(USER_ID);
    }

    @Test
    void getCardById_shouldReturnCardOwnedByCurrentUser() {
        Card card = new Card();
        card.setId(CARD_ID);
        CardDTO response = new CardDTO();
        response.setId(CARD_ID);

        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(cardRepository.findByIdAndUserId(CARD_ID, USER_ID)).thenReturn(Optional.of(card));
        when(modelMapper.map(card, CardDTO.class)).thenReturn(response);

        assertThat(cardService.getCardById(CARD_ID)).isSameAs(response);
    }

    @Test
    void getCardById_shouldRejectCardNotOwnedByCurrentUser() {
        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(cardRepository.findByIdAndUserId(CARD_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCardById(CARD_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Card not found with id : '34'");
    }

    @Test
    void getCardTransactions_shouldReturnTransactionsForOwnedCard() {
        Card card = new Card();
        List<Transaction> transactions = List.of(new Transaction());

        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(cardRepository.findByIdAndUserId(CARD_ID, USER_ID)).thenReturn(Optional.of(card));
        when(transactionRepository.findByUserIdAndCardId(USER_ID, CARD_ID)).thenReturn(transactions);

        assertThat(cardService.getCardTransactions(CARD_ID)).isSameAs(transactions);
    }

    @Test
    void getCardTransactions_shouldRejectCardNotOwnedByCurrentUser() {
        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(cardRepository.findByIdAndUserId(CARD_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCardTransactions(CARD_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(transactionRepository, never()).findByUserIdAndCardId(any(), any());
    }

    @Test
    void updateCard_shouldChangeNameOfOwnedCard() {
        Card card = new Card();
        card.setId(CARD_ID);
        card.setName("Old card");
        CardDTO request = new CardDTO();
        request.setName("New card");
        CardDTO response = new CardDTO();
        response.setName("New card");

        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(cardRepository.findByIdAndUserId(CARD_ID, USER_ID)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);
        when(modelMapper.map(card, CardDTO.class)).thenReturn(response);

        assertThat(cardService.updateCard(CARD_ID, request)).isSameAs(response);
        assertThat(card.getName()).isEqualTo("New card");
        verify(cardRepository).save(card);
    }

    @Test
    void updateCard_shouldRejectCardNotOwnedByCurrentUser() {
        CardDTO request = new CardDTO();
        request.setName("New card");

        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(cardRepository.findByIdAndUserId(CARD_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.updateCard(CARD_ID, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void deleteCard_shouldDeleteTransactionsBeforeCard() {
        Card card = new Card();
        List<Transaction> transactions = List.of(new Transaction());

        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(cardRepository.findByIdAndUserId(CARD_ID, USER_ID)).thenReturn(Optional.of(card));
        when(transactionRepository.findByUserIdAndCardId(USER_ID, CARD_ID)).thenReturn(transactions);

        cardService.deleteCard(CARD_ID);

        InOrder deletionOrder = inOrder(transactionRepository, cardRepository);
        deletionOrder.verify(transactionRepository).deleteAll(transactions);
        deletionOrder.verify(cardRepository).delete(card);
    }

    @Test
    void deleteCard_shouldNotDeleteAnythingWhenCardIsNotOwnedByCurrentUser() {
        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(cardRepository.findByIdAndUserId(CARD_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.deleteCard(CARD_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(transactionRepository, never()).deleteAll(any());
        verify(cardRepository, never()).delete(any(Card.class));
    }
}
