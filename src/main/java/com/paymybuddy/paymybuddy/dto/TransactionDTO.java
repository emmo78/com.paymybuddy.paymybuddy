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
	private String emailSender;
	private String emailReceiver;
	private boolean receiver;
	public TransactionDTO(String emailSender, String emailReceiver) {
		this.emailSender = emailSender;
		this.emailReceiver = emailReceiver;
	}
}
