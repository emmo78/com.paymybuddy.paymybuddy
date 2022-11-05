package com.paymybuddy.paymybuddy.dto.service;

import com.paymybuddy.paymybuddy.dto.TransactionDTO;
import com.paymybuddy.paymybuddy.model.Transaction;

public interface TransactionDTOService {
	TransactionDTO transactionToDTO(Transaction transaction);
	TransactionDTO transactionToDTOSender(Transaction transaction);
	TransactionDTO transactionToDTOReceiver(Transaction transaction);
}