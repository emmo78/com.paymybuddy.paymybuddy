package com.paymybuddy.paymybuddy.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.context.request.WebRequest;

import com.paymybuddy.paymybuddy.dto.TransactionDTO;
import com.paymybuddy.paymybuddy.exception.InsufficentFundsException;

/**
 * Service interface for Transaction DAL
 * @author Olivier MOREL
 *
 */
public interface TransactionService {
	
	/**
	 * persist a new transaction from a given DTO
	 * @param transactionDTO
	 * @param request
	 * @return
	 * @throws UnexpectedRollbackException
	 * @throws InsufficentFundsException
	 */
	TransactionDTO createATransaction(TransactionDTO transactionDTO, WebRequest request) throws UnexpectedRollbackException, InsufficentFundsException;
	
	/**
	 * Return page of all transaction sended or received by email. 
	 * @param email
	 * @param pageRequest
	 * @param request
	 * @return
	 * @throws UnexpectedRollbackException
	 */
	Page<TransactionDTO> getRegisteredAllTransaction(String email, Pageable pageRequest, WebRequest request) throws UnexpectedRollbackException;
}
