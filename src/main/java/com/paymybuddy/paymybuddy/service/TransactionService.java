package com.paymybuddy.paymybuddy.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.context.request.WebRequest;

import com.paymybuddy.paymybuddy.dto.TransactionDTO;
import com.paymybuddy.paymybuddy.exception.InsufficentFundsException;

public interface TransactionService {
	TransactionDTO createATransaction(TransactionDTO transactionDTO, WebRequest request) throws UnexpectedRollbackException, InsufficentFundsException;
	Page<TransactionDTO> getRegisteredAllTransaction(String email, Pageable pageRequest, WebRequest request) throws UnexpectedRollbackException;
}
