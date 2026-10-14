package com.expenseTracker.service;

import com.expenseTracker.dto.TransactionDTO;
import com.expenseTracker.entity.Bank;
import com.expenseTracker.entity.Card;
import com.expenseTracker.entity.PaymentType;
import com.expenseTracker.entity.Transaction;
import com.expenseTracker.exception.BadRequestException;
import com.expenseTracker.exception.ResourceNotFoundException;
import com.expenseTracker.repository.BankRepository;
import com.expenseTracker.repository.CardRepository;
import com.expenseTracker.repository.TransactionRepository;
import com.expenseTracker.util.JwtProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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
class TransactionServiceTest {

    private static final Long USER_ID = 12L;
    private static final Long TRANSACTION_ID = 56L;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private BankRepository bankRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void createTransaction_shouldSaveCardTransactionForCurrentUser() {
        TransactionDTO request = buildTransactionDto(PaymentType.CARD, 9L, null);
        Transaction entity = new Transaction();
        Transaction saved = new Transaction();
        saved.setId(TRANSACTION_ID);
        TransactionDTO response = new TransactionDTO();
        response.setId(TRANSACTION_ID);

        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(cardRepository.existsByIdAndUserId(9L, USER_ID)).thenReturn(true);
        when(modelMapper.map(request, Transaction.class)).thenReturn(entity);
        when(transactionRepository.save(entity)).thenReturn(saved);
        when(modelMapper.map(saved, TransactionDTO.class)).thenReturn(response);

        TransactionDTO result = transactionService.createTransaction(request);

        assertThat(entity.getUserId()).isEqualTo(USER_ID);
        assertThat(result).isSameAs(response);
        verify(transactionRepository).save(entity);
    }

    @Test
    void createTransaction_shouldRejectMissingCardIdForCardPayment() {
        TransactionDTO request = buildTransactionDto(PaymentType.CARD, null, null);

        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Card ID is required for CARD payment type");

        verify(cardRepository, never()).existsByIdAndUserId(any(), any());
    }

    @Test
    void createTransaction_shouldRejectCardOwnedByAnotherUser() {
        TransactionDTO request = buildTransactionDto(PaymentType.CARD, 9L, null);

        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(cardRepository.existsByIdAndUserId(9L, USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Card not found with id : '9'");
    }

    @Test
    void createTransaction_shouldRejectMissingBankIdForBankPayment() {
        TransactionDTO request = buildTransactionDto(PaymentType.BANK, null, null);

        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Bank ID is required for BANK payment type");

        verify(bankRepository, never()).existsByIdAndUserId(any(), any());
    }

    @Test
    void createTransaction_shouldRejectBankOwnedByAnotherUser() {
        TransactionDTO request = buildTransactionDto(PaymentType.BANK, null, 7L);

        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(bankRepository.existsByIdAndUserId(7L, USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Bank not found with id : '7'");
    }

    @Test
    void getAllTransactions_shouldReturnMappedTransactions() {
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);
        TransactionDTO response = new TransactionDTO();
        response.setId(TRANSACTION_ID);

        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(transactionRepository.findByUserId(USER_ID)).thenReturn(List.of(transaction));
        when(modelMapper.map(transaction, TransactionDTO.class)).thenReturn(response);

        assertThat(transactionService.getAllTransactions()).containsExactly(response);
    }

    @Test
    void getAllTransactionsPaginated_shouldMapPageOfTransactions() {
        Pageable pageable = Pageable.unpaged();
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);
        TransactionDTO response = new TransactionDTO();
        response.setId(TRANSACTION_ID);
        Page<Transaction> page = new PageImpl<>(List.of(transaction));

        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(transactionRepository.findByUserId(USER_ID, pageable)).thenReturn(page);
        when(modelMapper.map(transaction, TransactionDTO.class)).thenReturn(response);

        assertThat(transactionService.getAllTransactionsPaginated(pageable).getContent())
                .containsExactly(response);
    }

    @Test
    void getTransactionById_shouldReturnOwnedTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);
        TransactionDTO response = new TransactionDTO();
        response.setId(TRANSACTION_ID);

        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(transactionRepository.findByIdAndUserId(TRANSACTION_ID, USER_ID)).thenReturn(Optional.of(transaction));
        when(modelMapper.map(transaction, TransactionDTO.class)).thenReturn(response);

        assertThat(transactionService.getTransactionById(TRANSACTION_ID)).isSameAs(response);
    }

    @Test
    void getTransactionsByDateRange_shouldReturnTransactionsInDateWindow() {
        LocalDate start = LocalDate.of(2026, 9, 1);
        LocalDate end = LocalDate.of(2026, 9, 30);
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);
        TransactionDTO response = new TransactionDTO();
        response.setId(TRANSACTION_ID);

        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(transactionRepository.findByUserIdAndDateBetween(USER_ID, start, end)).thenReturn(List.of(transaction));
        when(modelMapper.map(transaction, TransactionDTO.class)).thenReturn(response);

        assertThat(transactionService.getTransactionsByDateRange(start, end)).containsExactly(response);
    }

    @Test
    void updateTransaction_shouldUpdateOwnedTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);
        transaction.setPaymentType(PaymentType.CARD);
        transaction.setCardId(9L);
        transaction.setUserId(USER_ID);
        TransactionDTO request = buildTransactionDto(PaymentType.CARD, 9L, null);
        request.setPaymentName("Updated name");
        Transaction updatedTransaction = new Transaction();
        updatedTransaction.setId(TRANSACTION_ID);
        TransactionDTO response = new TransactionDTO();
        response.setId(TRANSACTION_ID);

        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(transactionRepository.findByIdAndUserId(TRANSACTION_ID, USER_ID)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(transaction)).thenReturn(updatedTransaction);
        when(modelMapper.map(updatedTransaction, TransactionDTO.class)).thenReturn(response);

        TransactionDTO result = transactionService.updateTransaction(TRANSACTION_ID, request);

        assertThat(result).isSameAs(response);
        assertThat(transaction.getPaymentName()).isEqualTo("Updated name");
        verify(transactionRepository).save(transaction);
    }

    @Test
    void deleteTransaction_shouldDeleteOwnedTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);

        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(transactionRepository.findByIdAndUserId(TRANSACTION_ID, USER_ID)).thenReturn(Optional.of(transaction));

        transactionService.deleteTransaction(TRANSACTION_ID);

        verify(transactionRepository).delete(transaction);
    }

    @Test
    void deleteTransaction_shouldRejectTransactionNotOwnedByCurrentUser() {
        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(transactionRepository.findByIdAndUserId(TRANSACTION_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.deleteTransaction(TRANSACTION_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(transactionRepository, never()).delete(any(Transaction.class));
    }

    private TransactionDTO buildTransactionDto(PaymentType paymentType, Long cardId, Long bankId) {
        TransactionDTO dto = new TransactionDTO();
        dto.setPaymentName("Groceries");
        dto.setAmount(120.75);
        dto.setDate(LocalDate.of(2026, 9, 15));
        dto.setCategory("Food");
        dto.setPaymentType(paymentType);
        dto.setCardId(cardId);
        dto.setBankId(bankId);
        return dto;
    }
}
