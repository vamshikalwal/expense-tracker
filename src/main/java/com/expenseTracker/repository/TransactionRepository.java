package com.expenseTracker.repository;

import com.expenseTracker.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findByUserId(Long userId);
    
    Optional<Transaction> findByIdAndUserId(Long id, Long userId);
    
    Page<Transaction> findByUserId(Long userId, Pageable pageable);
    
    List<Transaction> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    
    List<Transaction> findByUserIdAndCardId(Long userId, Long cardId);
    
    List<Transaction> findByUserIdAndBankId(Long userId, Long bankId);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.userId = :userId")
    Double getTotalAmountByUserId(@Param("userId") Long userId);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.userId = :userId AND t.date BETWEEN :startDate AND :endDate")
    Double getTotalAmountByUserIdAndDateRange(@Param("userId") Long userId, 
                                             @Param("startDate") LocalDate startDate, 
                                             @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.userId = :userId")
    Long getTransactionCountByUserId(@Param("userId") Long userId);
}
