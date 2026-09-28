package com.expenseTracker.service;

import com.expenseTracker.dto.BankDTO;
import com.expenseTracker.entity.Bank;
import com.expenseTracker.entity.Transaction;
import com.expenseTracker.exception.ResourceNotFoundException;
import com.expenseTracker.repository.BankRepository;
import com.expenseTracker.repository.TransactionRepository;
import com.expenseTracker.util.JwtProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
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
class BankServiceTest {

    private static final Long USER_ID = 12L;
    private static final Long BANK_ID = 34L;

    @Mock
    private BankRepository bankRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private BankService bankService;

    @Test
    void createBank_shouldAssignCurrentUserAndReturnSavedBank() {
        BankDTO request = new BankDTO();
        request.setName("Everyday");
        Bank bank = new Bank();
        bank.setName("Everyday");
        Bank savedBank = new Bank();
        savedBank.setId(BANK_ID);
        savedBank.setName("Everyday");
        savedBank.setUserId(USER_ID);
        BankDTO response = new BankDTO();
        response.setId(BANK_ID);
        response.setName("Everyday");

        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(modelMapper.map(request, Bank.class)).thenReturn(bank);
        when(bankRepository.save(bank)).thenReturn(savedBank);
        when(modelMapper.map(savedBank, BankDTO.class)).thenReturn(response);

        BankDTO result = bankService.createBank(request);

        assertThat(bank.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getId()).isEqualTo(BANK_ID);
        assertThat(result.getName()).isEqualTo("Everyday");
        verify(bankRepository).save(bank);
    }

    @Test
    void getAllBanks_shouldReturnOnlyBanksForCurrentUser() {
        Bank bank = new Bank();
        bank.setId(BANK_ID);
        BankDTO response = new BankDTO();
        response.setId(BANK_ID);

        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(bankRepository.findByUserId(USER_ID)).thenReturn(List.of(bank));
        when(modelMapper.map(bank, BankDTO.class)).thenReturn(response);

        List<BankDTO> result = bankService.getAllBanks();

        assertThat(result).containsExactly(response);
        verify(bankRepository).findByUserId(USER_ID);
    }

    @Test
    void getBankById_shouldReturnBankOwnedByCurrentUser() {
        Bank bank = new Bank();
        bank.setId(BANK_ID);
        BankDTO response = new BankDTO();
        response.setId(BANK_ID);

        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(bankRepository.findByIdAndUserId(BANK_ID, USER_ID)).thenReturn(Optional.of(bank));
        when(modelMapper.map(bank, BankDTO.class)).thenReturn(response);

        assertThat(bankService.getBankById(BANK_ID)).isSameAs(response);
    }

    @Test
    void getBankById_shouldRejectBankNotOwnedByCurrentUser() {
        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(bankRepository.findByIdAndUserId(BANK_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bankService.getBankById(BANK_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Bank not found with id : '34'");
    }

    @Test
    void getBankTransactions_shouldReturnTransactionsForOwnedBank() {
        Bank bank = new Bank();
        List<Transaction> transactions = List.of(new Transaction());

        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(bankRepository.findByIdAndUserId(BANK_ID, USER_ID)).thenReturn(Optional.of(bank));
        when(transactionRepository.findByUserIdAndBankId(USER_ID, BANK_ID)).thenReturn(transactions);

        assertThat(bankService.getBankTransactions(BANK_ID)).isSameAs(transactions);
    }

    @Test
    void getBankTransactions_shouldRejectBankNotOwnedByCurrentUser() {
        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(bankRepository.findByIdAndUserId(BANK_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bankService.getBankTransactions(BANK_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(transactionRepository, never()).findByUserIdAndBankId(any(), any());
    }

    @Test
    void updateBank_shouldChangeNameOfOwnedBank() {
        Bank bank = new Bank();
        bank.setId(BANK_ID);
        bank.setName("Old name");
        BankDTO request = new BankDTO();
        request.setName("New name");
        BankDTO response = new BankDTO();
        response.setName("New name");

        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(bankRepository.findByIdAndUserId(BANK_ID, USER_ID)).thenReturn(Optional.of(bank));
        when(bankRepository.save(bank)).thenReturn(bank);
        when(modelMapper.map(bank, BankDTO.class)).thenReturn(response);

        assertThat(bankService.updateBank(BANK_ID, request)).isSameAs(response);
        assertThat(bank.getName()).isEqualTo("New name");
        verify(bankRepository).save(bank);
    }

    @Test
    void updateBank_shouldRejectBankNotOwnedByCurrentUser() {
        BankDTO request = new BankDTO();
        request.setName("New name");
        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(bankRepository.findByIdAndUserId(BANK_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bankService.updateBank(BANK_ID, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(bankRepository, never()).save(any(Bank.class));
    }

    @Test
    void deleteBank_shouldDeleteTransactionsBeforeBank() {
        Bank bank = new Bank();
        List<Transaction> transactions = List.of(new Transaction());

        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(bankRepository.findByIdAndUserId(BANK_ID, USER_ID)).thenReturn(Optional.of(bank));
        when(transactionRepository.findByUserIdAndBankId(USER_ID, BANK_ID)).thenReturn(transactions);

        bankService.deleteBank(BANK_ID);

        InOrder deletionOrder = inOrder(transactionRepository, bankRepository);
        deletionOrder.verify(transactionRepository).deleteAll(transactions);
        deletionOrder.verify(bankRepository).delete(bank);
    }

    @Test
    void deleteBank_shouldNotDeleteAnythingWhenBankIsNotOwnedByCurrentUser() {
        when(jwtProvider.getCurrentUserId()).thenReturn(USER_ID);
        when(bankRepository.findByIdAndUserId(BANK_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bankService.deleteBank(BANK_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(transactionRepository, never()).deleteAll(any());
        verify(bankRepository, never()).delete(any(Bank.class));
    }
}
