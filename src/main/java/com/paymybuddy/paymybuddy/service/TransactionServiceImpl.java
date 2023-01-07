package com.paymybuddy.paymybuddy.service;

import java.util.Locale;
import java.util.Optional;

import org.modelmapper.MappingException;
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
import com.paymybuddy.paymybuddy.model.Registered;
import com.paymybuddy.paymybuddy.model.Transaction;
import com.paymybuddy.paymybuddy.repository.RegisteredRepository;
import com.paymybuddy.paymybuddy.repository.TransactionRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor
public class TransactionServiceImpl implements TransactionService {

	private final TransactionDTOService transactionDTOService;
	
	private final TransactionRepository transactionRepository;
	
	private final RegisteredRepository registeredRepository;
	
	private final RequestService requestService;
	
	private final DateTimePatternProperties dateStringPattern;
	
	@Override
	@Transactional(rollbackFor = {UnexpectedRollbackException.class, InsufficentFundsException.class})
	public TransactionDTO createATransaction(TransactionDTO transactionDTO, WebRequest request) throws UnexpectedRollbackException, InsufficentFundsException{
		Transaction transaction = null; 
		try {
			//no CascadeType.PERSIST
			//Throws MappingException | IllegalArgumentException | OptimisticLockingFailureException
			Transaction transactionEnclosingScope = transactionRepository.save(transactionDTOService.transactionFromNewTransactionDTO(transactionDTO));
			double amount = transactionEnclosingScope.getAmount();
			// fee = 0.5 % so to have 1 cent need minimum transfer amount : 0.01/0.005 = 2.00
			if (amount < 2.0) {
				throw new InsufficentFundsException("Insufficient amount, min = 2.00");
			}
			//Throws: IllegalArgumentException | ResourceNotFoundException
			registeredRepository.findById(transactionDTO.getEmailSender()).ifPresentOrElse(sender -> {
					double difference = sender.getBalance()-(amount+transactionEnclosingScope.getFee());
					if (difference < 0) {
						throw new InsufficentFundsException("Insufficient funds for transaction : you need to transfer : "
								+String.format(new Locale(dateStringPattern.getLocalLanguage()) ,"%.2f", Math.abs(difference))
								+" from bank");
					}
					sender.setBalance(difference);
					transactionEnclosingScope.setSender(sender);
				}, () -> { 
					throw new ResourceNotFoundException("Registered sender not found for transaction");
				});
			//Throws: IllegalArgumentException | ResourceNotFoundException
			registeredRepository.findById(transactionDTO.getEmailReceiver()).ifPresentOrElse(receiver -> {
					receiver.setBalance(receiver.getBalance()+amount);
					transactionEnclosingScope.setReceiver(receiver);
				},() -> {
					throw new ResourceNotFoundException("Registered receiver "+transactionDTO.getEmailReceiver()+" not found for transaction");});
			//CascadeType.MERGE => Update Registered sender & receiver cf IT test
			//Throws: IllegalArgumentException | OptimisticLockingFailureException
			transaction = transactionRepository.save(transactionEnclosingScope);
		} catch (MappingException | IllegalArgumentException | ResourceNotFoundException | OptimisticLockingFailureException re) {
			log.error("{} : sender={} : {}", requestService.requestToString(request), transactionDTO.getEmailSender(), re.toString());
			throw new UnexpectedRollbackException("Error while creating money transfer");
		} catch (InsufficentFundsException ife) {
			log.error("{} : sender={} : {}", requestService.requestToString(request), transactionDTO.getEmailSender(), ife.toString());
			throw new InsufficentFundsException(ife.getMessage());
		} catch (Exception e) {
			log.error("{} : sender={} : {} ", requestService.requestToString(request), transactionDTO.getEmailSender(), e.toString());
			throw new UnexpectedRollbackException("Error while creating money transfer");
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
	public Page<TransactionDTO> getRegisteredAllTransaction(String email, Pageable pageRequest, WebRequest request) throws UnexpectedRollbackException {
		Page<TransactionDTO> pageTransactionDTO = null;
		try {
			// for Thymeleaf showing transactionDTO sended in red with "-" amount, it maps transaction to DTO Sender or Receiver converter
			pageTransactionDTO = transactionRepository.findAllTransactionsByEmailSenderOrReceiver(email, pageRequest)
					.map(transaction -> {
						if(email.equals(Optional.ofNullable(transaction.getSender()).orElseGet(() -> new Registered()).getEmail())) {
							return transactionDTOService.transactionToDTOSender(transaction);
						} else {
							return transactionDTOService.transactionToDTOReceiver(transaction);
						}
					});
		} catch (IllegalArgumentException re) {
			log.error("{} : {} ", requestService.requestToString(request), re.toString());
			throw new UnexpectedRollbackException("Error while looking for your money transactions");
		} catch (Exception e) {
			log.error("{} : {} ", requestService.requestToString(request), e.toString());
			throw new UnexpectedRollbackException("Error while looking for your money transactions");
		}
		log.info("{} : pageTransactionDTO number : {} of {}",
				requestService.requestToString(request),
				pageTransactionDTO.getNumber()+1,
				pageTransactionDTO.getTotalPages());
		return pageTransactionDTO;
	}
}
