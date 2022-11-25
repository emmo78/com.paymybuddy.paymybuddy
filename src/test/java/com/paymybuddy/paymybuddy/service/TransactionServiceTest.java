package com.paymybuddy.paymybuddy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.MappingException;
import org.modelmapper.spi.ErrorMessage;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.paymybuddy.paymybuddy.configuration.DateTimePatternProperties;
import com.paymybuddy.paymybuddy.dto.TransactionDTO;
import com.paymybuddy.paymybuddy.dto.service.TransactionDTOService;
import com.paymybuddy.paymybuddy.exception.InsufficentFundsException;
import com.paymybuddy.paymybuddy.model.Registered;
import com.paymybuddy.paymybuddy.model.Transaction;
import com.paymybuddy.paymybuddy.repository.RegisteredRepository;
import com.paymybuddy.paymybuddy.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

	@InjectMocks
	private TransactionServiceImpl transactionService;
	
	@Mock 
	private TransactionDTOService transactionDTOService;
	
	@Mock
	private TransactionRepository transactionRepository;
	
	@Mock
	private RegisteredRepository registeredRepository;
	
	@Spy
	private RequestService requestService = new RequestServiceImpl();
	
	@Mock
	private DateTimePatternProperties dateStringPattern;
	
	private MockHttpServletRequest requestMock;
	private WebRequest request;
	
	@Nested
	@Tag("createATransactionTests")
	@DisplayName("Tests for method createATransaction")
	@TestInstance(Lifecycle.PER_CLASS)
	class CreateATransactionTests {
		
		private TransactionDTO transactionDTO;
		private Transaction transaction;

		@BeforeAll
		public void setUpForAllCreateATransactionTests() {
			requestMock = new MockHttpServletRequest();
			requestMock.setServerName("http://localhost:8080");
			requestMock.setRequestURI("/createTransaction");
			request = new ServletWebRequest(requestMock);
		}

		@AfterAll
		public void unSetForAllCreateATransactionTests() {
			requestMock = null;
			request = null;
		}
		
		@BeforeEach
		public void setUpForEachTests() {
			transactionDTO = new TransactionDTO("aaa@aaa.com", "bbb@bbb.com");
			transaction = new Transaction();
			transaction.setDateTime(LocalDateTime.now());
			transaction.setAmount(100);
			transaction.monetize();

		}
		
		@AfterEach
		public void unSetForEachTests() {
			transactionDTO = null;
			transaction = null;
		}
		
		@Test
		@Tag("TransactionServiceTest")
		@DisplayName("test createATransaction should throw UnexpectedRollbackException on MappingException")
		public void createATransactionTestShouldThrowUnexpectedRollbackExceptionOnMappingException() {
			//GIVEN
			when(transactionDTOService.transactionFromNewTransactionDTO(any(TransactionDTO.class))).thenThrow(new MappingException(new ArrayList<ErrorMessage>()));
			
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
				() -> transactionService.createATransaction(transactionDTO, request))
				.getMessage()).isEqualTo("Error while creating money transfer");
		}
	
		@Test
		@Tag("TransactionServiceTest")
		@DisplayName("test createATransaction should throw UnexpectedRollbackException on OptimisticLockingFailureException")
		public void createATransactionTestShouldThrowUnexpectedRollbackExceptionOnOptimisticLockingFailureException() {
			//GIVEN
			when(transactionDTOService.transactionFromNewTransactionDTO(any(TransactionDTO.class))).thenReturn(transaction);
			when(transactionRepository.save(any(Transaction.class))).thenThrow(new OptimisticLockingFailureException(""));
			
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
				() -> transactionService.createATransaction(transactionDTO, request))
				.getMessage()).isEqualTo("Error while creating money transfer");
		}
		
		@Test
		@Tag("TransactionServiceTest")
		@DisplayName("test createATransaction should throw UnexpectedRollbackException on IllegalArgumentException")
		public void createATransactionTestShouldThrowUnexpectedRollbackExceptionOnIllegalArgumentException() {
			//GIVEN
			when(transactionDTOService.transactionFromNewTransactionDTO(any(TransactionDTO.class))).thenReturn(transaction);
			when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
			when(registeredRepository.findById(any(String.class))).thenThrow(new IllegalArgumentException());
			
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
				() -> transactionService.createATransaction(transactionDTO, request))
				.getMessage()).isEqualTo("Error while creating money transfer");	
		}
	
		@Test
		@Tag("TransactionServiceTest")
		@DisplayName("test createATransaction should throw UnexpectedRollbackException on ResourceNotFoundException for Sender")
		public void createATransactionTestShouldThrowUnexpectedRollbackExceptionOnResourceNotFoundExceptionForSender() {
			//GIVEN
			when(transactionDTOService.transactionFromNewTransactionDTO(any(TransactionDTO.class))).thenReturn(transaction);
			when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
			when(registeredRepository.findById(anyString())).thenReturn(Optional.ofNullable(null));
			
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
				() -> transactionService.createATransaction(transactionDTO, request))
				.getMessage()).isEqualTo("Error while creating money transfer");
		}
		
		@Test
		@Tag("TransactionServiceTest")
		@DisplayName("test createATransaction should throw InsufficentFundsException")
		public void createATransactionTestShouldThrowInsufficentFundsException() {
			//GIVEN
			Registered registeredASender = new Registered();
			registeredASender.setEmail("aaa@aaa.com");
			registeredASender.setPassword("aaaPasswd");
			registeredASender.setFirstName("Aaa");
			registeredASender.setLastName("AAA");
			registeredASender.setBirthDate(LocalDate.parse("01/21/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
			registeredASender.setIban("aaaIban");
			registeredASender.setBalance(100.0);
			when(transactionDTOService.transactionFromNewTransactionDTO(any(TransactionDTO.class))).thenReturn(transaction);
			when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
			when(registeredRepository.findById(anyString())).thenReturn(Optional.ofNullable(registeredASender));
			when(dateStringPattern.getLocalLanguage()).thenReturn("en");
			
			//WHEN
			//THEN
			assertThat(assertThrows(InsufficentFundsException.class,
				() -> transactionService.createATransaction(transactionDTO, request))
				.getMessage()).isEqualTo("Insufficient funds for transaction : you need to transfert : 0.50 from bank");
		}
		
		@Test
		@Tag("TransactionServiceTest")
		@DisplayName("test createATransaction should throw UnexpectedRollbackException on ResourceNotFoundException for Receiver")
		public void createATransactionTestShouldThrowUnexpectedRollbackExceptionOnResourceNotFoundExceptionForReceiver() {
			//GIVEN
			Registered registeredASender = new Registered();
			registeredASender.setEmail("aaa@aaa.com");
			registeredASender.setPassword("aaaPasswd");
			registeredASender.setFirstName("Aaa");
			registeredASender.setLastName("AAA");
			registeredASender.setBirthDate(LocalDate.parse("01/21/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
			registeredASender.setIban("aaaIban");
			registeredASender.setBalance(100.5);
			when(transactionDTOService.transactionFromNewTransactionDTO(any(TransactionDTO.class))).thenReturn(transaction);
			when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
			when(registeredRepository.findById(anyString())).thenReturn(Optional.ofNullable(registeredASender)).thenReturn(Optional.ofNullable(null));
			
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
				() -> transactionService.createATransaction(transactionDTO, request))
				.getMessage()).isEqualTo("Error while creating money transfer");
		}
		
		@Test
		@Tag("TransactionServiceTest")
		@DisplayName("test createATransaction should throw UnexpectedRollbackException on any RuntimeException")
		public void createATransactionTestShouldThrowUnexpectedRollbackExceptionOnAnyRuntimeException() {
			//GIVEN
			Registered registeredASender = new Registered();
			registeredASender.setEmail("aaa@aaa.com");
			registeredASender.setPassword("aaaPasswd");
			registeredASender.setFirstName("Aaa");
			registeredASender.setLastName("AAA");
			registeredASender.setBirthDate(LocalDate.parse("01/21/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
			registeredASender.setIban("aaaIban");
			registeredASender.setBalance(100.5);
			when(transactionDTOService.transactionFromNewTransactionDTO(any(TransactionDTO.class))).thenReturn(transaction);
			when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
			when(registeredRepository.findById(anyString())).thenThrow(new RuntimeException());
			
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
				() -> transactionService.createATransaction(transactionDTO, request))
				.getMessage()).isEqualTo("Error while creating money transfer");
		}
	}
	
	@Nested
	@Tag("getRegisteredAllTransactionTests")
	@DisplayName("Tests for method getRegisteredAllTransaction")
	@TestInstance(Lifecycle.PER_CLASS)
	class GetRegisteredAllTransactionTests {

		private Pageable pageRequest;

		@BeforeAll
		public void setUpForAllCreateATransactionTests() {
			pageRequest = PageRequest.of(0, 3);
			requestMock = new MockHttpServletRequest();
			requestMock.setServerName("http://localhost:8080");
			requestMock.setRequestURI("/transfert?email=aaa@aaa.com");
			request = new ServletWebRequest(requestMock);
		}

		@AfterAll
		public void unSetForAllCreateATransactionTests() {
			pageRequest = null;
			requestMock = null;
			request = null;
		}

		@Test
		@Tag("TransactionServiceTest")
		@DisplayName("test getRegisteredAllTransaction should give Page with TranscationDTO sended and received for email")
		public void getRegisteredAllTransactionTestShouldGivePageWithTransactionDTOSendedAndReceivedForEmail() {
			//GIVEN
			Registered registeredA = new Registered();
			registeredA.setEmail("aaa@aaa.com");
			registeredA.setPassword("aaaPasswd");
			registeredA.setFirstName("Aaa");
			registeredA.setLastName("AAA");
			registeredA.setBirthDate(LocalDate.parse("01/21/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
			registeredA.setIban("aaaIban");
			registeredA.setBalance(100);

			Registered registeredB = new Registered();
			registeredB.setEmail("bbb@bbb.com");
			registeredB.setPassword("bbbPasswd");
			registeredB.setFirstName("Bbb");
			registeredB.setLastName("BBB");
			registeredB.setBirthDate(LocalDate.parse("02/22/1992", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
			registeredB.setIban("bbbIban");
			registeredB.setBalance(200);
			
			Transaction transactionAtoB = new Transaction();
			transactionAtoB.setDateTime(LocalDateTime.now());
			transactionAtoB.setAmount(100);
			transactionAtoB.monetize();
			transactionAtoB.setSender(registeredA);
			transactionAtoB.setReceiver(registeredB);
			Transaction transactionBtoA = new Transaction();
			transactionBtoA.setDateTime(LocalDateTime.now());
			transactionBtoA.setAmount(100);
			transactionBtoA.monetize();
			transactionBtoA.setSender(registeredB);
			transactionBtoA.setReceiver(registeredA);
			Transaction transactionAtoNull = new Transaction();
			transactionAtoNull.setDateTime(LocalDateTime.now());
			transactionAtoNull.setAmount(100);
			transactionAtoNull.monetize();
			transactionAtoNull.setSender(registeredA);
			transactionAtoNull.setReceiver(null);
			Transaction transactionNulltoA = new Transaction();
			transactionNulltoA.setDateTime(LocalDateTime.now());
			transactionNulltoA.setAmount(100);
			transactionNulltoA.monetize();
			transactionNulltoA.setSender(null);
			transactionNulltoA.setReceiver(registeredA);
			
			TransactionDTO transactionAtoBDTO = new TransactionDTO("aaa@aaa.com", "bbb@bbb.com");
			transactionAtoBDTO.setAmount("-100.00");
			transactionAtoBDTO.setFee("-0.50");
			transactionAtoBDTO.setReceiver(false);
			TransactionDTO transactionBtoADTO = new TransactionDTO("bbb@bbb.com", "aaa@aaa.com");
			transactionBtoADTO.setAmount("100.00");
			transactionBtoADTO.setFee("0.00");
			transactionBtoADTO.setReceiver(true);
			TransactionDTO transactionAtoNullDTO = new TransactionDTO("aaa@aaa.com", null);
			transactionAtoNullDTO.setAmount("-100.00");
			transactionAtoNullDTO.setFee("-0.50");
			transactionAtoNullDTO.setReceiver(false);
			TransactionDTO transactionNulltoADTO = new TransactionDTO(null, "aaa@aaa.com");
			transactionNulltoADTO.setAmount("100.00");
			transactionNulltoADTO.setFee("0.00");
			transactionNulltoADTO.setReceiver(true);
			
			List<TransactionDTO> transactionsDTOExpected = Arrays.asList(transactionAtoBDTO, transactionBtoADTO, transactionAtoNullDTO, transactionNulltoADTO);
			when(transactionRepository.findAllTransactionsByEmailSenderOrReceiver(anyString(), any(Pageable.class)))
				.thenReturn(new PageImpl<Transaction>(Arrays.asList(transactionAtoB, transactionBtoA, transactionAtoNull, transactionNulltoA), pageRequest, 4));
			when(transactionDTOService.transactionToDTOSender(any(Transaction.class))).thenReturn(transactionAtoBDTO).thenReturn(transactionAtoNullDTO);
			when(transactionDTOService.transactionToDTOReceiver(any(Transaction.class))).thenReturn(transactionBtoADTO).thenReturn(transactionNulltoADTO);
			
			//WHEN
			Page<TransactionDTO> pageTransactionDTO = transactionService.getRegisteredAllTransaction("aaa@aaa.com", pageRequest, request);
			
			//THEN
			assertThat(pageTransactionDTO.getContent()).containsExactlyElementsOf(transactionsDTOExpected);
			assertThat(pageTransactionDTO.getPageable().getPageSize()).isEqualTo(3);
		}
		
		@Test
		@Tag("TransactionServiceTest")
		@DisplayName("test getRegisteredAllTransaction should throw UnexpectedRollbackException on IllegalArgumentException")
		public void getRegisteredAllTransactionTestShouldThrowUnexpectedRollbackExceptionOnIllegalArgumentException() {
			//GIVEN
			when(transactionRepository.findAllTransactionsByEmailSenderOrReceiver(anyString(), any(Pageable.class)))
			.thenThrow(new IllegalArgumentException());
		
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
				() -> transactionService.getRegisteredAllTransaction("aaa@aaa.com", pageRequest, request))
				.getMessage()).isEqualTo("Error while looking for your money transactions");
		}
		
		@Test
		@Tag("TransactionServiceTest")
		@DisplayName("test getRegisteredAllTransaction should throw UnexpectedRollbackException on any RuntimeException")
		public void getRegisteredAllTransactionTestShouldThrowUnexpectedRollbackExceptionOnAnyuntimeException() {
			//GIVEN
			when(transactionRepository.findAllTransactionsByEmailSenderOrReceiver(anyString(), any(Pageable.class)))
			.thenThrow(new RuntimeException());
		
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
				() -> transactionService.getRegisteredAllTransaction("aaa@aaa.com", pageRequest, request))
				.getMessage()).isEqualTo("Error while looking for your money transactions");
		}

	}
}
