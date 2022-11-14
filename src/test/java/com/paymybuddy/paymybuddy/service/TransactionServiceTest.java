package com.paymybuddy.paymybuddy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.MappingException;
import org.modelmapper.spi.ErrorMessage;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.UnexpectedRollbackException;
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
	
	@Mock
	private RequestService requestService;
	
	@Mock
	private DateTimePatternProperties dateStringPattern;
	
	private WebRequest request = null;
	
	@Nested
	@Tag("createATransactionTests")
	@DisplayName("Tests for method createATransaction")
	class CreateATransactionTests{
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
		@DisplayName("test createATransaction should throw UnexpectedRollbackException On OptimisticLockingFailureException")
		public void createATransactionTestShouldThrowUnexpectedRollbackExceptionOnOptimisticLockingFailureException() {
			//GIVEN
			TransactionDTO transactionDTO = new TransactionDTO("aaa@aaa.com", "bbb@bbb.com");
			Transaction transaction = new Transaction(LocalDateTime.now(), 100.0);
			when(transactionDTOService.transactionFromNewTransactionDTO(any(TransactionDTO.class))).thenReturn(transaction);
			when(transactionRepository.save(any(Transaction.class))).thenThrow(new OptimisticLockingFailureException(""));
			
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
			Transaction transaction = new Transaction(LocalDateTime.now(), 100.0);
			when(transactionDTOService.transactionFromNewTransactionDTO(any(TransactionDTO.class))).thenReturn(transaction);
			when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
			when(registeredRepository.findById(any(String.class))).thenThrow(new IllegalArgumentException());
			
			//WHEN
			//THEN
			assertThrows(UnexpectedRollbackException.class, () -> transactionService.createATransaction(transactionDTO, request));
		}
	
		@Test
		@Tag("TransactionServiceTest")
		@DisplayName("test createATransaction should throw UnexpectedRollbackException On ResourceNotFoundException for Sender")
		public void createATransactionTestShouldThrowUnexpectedRollbackExceptionOnResourceNotFoundExceptionForSender() {
			//GIVEN
			TransactionDTO transactionDTO = new TransactionDTO("aaa@aaa.com", "bbb@bbb.com");
			Transaction transaction = new Transaction(LocalDateTime.now(), 100.0);
			when(transactionDTOService.transactionFromNewTransactionDTO(any(TransactionDTO.class))).thenReturn(transaction);
			when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
			when(registeredRepository.findById(any(String.class))).thenReturn(Optional.ofNullable(null));
			
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> transactionService.createATransaction(transactionDTO, request))
					.getMessage()).isEqualTo("Registered sender not found for transaction");
		}
		
		@Test
		@Tag("TransactionServiceTest")
		@DisplayName("test createATransaction should throw InsufficentFundsException")
		public void createATransactionTestShouldThrowInsufficentFundsException() {
			//GIVEN
			TransactionDTO transactionDTO = new TransactionDTO("aaa@aaa.com", "bbb@bbb.com");
			Transaction transaction = new Transaction(LocalDateTime.now(), 100.0);
			Registered registeredASender = new Registered("aaa@aaa.com", "aaaPasswd", "Aaa", "AAA", LocalDate.parse("01/01/1991", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "aaaIban");
			registeredASender.setBalance(100.0);
			when(transactionDTOService.transactionFromNewTransactionDTO(any(TransactionDTO.class))).thenReturn(transaction);
			when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
			when(registeredRepository.findById(any(String.class))).thenReturn(Optional.ofNullable(registeredASender));
			when(dateStringPattern.getLocalLanguage()).thenReturn("en");
			
			//WHEN
			//THEN
			assertThat(assertThrows(InsufficentFundsException.class,
					() -> transactionService.createATransaction(transactionDTO, request))
					.getMessage()).isEqualTo("Insufficient funds for transaction : you need to transfert : 0.50 from bank");
		}
		
		@Test
		@Tag("TransactionServiceTest")
		@DisplayName("test createATransaction should throw UnexpectedRollbackException On ResourceNotFoundException for Receiver")
		public void createATransactionTestShouldThrowUnexpectedRollbackExceptionOnResourceNotFoundExceptionForReceiver() {
			//GIVEN
			TransactionDTO transactionDTO = new TransactionDTO("aaa@aaa.com", "bbb@bbb.com");
			Transaction transaction = new Transaction(LocalDateTime.now(), 100.0);
			Registered registeredASender = new Registered("aaa@aaa.com", "aaaPasswd", "Aaa", "AAA", LocalDate.parse("01/01/1991", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "aaaIban");
			registeredASender.setBalance(100.5);
			when(transactionDTOService.transactionFromNewTransactionDTO(any(TransactionDTO.class))).thenReturn(transaction);
			when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
			when(registeredRepository.findById(any(String.class))).thenReturn(Optional.ofNullable(registeredASender)).thenReturn(Optional.ofNullable(null));
			
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class, () -> transactionService.createATransaction(transactionDTO, request)).getMessage()).isEqualTo("Registered receiver not found for transaction");
		}
	}
	
	@Nested
	@Tag("getRegisteredAllTransactionTests")
	@DisplayName("Tests for method getRegisteredAllTransaction")
	class GetRegisteredAllTransactionTests {

		private Pageable pageRequest = PageRequest.of(0, 3);

		@Test
		@Tag("TransactionServiceTest")
		@DisplayName("test getRegisteredAllTransaction should give Page with TranscationDTO sended and received for email")
		public void getRegisteredAllTransactionTestShouldGivePageWithTransactionDTOSendedAndReceivedForEmail() {
			//GIVEN
			Registered registeredA = new Registered("aaa@aaa.com", "aaaPasswd", "Aaa", "AAA", LocalDate.parse("01/01/1991", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "aaaIban");
			Registered registeredB = new Registered("bbb@bbb.com", "bbbPasswd", "Bbb", "BBB", LocalDate.parse("02/02/1992", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "bbbIban");
			Transaction transactionAtoB = new Transaction(LocalDateTime.now(), 100);
			transactionAtoB.setSender(registeredA);
			transactionAtoB.setReceiver(registeredB);
			Transaction transactionBtoA = new Transaction(LocalDateTime.now(), 100);
			transactionBtoA.setSender(registeredB);
			transactionBtoA.setReceiver(registeredA);
			TransactionDTO transactionAtoBDTO = new TransactionDTO("aaa@aaa.com", "bbb@bbb.com");
			transactionAtoBDTO.setAmount("-100.00");
			transactionAtoBDTO.setFee("-0.50");
			transactionAtoBDTO.setReceiver(false);
			TransactionDTO transactionBtoADTO = new TransactionDTO("bbb@bbb.com", "aaa@aaa.com");
			transactionBtoADTO.setAmount("100.00");
			transactionBtoADTO.setFee("0.00");
			transactionBtoADTO.setReceiver(true);
			List<TransactionDTO> transactionsDTOExpected = Arrays.asList(transactionAtoBDTO, transactionBtoADTO);
			when(transactionRepository.findAllTransactionsByEmailSenderOrReceiver(any(String.class), any(Pageable.class)))
				.thenReturn(new PageImpl<Transaction>(Arrays.asList(transactionAtoB, transactionBtoA), pageRequest, 2));
			when(transactionDTOService.transactionToDTOSender(any(Transaction.class))).thenReturn(transactionAtoBDTO);
			when(transactionDTOService.transactionToDTOReceiver(any(Transaction.class))).thenReturn(transactionBtoADTO);
			
			//WHEN
			Page<TransactionDTO> pageTransactionDTO = transactionService.getRegisteredAllTransaction("aaa@aaa.com", pageRequest, request);
			
			//THEN
			assertThat(pageTransactionDTO.getContent()).containsExactlyElementsOf(transactionsDTOExpected);
			assertThat(pageTransactionDTO.getPageable().getPageSize()).isEqualTo(3);
		}
		
		@Test
		@Tag("TransactionServiceTest")
		@DisplayName("test getRegisteredAllTransaction should throw UnexpectedRollbackException On IllegalArgumentException")
		public void getRegisteredAllTransactionTestShouldThrowUnexpectedRollbackExceptionOnIllegalArgumentException() {
			//GIVEN
			when(transactionRepository.findAllTransactionsByEmailSenderOrReceiver(any(String.class), any(Pageable.class)))
			.thenThrow(new IllegalArgumentException());
		
			//WHEN
			//THEN
			assertThrows(UnexpectedRollbackException.class, () -> transactionService.getRegisteredAllTransaction("aaa@aaa.com", pageRequest, request));
		}
	}
}
