package com.paymybuddy.paymybuddy.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.MappingException;
import org.modelmapper.spi.ErrorMessage;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.context.request.WebRequest;

import com.paymybuddy.paymybuddy.dto.TransactionDTO;
import com.paymybuddy.paymybuddy.dto.service.TransactionDTOService;
import com.paymybuddy.paymybuddy.model.Registered;
import com.paymybuddy.paymybuddy.model.Transaction;
import com.paymybuddy.paymybuddy.repository.RegisteredRepository;
import com.paymybuddy.paymybuddy.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

	@InjectMocks
	private TransactionServiceImpl transactionService = new TransactionServiceImpl();
	
	@Mock 
	private TransactionDTOService transactionDTOService;
	
	@Mock
	private TransactionRepository transactionRepository;
	
	@Mock
	private RegisteredRepository registeredRepository;
	
	@Mock
	private RequestService requestService;
	
	private WebRequest request = null;
	
	@BeforeEach
	public void setUpPerTest() {
	}
	
	@AfterEach
	public void undefPerTest() {
		
	}

	@Test
	@Tag("TransactionServiceTest")
	@DisplayName("test createATransaction should throw UnexpectedRollbackException On MappingException")
	public void createATransactionTestShouldThrowUnexpectedRollbackExceptionOnMappingException() {
		//GIVEN
		TransactionDTO transactionDTO = new TransactionDTO();
		when(transactionDTOService.transactionFromNewTransactionDTO(any(TransactionDTO.class))).thenThrow(new MappingException(new ArrayList<ErrorMessage>()));
		
		//WHEN
		//THEN
		assertThrows(UnexpectedRollbackException.class, () -> transactionService.createATransaction(transactionDTO, request));
	}

	@Test
	@Tag("TransactionServiceTest")
	@DisplayName("test createATransaction should throw UnexpectedRollbackException On IllegalArgumentException")
	public void createATransactionTestShouldThrowUnexpectedRollbackExceptionOnIllegalArgumentException() {
		//GIVEN
		TransactionDTO transactionDTO = new TransactionDTO("aaa@aaa.com", "bbb@bbb.com");
		when(registeredRepository.findById(any(String.class))).thenThrow(new IllegalArgumentException());
		
		//WHEN
		//THEN
		assertThrows(UnexpectedRollbackException.class, () -> transactionService.createATransaction(transactionDTO, request));
	}

	@Test
	@Tag("TransactionServiceTest")
	@DisplayName("test createATransaction should throw UnexpectedRollbackException On ResourceNotFoundException")
	public void createATransactionTestShouldThrowUnexpectedRollbackExceptionOnResourceNotFoundException() {
		//GIVEN
		TransactionDTO transactionDTO = new TransactionDTO();
		
		//WHEN
		//THEN
		assertThrows(UnexpectedRollbackException.class, () -> transactionService.createATransaction(transactionDTO, request));
	}
	
	@Test
	@Tag("TransactionServiceTest")
	@DisplayName("test createATransaction should throw UnexpectedRollbackException On OptimisticLockingFailureException")
	public void createATransactionTestShouldThrowUnexpectedRollbackExceptionOnOptimisticLockingFailureException() {
		//GIVEN
		TransactionDTO transactionDTO = new TransactionDTO();
		Transaction transaction = new Transaction();
		when(transactionDTOService.transactionFromNewTransactionDTO(any(TransactionDTO.class))).thenReturn(transaction).thenThrow(new OptimisticLockingFailureException(""));
		
		//WHEN
		//THEN
		assertThrows(UnexpectedRollbackException.class, () -> transactionService.createATransaction(transactionDTO, request));
	}


}
