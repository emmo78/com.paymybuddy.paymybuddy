package com.paymybuddy.paymybuddy.dto.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.paymybuddy.paymybuddy.configuration.DateTimePatternProperties;
import com.paymybuddy.paymybuddy.dto.TransactionDTO;
import com.paymybuddy.paymybuddy.model.Registered;
import com.paymybuddy.paymybuddy.model.Transaction;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class TransactionDTOServiceImplTest {
	
	@Autowired
	@InjectMocks
	private TransactionDTOServiceImpl transactionDTOService;
	
	@Mock
	private DateTimePatternProperties dateStringPattern;

	@Test
	@DisplayName("test transactionToDTOSender should have receiver false and negative amount")
	public void transactionToDTOSenderTestShouldHaveReceiverFalseAndNegativeAmount() {
		//GIVEN
		Calendar calTransaction = new GregorianCalendar(2022, 9, 23, 18, 43, 55); //Month value is 0-based. e.g., 0 for January.
		LocalDateTime dateTimeTransaction = LocalDateTime.ofInstant(calTransaction.toInstant(), ZoneId.systemDefault());
		Registered registeredASender = new Registered("aaa@aaa.com", "aaaPasswd", "Aaa", "AAA", LocalDate.parse("01/21/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")), "aaaIban");
		Registered registeredBReceiver = new Registered("bbb@bbb.com", "bbbPasswd", "Bbb", "BBB", LocalDate.parse("02/22/1992", DateTimeFormatter.ofPattern("MM/dd/yyyy")), "bbbIban");
		Transaction transaction = new Transaction(dateTimeTransaction, 100);
		transaction.setSender(registeredASender);
		transaction.setReceiver(registeredBReceiver);
		when(dateStringPattern.getDateTimeStringPattern()).thenReturn("MM/dd/yyyy HH:mm:ss");
		when(dateStringPattern.getLocalLanguage()).thenReturn("en");

		//WHEN
		TransactionDTO transactionDTOresult = transactionDTOService.transactionToDTOSender(transaction);
		
		//THEN
		assertThat(transactionDTOresult).extracting(
				TransactionDTO::getDateTime,
				TransactionDTO::getAmount,
				TransactionDTO::getFee,
				TransactionDTO::getEmailSender,
				TransactionDTO::getEmailReceiver,
				TransactionDTO::isReceiver
				).containsExactly(
						"10/23/2022 18:43:55",
						"-100.00",
						"-0.50",
						"aaa@aaa.com",
						"bbb@bbb.com",
						false
				);
	}

	@Test
	@DisplayName("test transactionToDTOReceiver should have receiver true and fee to zero")
	public void transactionToDTOReceiverTestShouldHaveReceiverTrueAndFeeZero() {
		//GIVEN
		Calendar calTransaction = new GregorianCalendar(2022, 9, 23, 18, 43, 55); //Month value is 0-based. e.g., 0 for January.
		LocalDateTime dateTimeTransaction = LocalDateTime.ofInstant(calTransaction.toInstant(), ZoneId.systemDefault());
		Registered registeredASender = new Registered("aaa@aaa.com", "aaaPasswd", "Aaa", "AAA", LocalDate.parse("01/21/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")), "aaaIban");
		Registered registeredBReceiver = new Registered("bbb@bbb.com", "bbbPasswd", "Bbb", "BBB", LocalDate.parse("02/22/1992", DateTimeFormatter.ofPattern("MM/dd/yyyy")), "bbbIban");
		Transaction transaction = new Transaction(dateTimeTransaction, 100);
		transaction.setSender(registeredASender);
		transaction.setReceiver(registeredBReceiver);
		when(dateStringPattern.getDateTimeStringPattern()).thenReturn("MM/dd/yyyy HH:mm:ss");
		when(dateStringPattern.getLocalLanguage()).thenReturn("en");

		//WHEN
		TransactionDTO transactionDTOresult = transactionDTOService.transactionToDTOReceiver(transaction);
		
		//THEN
		assertThat(transactionDTOresult).extracting(
				TransactionDTO::getDateTime,
				TransactionDTO::getAmount,
				TransactionDTO::getFee,
				TransactionDTO::getEmailSender,
				TransactionDTO::getEmailReceiver,
				TransactionDTO::isReceiver
				).containsExactly(
						"10/23/2022 18:43:55",
						"100.00",
						"0.00",
						"aaa@aaa.com",
						"bbb@bbb.com",
						true
				);
	}
	
	@Test
	@DisplayName("test transactionFromNewTransactionDTO should have LocalDateTime.now() and fee")
	public void transactionFromNewTransactionDTOTestShouldHaveLocalDateTimeNowAndFee() {
		//GIVEN
		TransactionDTO transactionDTO = new TransactionDTO("aaa@aaa.com", "bbb@bbb.com");
		transactionDTO.setAmount("100.00");
		when(dateStringPattern.getLocalLanguage()).thenReturn("en");
		
		//WHEN
		Transaction transactionExpected = transactionDTOService.transactionFromNewTransactionDTO(transactionDTO);
		
		//THEN
		assertThat(transactionExpected).extracting(
				Transaction::getAmount,
				Transaction::getFee,
				transaction -> transaction.getSender().getEmail(),
				transaction -> transaction.getReceiver().getEmail()
				).containsExactly(
						100d,
						0.5,
						"aaa@aaa.com",
						"bbb@bbb.com"
						);
		assertThat(transactionExpected.getDateTime()).isCloseTo(LocalDateTime.now(), within(2, ChronoUnit.SECONDS));
	}
	
}
