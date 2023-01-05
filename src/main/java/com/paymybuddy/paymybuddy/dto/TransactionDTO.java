package com.paymybuddy.paymybuddy.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransactionDTO  {
	private String dateTime;
	private String amount;
	private String fee;
	private String description;
	private String emailSender;
	private String emailReceiver;
	private boolean receiver;

	// To create a transaction first construct a DTO One with email sender before give it (model) to Thymeleaf template form "create"  
	public TransactionDTO(String emailSender) {
		this.emailSender=emailSender;
		amount="0.00";
	}
	
	// For Transaction List to show the contact who sent or received the transaction
	public String getEmail() {
		if (receiver) {
			return emailSender;
		} else {
			return emailReceiver;
		}
	}
}
