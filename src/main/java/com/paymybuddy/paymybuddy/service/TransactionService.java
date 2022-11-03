package com.paymybuddy.paymybuddy.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.paymybuddy.paymybuddy.model.Transaction;

public interface TransactionService {
	Transaction createATransaction(double amount, String emailSender, String emailReceiver);
	Page<Transaction> getRegisteredAllTransaction(String email, Pageable pageRequest);
}
