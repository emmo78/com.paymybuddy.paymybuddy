package com.paymybuddy.paymybuddy.dto.service;

import org.modelmapper.MappingException;

import com.paymybuddy.paymybuddy.dto.TransactionDTO;
import com.paymybuddy.paymybuddy.model.Transaction;

public interface TransactionDTOService {
	TransactionDTO transactionToDTOSender(Transaction transaction);
	TransactionDTO transactionToDTOReceiver(Transaction transaction);
	Transaction transactionFromNewTransactionDTO(TransactionDTO transactionDTO) throws MappingException;
}