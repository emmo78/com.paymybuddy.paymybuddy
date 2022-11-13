package com.paymybuddy.paymybuddy.service;

import java.util.Locale;

import org.modelmapper.MappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.WebRequest;

import com.paymybuddy.paymybuddy.configuration.DateTimePatternProperties;
import com.paymybuddy.paymybuddy.dto.TransactionDTO;
import com.paymybuddy.paymybuddy.dto.service.TransactionDTOService;
import com.paymybuddy.paymybuddy.exception.InsufficentFundsException;
import com.paymybuddy.paymybuddy.exception.ResourceNotFoundException;
import com.paymybuddy.paymybuddy.model.Transaction;
import com.paymybuddy.paymybuddy.repository.RegisteredRepository;
import com.paymybuddy.paymybuddy.repository.TransactionRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {

	@Autowired 
	private TransactionDTOService transactionDTOService;
	
	@Autowired
	private TransactionRepository transactionRepository;
	
	@Autowired
	private RegisteredRepository registeredRepository;
	
	@Autowired
	private RequestService requestService;
	
	@Autowired
	private DateTimePatternProperties dateStringPattern;
	
	@Override
	@Transactional(rollbackFor = UnexpectedRollbackException.class)
	public TransactionDTO createATransaction(TransactionDTO transactionDTO, WebRequest request) throws UnexpectedRollbackException{
		Transaction transaction = null; 
		try {
			//Throws MappingException | IllegalArgumentException | OptimisticLockingFailureException
			Transaction transactionEnclosingScope = transactionRepository.save(transactionDTOService.transactionFromNewTransactionDTO(transactionDTO));
			double amount = transactionEnclosingScope.getAmount();
			//Throws: IllegalArgumentException | ResourceNotFoundException
			registeredRepository.findById(transactionDTO.getEmailSender()).ifPresentOrElse(sender -> {
				double difference = sender.getBalance()-(amount+transactionEnclosingScope.getFee());
				if (difference < 0) {
					throw new InsufficentFundsException("Insufficient funds for transaction : you need to transfert : "
							+String.format(new Locale(dateStringPattern.getLocalLanguage()) ,"%.2f", Math.abs(difference))
							+" from bank");
				}
				sender.setBalance(difference);
				sender.addSendedTransaction(transactionEnclosingScope);
				}, () -> {throw new ResourceNotFoundException("Registered sender not found for transaction");});
			//Throws: IllegalArgumentException | ResourceNotFoundException
			registeredRepository.findById(transactionDTO.getEmailReceiver()).ifPresentOrElse(receiver -> {
				receiver.setBalance(receiver.getBalance()+amount);
				receiver.addReceivedTransaction(transactionEnclosingScope);},
					() -> {throw new ResourceNotFoundException("Registered receiver not found for transaction");});
			//CascadeType.MERGE => Update Registered sender & receiver cf IT test
			//Throws: IllegalArgumentException | OptimisticLockingFailureException
			transaction = transactionRepository.save(transactionEnclosingScope);
		} catch (MappingException | IllegalArgumentException | InsufficentFundsException | ResourceNotFoundException | OptimisticLockingFailureException re) {
			throw new UnexpectedRollbackException(re.getMessage());
		} catch (Exception e) {
			throw new UnexpectedRollbackException(e.getMessage());
		}
	log.info("{} : transaction sender={} receiver={} amount={} fee={} persisted",
			requestService.requestToString(request),
			transaction.getSender().getEmail(),
			transaction.getReceiver().getEmail(),
			transaction.getAmount(),
			transaction.getFee());
	return transactionDTOService.transactionToDTOSender(transaction);
	}

	@Override
	@Transactional(readOnly = true, rollbackFor = UnexpectedRollbackException.class)
	public Page<TransactionDTO> getRegisteredAllTransaction(String email, Pageable pageRequest, WebRequest request) {
		Page<TransactionDTO> pageTransactionDTO = null;
		try {
			pageTransactionDTO = transactionRepository.findAllTransactionsByEmailSenderOrReceiver(email, pageRequest)
					.map(t -> {
						if(email.equals(t.getSender().getEmail())) {
							return transactionDTOService.transactionToDTOSender(t);
						} else {
							return transactionDTOService.transactionToDTOReceiver(t);
						}
					});
		} catch (IllegalArgumentException re) {
			throw new UnexpectedRollbackException(re.getMessage());
		} catch (Exception e) {
			throw new UnexpectedRollbackException(e.getMessage());
		}
		log.info("{} : pageTransactionDTO number : {} of {}",
				requestService.requestToString(request),
				pageTransactionDTO.getNumber(),
				pageTransactionDTO.getTotalPages());
		return pageTransactionDTO;
	}
}
