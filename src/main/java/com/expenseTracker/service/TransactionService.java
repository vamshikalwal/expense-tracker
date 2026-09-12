package com.expenseTracker.service;
import com.expenseTracker.dto.TransactionDTO;
import com.expenseTracker.entity.PaymentType;
import com.expenseTracker.entity.Transaction;
import com.expenseTracker.exception.BadRequestException;
import com.expenseTracker.exception.ResourceNotFoundException;
import com.expenseTracker.repository.BankRepository;
import com.expenseTracker.repository.CardRepository;
import com.expenseTracker.repository.TransactionRepository;
import com.expenseTracker.util.JwtProvider;

import org.modelmapper.ModelMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionService {

    private static final Logger log =
            LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private ModelMapper modelMapper;

    public TransactionDTO createTransaction(
            TransactionDTO transactionDTO
    ) {

        Long userId = jwtProvider.getCurrentUserId();

        if (PaymentType.CARD.equals(
                transactionDTO.getPaymentType()
        )) {

            if (transactionDTO.getCardId() == null) {

                throw new BadRequestException(
                        "Card ID is required for CARD payment type"
                );
            }

            if (!cardRepository.existsByIdAndUserId(
                    transactionDTO.getCardId(),
                    userId
            )) {

                throw new ResourceNotFoundException(
                        "Card",
                        "id",
                        transactionDTO.getCardId()
                );
            }

        } else if (PaymentType.BANK.equals(
                transactionDTO.getPaymentType()
        )) {

            if (transactionDTO.getBankId() == null) {

                throw new BadRequestException(
                        "Bank ID is required for BANK payment type"
                );
            }

            if (!bankRepository.existsByIdAndUserId(
                    transactionDTO.getBankId(),
                    userId
            )) {

                throw new ResourceNotFoundException(
                        "Bank",
                        "id",
                        transactionDTO.getBankId()
                );
            }
        }

        Transaction transaction =
                modelMapper.map(
                        transactionDTO,
                        Transaction.class
                );

        transaction.setUserId(userId);

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        log.info(
                "Transaction created successfully: {}",
                savedTransaction.getId()
        );

        return modelMapper.map(
                savedTransaction,
                TransactionDTO.class
        );
    }

    public List<TransactionDTO> getAllTransactions() {

        Long userId = jwtProvider.getCurrentUserId();

        List<Transaction> transactions =
                transactionRepository.findByUserId(userId);

        return transactions.stream()

                .map(trans ->
                        modelMapper.map(
                                trans,
                                TransactionDTO.class
                        )
                )

                .toList();
    }

    public Page<TransactionDTO> getAllTransactionsPaginated(
            Pageable pageable
    ) {

        Long userId = jwtProvider.getCurrentUserId();

        Page<Transaction> transactions =
                transactionRepository.findByUserId(
                        userId,
                        pageable
                );

        return transactions.map(trans ->
                modelMapper.map(
                        trans,
                        TransactionDTO.class
                )
        );
    }

    public TransactionDTO getTransactionById(
            Long transactionId
    ) {

        Long userId = jwtProvider.getCurrentUserId();

        Transaction transaction =
                transactionRepository.findByIdAndUserId(
                                transactionId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Transaction",
                                        "id",
                                        transactionId
                                )
                        );

        return modelMapper.map(
                transaction,
                TransactionDTO.class
        );
    }

    public List<TransactionDTO> getTransactionsByDateRange(
            LocalDate startDate,
            LocalDate endDate
    ) {

        Long userId = jwtProvider.getCurrentUserId();

        List<Transaction> transactions =
                transactionRepository
                        .findByUserIdAndDateBetween(
                                userId,
                                startDate,
                                endDate
                        );

        return transactions.stream()

                .map(trans ->
                        modelMapper.map(
                                trans,
                                TransactionDTO.class
                        )
                )

                .toList();
    }

    public void deleteTransaction(Long transactionId) {

        Long userId = jwtProvider.getCurrentUserId();

        Transaction transaction =
                transactionRepository.findByIdAndUserId(
                                transactionId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Transaction",
                                        "id",
                                        transactionId
                                )
                        );

        transactionRepository.delete(transaction);

        log.info(
                "Transaction deleted successfully: {}",
                transactionId
        );
    }

    public TransactionDTO updateTransaction(
            Long transactionId,
            TransactionDTO transactionDTO
    ) {

        Long userId = jwtProvider.getCurrentUserId();

        Transaction transaction =
                transactionRepository.findByIdAndUserId(
                                transactionId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Transaction",
                                        "id",
                                        transactionId
                                )
                        );

        if (!transaction.getPaymentType().equals(
                transactionDTO.getPaymentType()
        )) {

            if (PaymentType.CARD.equals(
                    transactionDTO.getPaymentType()
            )) {

                if (transactionDTO.getCardId() == null) {

                    throw new BadRequestException(
                            "Card ID is required for CARD payment type"
                    );
                }

                if (!cardRepository.existsByIdAndUserId(
                        transactionDTO.getCardId(),
                        userId
                )) {

                    throw new ResourceNotFoundException(
                            "Card",
                            "id",
                            transactionDTO.getCardId()
                    );
                }

            } else if (PaymentType.BANK.equals(
                    transactionDTO.getPaymentType()
            )) {

                if (transactionDTO.getBankId() == null) {

                    throw new BadRequestException(
                            "Bank ID is required for BANK payment type"
                    );
                }

                if (!bankRepository.existsByIdAndUserId(
                        transactionDTO.getBankId(),
                        userId
                )) {

                    throw new ResourceNotFoundException(
                            "Bank",
                            "id",
                            transactionDTO.getBankId()
                    );
                }
            }
        }

        transaction.setPaymentName(
                transactionDTO.getPaymentName()
        );

        transaction.setAmount(
                transactionDTO.getAmount()
        );

        transaction.setDate(
                transactionDTO.getDate()
        );

        transaction.setCategory(
                transactionDTO.getCategory()
        );

        transaction.setPaymentType(
                transactionDTO.getPaymentType()
        );

        transaction.setCardId(
                transactionDTO.getCardId()
        );

        transaction.setBankId(
                transactionDTO.getBankId()
        );

        Transaction updatedTransaction =
                transactionRepository.save(transaction);

        log.info(
                "Transaction updated successfully: {}",
                updatedTransaction.getId()
        );

        return modelMapper.map(
                updatedTransaction,
                TransactionDTO.class
        );
    }
}