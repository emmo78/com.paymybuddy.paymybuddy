package com.paymybuddy.paymybuddy.service;

import org.modelmapper.MappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.WebRequest;

import com.paymybuddy.paymybuddy.dto.TransactionDTO;
import com.paymybuddy.paymybuddy.dto.service.TransactionDTOService;
import com.paymybuddy.paymybuddy.exception.ResourceNotFoundException;
import com.paymybuddy.paymybuddy.model.Transaction;
import com.paymybuddy.paymybuddy.repository.RegisteredRepository;
import com.paymybuddy.paymybuddy.repository.TransactionRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {

	@Autowired 
	TransactionDTOService transactionDTOService;
	
	@Autowired
	TransactionRepository transactionRepository;
	
	@Autowired
	RegisteredRepository registeredRepository;
	
	@Autowired
	RequestService requestService;
	
	@Override
	@Transactional(rollbackFor = UnexpectedRollbackException.class)
	public TransactionDTO createATransaction(TransactionDTO transactionDTO, WebRequest request) throws UnexpectedRollbackException{
		Transaction transaction = null; 
		try {
			//Throws MappingException
			Transaction transactionEnclosingScope = transactionRepository.save(transactionDTOService.transactionFromNewTransactionDTO(transactionDTO));
			//Throws: IllegalArgumentException | ResourceNotFoundException
			registeredRepository.findById(transactionDTO.getEmailSender()).ifPresentOrElse(r -> r.addSendedTransaction(transactionEnclosingScope), () -> {throw new ResourceNotFoundException("Registered sender not found for transaction");});
			registeredRepository.findById(transactionDTO.getEmailReceiver()).ifPresentOrElse(r -> r.addReceivedTransaction(transactionEnclosingScope), () -> {throw new ResourceNotFoundException("Registered receiver not found for transaction");});
			//Throws: IllegalArgumentException | OptimisticLockingFailureException
			transaction = transactionRepository.save(transactionEnclosingScope);
		} catch (MappingException | IllegalArgumentException | ResourceNotFoundException | OptimisticLockingFailureException e) {
			throw new UnexpectedRollbackException(e.getMessage());
		}
	log.info("{} : transaction sender={} receiver={} amount={} fee={} persisted", requestService.requestToString(request), transaction.getSender().getEmail(), transaction.getReceiver().getEmail(), transaction.getAmount(), transaction.getFee());
	return transactionDTOService.transactionToDTOSender(transaction);
	}

	@Override
	public Page<Transaction> getRegisteredAllTransaction(String email, Pageable pageRequest, WebRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

}
