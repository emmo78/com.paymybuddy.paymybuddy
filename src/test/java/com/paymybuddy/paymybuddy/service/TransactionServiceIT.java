package com.paymybuddy.paymybuddy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.paymybuddy.paymybuddy.dto.TransactionDTO;
import com.paymybuddy.paymybuddy.exception.InsufficentFundsException;
import com.paymybuddy.paymybuddy.model.Registered;
import com.paymybuddy.paymybuddy.repository.RegisteredRepository;
import com.paymybuddy.paymybuddy.repository.TransactionRepository;

@SpringBootTest
public class TransactionServiceIT {

	@Autowired
	private TransactionService 	transactionService;
	
	//Needed for testing
	@Autowired
	private TransactionRepository transactionRepository;
	
	// Needed for testing
	@Autowired
	private RegisteredRepository registeredRepository;
	
	@AfterEach
	public void undefPerTest() {
		transactionRepository.deleteAll();
		registeredRepository.deleteAll();
	}
	
	@Test
	@Tag("TransactionServiceIT")
	@DisplayName("IT createATransaction should commit it and return transactionDTO for Sender")
	public void createATransactionITShouldCommitItAndReturnTransactionDTOSender() {
		//GIVEN
		Registered registeredASender = new Registered("aaa@aaa.com", "aaaPasswd", "Aaa", "AAA", LocalDate.parse("01/01/1991", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "aaaIban");
		Registered registeredBReceiver = new Registered("bbb@bbb.com", "bbbPasswd", "Bbb", "BBB", LocalDate.parse("02/02/1992", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "bbbIban");
		registeredASender.setBalance(100.5);
		registeredRepository.saveAndFlush(registeredASender);
		registeredRepository.saveAndFlush(registeredBReceiver);
		
		TransactionDTO transactionDTO = new TransactionDTO("aaa@aaa.com", "bbb@bbb.com");
		transactionDTO.setAmount("100.00");
		
		MockHttpServletRequest requestMock = new MockHttpServletRequest();
		requestMock.setServerName("http://localhost:8080");
		requestMock.setRequestURI("/createTransaction");
		WebRequest request = new ServletWebRequest(requestMock);
		
		//WHEN
		TransactionDTO transactionDTOResult = transactionService.createATransaction(transactionDTO, request);
		
		//THEN
		assertThat(transactionRepository.count()).isEqualTo(1L);
		assertThat(transactionDTOResult).extracting(
				TransactionDTO::getAmount,
				TransactionDTO::getFee,
				TransactionDTO::getEmailSender,
				TransactionDTO::getEmailReceiver,
				TransactionDTO::isReceiver
				).containsExactly(
						"-100.00",
						"-0.50",
						"aaa@aaa.com",
						"bbb@bbb.com",
						false
				);
		assertThat(LocalDateTime.parse(transactionDTOResult.getDateTime(), DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss")))
			.isCloseTo(LocalDateTime.now(), within(2, ChronoUnit.SECONDS));
		assertThat(registeredRepository.findById("aaa@aaa.com").get().getBalance()).isZero();
		assertThat(registeredRepository.findById("bbb@bbb.com").get().getBalance()).isEqualTo(100d);
	}
	
	@Test
	@Tag("TransactionServiceIT")
	@DisplayName("IT createATransaction with InsufficentFundsException should Rollback")
	public void createATransactionITShouldRollbackOnInsufficentFundsException() {
		//GIVEN
		TransactionDTO transactionDTO = new TransactionDTO("aaa@aaa.com", "bbb@bbb.com");
		transactionDTO.setAmount("100.00");
		Registered registeredASender = new Registered("aaa@aaa.com", "aaaPasswd", "Aaa", "AAA", LocalDate.parse("01/01/1991", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "aaaIban");
		registeredASender.setBalance(100.0);
		registeredRepository.saveAndFlush(registeredASender);
		
		MockHttpServletRequest requestMock = new MockHttpServletRequest();
		WebRequest request = new ServletWebRequest(requestMock);
		
		//WHEN
		//THEN
		assertThat(assertThrows(InsufficentFundsException.class,
				() -> transactionService.createATransaction(transactionDTO, request))
				.getMessage()).isEqualTo("Insufficient funds for transaction : you need to transfert : 0.50 from bank");
		assertThat(transactionRepository.count()).isZero();
		assertThat(registeredRepository.findById("aaa@aaa.com").get().getBalance()).isEqualTo(100d);
	}
	
	@Test
	@Tag("TransactionServiceIT")
	@DisplayName("IT createATransaction with ResourceNotFoundException for Receiver should Rollback")
	public void createATransactionITShouldRollbackOnResourceNotFoundException() {
		//GIVEN
		TransactionDTO transactionDTO = new TransactionDTO("aaa@aaa.com", "bbb@bbb.com");
		transactionDTO.setAmount("100.00");
		Registered registeredASender = new Registered("aaa@aaa.com", "aaaPasswd", "Aaa", "AAA", LocalDate.parse("01/01/1991", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "aaaIban");
		registeredASender.setBalance(100.5);
		registeredRepository.saveAndFlush(registeredASender);
		
		MockHttpServletRequest requestMock = new MockHttpServletRequest();
		WebRequest request = new ServletWebRequest(requestMock);
		
		//WHEN
		//THEN
		assertThat(assertThrows(UnexpectedRollbackException.class, () -> transactionService.createATransaction(transactionDTO, request)).getMessage()).isEqualTo("Registered receiver not found for transaction");
		assertThat(transactionRepository.count()).isZero();
		assertThat(registeredRepository.findById("aaa@aaa.com").get().getBalance()).isEqualTo(100.5);
	}
}

