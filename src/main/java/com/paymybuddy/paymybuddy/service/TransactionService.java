package com.paymybuddy.paymybuddy.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.paymybuddy.paymybuddy.dto.TransactionDTO;
import com.paymybuddy.paymybuddy.model.Transaction;

public interface TransactionService {
	TransactionDTO createATransaction(TransactionDTO transactionDTO);
	Page<Transaction> getRegisteredAllTransaction(String email, Pageable pageRequest);
}
