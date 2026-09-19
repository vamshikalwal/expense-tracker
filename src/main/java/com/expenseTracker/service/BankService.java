package com.expenseTracker.service;

import com.expenseTracker.dto.BankDTO;
import com.expenseTracker.dto.BankDTO;
import com.expenseTracker.entity.Bank;
import com.expenseTracker.entity.Transaction;
import com.expenseTracker.exception.ResourceNotFoundException;
import com.expenseTracker.repository.BankRepository;
import com.expenseTracker.repository.TransactionRepository;
import com.expenseTracker.util.JwtProvider;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BankService {

    private static final Logger log =
            LoggerFactory.getLogger(BankService.class);

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private ModelMapper modelMapper;

    public BankDTO createBank(@Valid BankDTO bankDTO) {

        Long userId = jwtProvider.getCurrentUserId();

        Bank bank = modelMapper.map(bankDTO, Bank.class);

        bank.setUserId(userId);

        Bank savedBank = bankRepository.save(bank);

        log.info(
                "Bank created successfully: {}",
                savedBank.getId()
        );

        return modelMapper.map(savedBank, BankDTO.class);
    }

    public List<BankDTO> getAllBanks() {

        Long userId = jwtProvider.getCurrentUserId();

        List<Bank> banks =
                bankRepository.findByUserId(userId);

        return banks.stream()
                .map(bank ->
                        modelMapper.map(bank, BankDTO.class)
                )
                .toList();
    }

    public BankDTO getBankById(Long bankId) {

        Long userId = jwtProvider.getCurrentUserId();

        Bank bank =
                bankRepository.findByIdAndUserId(
                                bankId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Bank",
                                        "id",
                                        bankId
                                )
                        );

        return modelMapper.map(bank, BankDTO.class);
    }

    public List<Transaction> getBankTransactions(Long bankId) {

        Long userId = jwtProvider.getCurrentUserId();

        bankRepository.findByIdAndUserId(bankId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bank",
                                "id",
                                bankId
                        )
                );

        return transactionRepository
                .findByUserIdAndBankId(userId, bankId);
    }

        @Transactional
        public void deleteBank(Long bankId) {

        Long userId = jwtProvider.getCurrentUserId();

        Bank bank =
                bankRepository.findByIdAndUserId(
                                bankId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Bank",
                                        "id",
                                        bankId
                                )
                        );

        transactionRepository.deleteAll(
                transactionRepository.findByUserIdAndBankId(userId, bankId)
        );
        bankRepository.delete(bank);

        log.info(
                "Bank deleted successfully: {}",
                bankId
        );
    }

    public BankDTO updateBank(Long bankId, BankDTO bankDTO) {

        Long userId = jwtProvider.getCurrentUserId();

        Bank bank =
                bankRepository.findByIdAndUserId(
                                bankId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Bank",
                                        "id",
                                        bankId
                                )
                        );

        bank.setName(bankDTO.getName());

        Bank updatedBank = bankRepository.save(bank);

        log.info(
                "Bank updated successfully: {}",
                updatedBank.getId()
        );

        return modelMapper.map(updatedBank, BankDTO.class);
    }
}