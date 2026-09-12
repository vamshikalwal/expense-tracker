package com.expenseTracker.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardWiseDTO {
    private Long cardId;
    public Long getCardId() {
		return cardId;
	}
	public void setCardId(Long cardId) {
		this.cardId = cardId;
	}
	public String getCardName() {
		return cardName;
	}
	public void setCardName(String cardName) {
		this.cardName = cardName;
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
	private String cardName;
    private Double totalAmount;
    private Long transactionCount;
}
