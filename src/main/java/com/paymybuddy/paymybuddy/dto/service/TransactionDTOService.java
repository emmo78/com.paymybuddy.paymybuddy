package com.paymybuddy.paymybuddy.dto.service;

import org.modelmapper.MappingException;

import com.paymybuddy.paymybuddy.dto.TransactionDTO;
import com.paymybuddy.paymybuddy.model.Transaction;

/**
 * service interface for converting DTOs to the model and vice versa
 * use DateTimePatternProperties
 * @author Olivier MOREL
 *
 */
public interface TransactionDTOService {
	TransactionDTO transactionToDTOSender(Transaction transaction);
	TransactionDTO transactionToDTOReceiver(Transaction transaction);
	Transaction transactionFromNewTransactionDTO(TransactionDTO transactionDTO) throws MappingException;
}