package com.expenseTracker.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankWiseDTO {
    private Long bankId;
    private String bankName;
    private Double totalAmount;
    private Long transactionCount;
	public Long getBankId() {
		return bankId;
	}
	public void setBankId(Long bankId) {
		this.bankId = bankId;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
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
