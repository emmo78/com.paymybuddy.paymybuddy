package com.paymybuddy.paymybuddy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
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
@TestInstance(Lifecycle.PER_CLASS)
public class TransactionServiceIT {

	@Autowired
	private TransactionService 	transactionService;
	
	//Needed for testing
	@Autowired
	private TransactionRepository transactionRepository;
	
	// Needed for testing
	@Autowired
	private RegisteredRepository registeredRepository;
	
	private static MockHttpServletRequest requestMock;
	private static WebRequest request;
	
	@BeforeAll
	public void setUpForAllTests() {
		requestMock = new MockHttpServletRequest();
		requestMock.setServerName("http://localhost:8080");
		requestMock.setRequestURI("/createTransaction");
		request = new ServletWebRequest(requestMock);
	}

	@AfterAll
	public void unSetForAllTests() {
		requestMock=null;
		request=null;
	}
	
	@AfterEach
	public void unSetForEachTests() {
		transactionRepository.deleteAll();
		registeredRepository.deleteAll();
	}

	@Test
	@Tag("TransactionServiceIT")
	@DisplayName("IT createATransaction should commit it and return transactionDTO for Sender")
	public void createATransactionITShouldCommitItAndReturnTransactionDTOSender() {
		//GIVEN
		Registered registeredASender = new Registered();
		registeredASender.setEmail("aaa@aaa.com");
		registeredASender.setPassword("aaaPasswd");
		registeredASender.setFirstName("Aaa");
		registeredASender.setLastName("AAA");
		registeredASender.setBirthDate(LocalDate.parse("01/21/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
		registeredASender.setIban("aaaIban");
		registeredASender.setBalance(100.50);

		Registered registeredBReceiver = new Registered();
		registeredBReceiver.setEmail("bbb@bbb.com");
		registeredBReceiver.setPassword("bbbPasswd");
		registeredBReceiver.setFirstName("Bbb");
		registeredBReceiver.setLastName("BBB");
		registeredBReceiver.setBirthDate(LocalDate.parse("02/22/1992", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
		registeredBReceiver.setIban("bbbIban");
		registeredBReceiver.setBalance(0);

		registeredRepository.saveAndFlush(registeredASender);
		registeredRepository.saveAndFlush(registeredBReceiver);
		
		TransactionDTO transactionDTO = new TransactionDTO();
		transactionDTO.setEmailSender("aaa@aaa.com");
		transactionDTO.setEmailReceiver("bbb@bbb.com");
		transactionDTO.setAmount("100.00");
		
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
		TransactionDTO transactionDTO = new TransactionDTO();
		transactionDTO.setEmailSender("aaa@aaa.com");
		transactionDTO.setEmailReceiver("bbb@bbb.com");
		transactionDTO.setAmount("100.00");
		Registered registeredASender = new Registered();
		registeredASender.setEmail("aaa@aaa.com");
		registeredASender.setPassword("aaaPasswd");
		registeredASender.setFirstName("Aaa");
		registeredASender.setLastName("AAA");
		registeredASender.setBirthDate(LocalDate.parse("01/21/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
		registeredASender.setIban("aaaIban");
		registeredASender.setBalance(100);
		registeredRepository.saveAndFlush(registeredASender);
		
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
		TransactionDTO transactionDTO = new TransactionDTO();
		transactionDTO.setEmailSender("aaa@aaa.com");
		transactionDTO.setEmailReceiver("bbb@bbb.com");
		transactionDTO.setAmount("100.00");
		Registered registeredASender = new Registered();
		registeredASender.setEmail("aaa@aaa.com");
		registeredASender.setPassword("aaaPasswd");
		registeredASender.setFirstName("Aaa");
		registeredASender.setLastName("AAA");
		registeredASender.setBirthDate(LocalDate.parse("01/21/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
		registeredASender.setIban("aaaIban");
		registeredASender.setBalance(100.5);
		registeredRepository.saveAndFlush(registeredASender);
		
		//WHEN
		//THEN
		assertThat(assertThrows(UnexpectedRollbackException.class,
			() -> transactionService.createATransaction(transactionDTO, request))
			.getMessage()).isEqualTo("Error while creating money transfer");
		assertThat(transactionRepository.count()).isZero();
		assertThat(registeredRepository.findById("aaa@aaa.com").get().getBalance()).isEqualTo(100.5);
	}
}
