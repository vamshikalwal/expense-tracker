package com.expenseTracker.dto;


import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyReportDTO {
    private LocalDate date;
    private Double totalAmount;
    private Long transactionCount;
	public LocalDate getDate() {
		return date;
	}
	public void setDate(LocalDate date) {
		this.date = date;
	}
	public Double getTotalAmount() {
		return totalAmount;
	}
	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}
	public Long getTransactionCount() {
		return transactionCount;
	}
	public void setTransactionCount(Long transactionCount) {
		this.transactionCount = transactionCount;
	}
}
