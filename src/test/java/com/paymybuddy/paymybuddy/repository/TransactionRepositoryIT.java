package com.paymybuddy.paymybuddy.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.paymybuddy.paymybuddy.model.Registered;
import com.paymybuddy.paymybuddy.model.Transaction;

@SpringBootTest
class TransactionRepositoryIT {

	@Autowired
	private TransactionRepository transactionRepository;

	// Needed for testing
	@Autowired
	private RegisteredRepository registeredRepository;

	private Registered registeredA;
	private Registered registeredB;
	private Registered registeredC;
	
	@BeforeEach
	public void setUpPerTest() {
		registeredA = new Registered();
		registeredA.setEmail("aaa@aaa.com");
		registeredA.setPassword("aaaPasswd");
		registeredA.setFirstName("Aaa");
		registeredA.setLastName("AAA");
		registeredA.setBirthDate(LocalDate.parse("01/21/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
		registeredA.setIban("aaaIban");
		registeredA.setBalance(301.50);

		registeredB = new Registered();
		registeredB.setEmail("bbb@bbb.com");
		registeredB.setPassword("bbbPasswd");
		registeredB.setFirstName("Bbb");
		registeredB.setLastName("BBB");
		registeredB.setBirthDate(LocalDate.parse("02/22/1992", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
		registeredB.setIban("bbbIban");
		registeredB.setBalance(200);

		registeredC = new Registered();
		registeredC.setEmail("ccc@ccc.com");
		registeredC.setPassword("cccPasswd");
		registeredC.setFirstName("Ccc");
		registeredC.setLastName("CCC");
		registeredC.setBirthDate(LocalDate.parse("03/23/1993", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
		registeredC.setIban("cccIban");
		registeredC.setBalance(100);
		
		registeredRepository.saveAndFlush(registeredA);
		registeredRepository.saveAndFlush(registeredB);
		registeredRepository.saveAndFlush(registeredC);
	}

	@AfterEach
	public void undefPerTest() {
		transactionRepository.deleteAll();
		registeredRepository.deleteAll();
		registeredA = null;
		registeredB = null;
		registeredC = null;
	}

	@Test
	@Tag("TransactionRepositoryIT")
	@DisplayName("Transactions A send to B and C merge cascade should update Registered balances")
	@Transactional
	public void transactionsASendToBAndCMergeCascadeShouldUpdateRegisteredBalances() {
		// GIVEN
		Transaction transactionAtoB = new Transaction();
		transactionAtoB.setDateTime(LocalDateTime.now());
		transactionAtoB.setAmount(100);
		transactionAtoB.monetize();
		transactionAtoB = transactionRepository.saveAndFlush(transactionAtoB);
		
		Transaction transactionAtoC = new Transaction();
		transactionAtoC.setDateTime(LocalDateTime.now());
		transactionAtoC.setAmount(200);
		transactionAtoC.monetize();
		transactionAtoC = transactionRepository.saveAndFlush(transactionAtoC);
		
		transactionAtoB.setSender(registeredA);
		transactionAtoB.setReceiver(registeredB);
		transactionAtoC.setSender(registeredA);
		transactionAtoC.setReceiver(registeredC);
		registeredA.setBalance(0);
		registeredB.setBalance(300.50);
		registeredC.setBalance(301);

		// WHEN
		transactionRepository.saveAndFlush(transactionAtoB);
		transactionRepository.saveAndFlush(transactionAtoC);

		// THEN
		assertThat(transactionRepository.findAll()).containsExactlyInAnyOrder(transactionAtoB, transactionAtoC);

		registeredA = registeredRepository.findById("aaa@aaa.com").get();
		registeredB = registeredRepository.findById("bbb@bbb.com").get();
		registeredC = registeredRepository.findById("ccc@ccc.com").get();

		assertThat(registeredA.getBalance()).isEqualTo(0d);
		assertThat(registeredB.getBalance()).isEqualTo(300.50);
		assertThat(registeredC.getBalance()).isEqualTo(301d);
	}

	@Test
	@Tag("TransactionRepositoryIT")
	@DisplayName("After A sended to B and C all remove from application, FK for all should be null")
	@Transactional // object references an unsaved transient instance
	public void afterASendedToBandCAllremoveFromApplicationSoAllFKShouldBeNull() {
		// GIVEN
		Transaction transactionAtoB = new Transaction();
		transactionAtoB.setDateTime(LocalDateTime.now());
		transactionAtoB.setAmount(100);
		transactionAtoB.monetize();
		transactionAtoB = transactionRepository.saveAndFlush(transactionAtoB);
		
		Transaction transactionAtoC = new Transaction();
		transactionAtoC.setDateTime(LocalDateTime.now());
		transactionAtoC.setAmount(200);
		transactionAtoC.monetize();
		transactionAtoC = transactionRepository.saveAndFlush(transactionAtoC);
		
		transactionAtoB.setSender(registeredA);
		transactionAtoB.setReceiver(registeredB);
		transactionAtoC.setSender(registeredA);
		transactionAtoC.setReceiver(registeredC);

		transactionRepository.saveAndFlush(transactionAtoB);
		transactionRepository.saveAndFlush(transactionAtoC);

		// WHEN
		// saveAndFlush the transient instance before flushing
		
		registeredRepository.deleteById("aaa@aaa.com");
		registeredRepository.deleteById("bbb@bbb.com");
		registeredRepository.deleteById("ccc@ccc.com");
	
		transactionAtoB.setSender(null);
		transactionAtoB.setReceiver(null);
		transactionAtoC.setSender(null);
		transactionAtoC.setReceiver(null);		
		
		transactionRepository.saveAndFlush(transactionAtoB);
		transactionRepository.saveAndFlush(transactionAtoC);		

		// THEN
		assertThat(transactionRepository.findAll()).containsExactlyInAnyOrder(transactionAtoB, transactionAtoC);
		transactionRepository.findAll().forEach(transaction -> assertThat(transaction.getSender()).isNull());
		transactionRepository.findAll().forEach(transaction -> assertThat(transaction.getReceiver()).isNull());
	}
	
	@Test
	@Tag("TransactionRepositoryIT")
	@DisplayName("Test feeSumForARegisteredBetweenDate should return 10.0")
	@Transactional
	public void testFeeSumForARegisteredBetweenDateShouldReturnTen() {
		// GIVEN
		Calendar dateTransaction = GregorianCalendar.getInstance();
		dateTransaction.set(Calendar.DATE, 1); // set date at the begin of month
		dateTransaction.add(Calendar.MONTH, -3); // three months ago, add rule example : 01/01/2022 Calling add(Calendar.MONTH,-1) sets the calendar to 01/12/2021

		Calendar dateMonthLimit = (GregorianCalendar) dateTransaction.clone();
		dateMonthLimit.set(Calendar.HOUR_OF_DAY, 0);
		dateMonthLimit.set(Calendar.MINUTE, 0);
		dateMonthLimit.set(Calendar.SECOND, 0);
		dateMonthLimit.set(Calendar.MILLISECOND, 0);		
		LocalDateTime beginDate = LocalDateTime.ofInstant(dateMonthLimit.toInstant(), ZoneId.systemDefault()); //(dateMonthLimit);
		dateMonthLimit.add(Calendar.MONTH, 1);
		LocalDateTime endDate = LocalDateTime.ofInstant(dateMonthLimit.toInstant(), ZoneId.systemDefault());
		LocalDateTime dateTimeTransaction = null;
		for (int i = 1; i <= 4; i++) { // loops 4 times
			dateTimeTransaction = LocalDateTime.ofInstant(dateTransaction.toInstant(), ZoneId.systemDefault());
			
			Transaction transactionAtoB = new Transaction();
			transactionAtoB.setDateTime(dateTimeTransaction);
			transactionAtoB.setAmount(100 * i); // 100+200+300+400 = 1000 fee = 5 A
			transactionAtoB.monetize();
			transactionAtoB = transactionRepository.saveAndFlush(transactionAtoB);
			transactionAtoB.setSender(registeredA);
			transactionAtoB.setReceiver(registeredB);
			transactionRepository.saveAndFlush(transactionAtoB);

			Transaction transactionAtoC = new Transaction();
			transactionAtoC.setDateTime(dateTimeTransaction);
			transactionAtoC.setAmount(100 * i); // 100+200+300+400 = 1000 fee = 5 A
			transactionAtoC.monetize();
			transactionAtoC = transactionRepository.saveAndFlush(transactionAtoC);
			transactionAtoC.setSender(registeredA);
			transactionAtoC.setReceiver(registeredC);
			transactionRepository.saveAndFlush(transactionAtoC);

			Transaction transactionBtoC = new Transaction();
			transactionBtoC.setDateTime(dateTimeTransaction);
			transactionBtoC.setAmount(100 * i); // 100+200+300+400 = 1000 fee = 5 B
			transactionBtoC.monetize();
			transactionBtoC = transactionRepository.saveAndFlush(transactionBtoC);
			transactionBtoC.setSender(registeredB);
			transactionBtoC.setReceiver(registeredC);
			transactionRepository.saveAndFlush(transactionBtoC);

			dateTransaction.roll(Calendar.DATE, 8); // 1, 9, 17, 25, (33) Roll rule : Larger fields (here MONTH) are unchanged after
													// the call.
		}
		dateTransaction.set(Calendar.DATE, 4);
		dateTransaction.add(Calendar.MONTH, 1);
		dateTimeTransaction = LocalDateTime.ofInstant(dateTransaction.toInstant(), ZoneId.systemDefault());
		Transaction transactionAtoB = new Transaction();
		transactionAtoB.setDateTime(dateTimeTransaction);
		transactionAtoB.setAmount(500);
		transactionAtoB.monetize();
		transactionAtoB = transactionRepository.saveAndFlush(transactionAtoB);
		transactionAtoB.setSender(registeredA);
		transactionAtoB.setReceiver(registeredB);
		transactionRepository.saveAndFlush(transactionAtoB);

		Transaction transactionAtoC = new Transaction();
		transactionAtoC.setDateTime(dateTimeTransaction);
		transactionAtoC.setAmount(500);
		transactionAtoC.monetize();
		transactionAtoC = transactionRepository.saveAndFlush(transactionAtoC);
		transactionAtoC.setSender(registeredA);
		transactionAtoC.setReceiver(registeredC);
		transactionRepository.saveAndFlush(transactionAtoC);

		Transaction transactionBtoC = new Transaction();
		transactionBtoC.setDateTime(dateTimeTransaction);
		transactionBtoC.setAmount(500);
		transactionBtoC.monetize();
		transactionBtoC = transactionRepository.saveAndFlush(transactionBtoC);
		transactionBtoC.setSender(registeredB);
		transactionBtoC.setReceiver(registeredC);
		transactionRepository.saveAndFlush(transactionBtoC);

		// WHEN
		double result = transactionRepository.feeSumForARegisteredBetweenDate(beginDate, endDate, "aaa@aaa.com");
		double resultAll = transactionRepository.feeSumBetweenDate(beginDate, endDate);
		// THEN
		assertThat(result).isEqualTo(10.0);
		assertThat(resultAll).isEqualTo(15.0);
	}

	@Test
	@Tag("TransactionRepositoryIT")
	@DisplayName("Test findAllTransactionsByEmailSenderOrReceiver should return expected pages")
	@Transactional
	public void testFindAllTransactionsByEmailSenderOrReceiverShouldReturnExpectedPages() {
		// GIVEN
		Calendar dateTransaction = GregorianCalendar.getInstance();
		dateTransaction.set(Calendar.DATE, 1); // set date at the begin of month
		dateTransaction.add(Calendar.MONTH, -3); // three months ago, add rule example : 01/01/2022 Calling add(Calendar.MONTH,
													// -1) sets the calendar to 01/12/2021
		List<Transaction> transactionsBExpected = new ArrayList<>();
		for (int i = 1; i <= 5; i++) { // loops 5 times
			LocalDateTime dateTimeTransaction = LocalDateTime.ofInstant(dateTransaction.toInstant(), ZoneId.systemDefault());

			Transaction transactionAtoB = new Transaction();
			transactionAtoB.setDateTime(dateTimeTransaction);
			transactionAtoB.setAmount(100 * i);
			transactionAtoB.monetize();
			transactionAtoB = transactionRepository.saveAndFlush(transactionAtoB);
			transactionAtoB.setSender(registeredA);
			transactionAtoB.setReceiver(registeredB);
			transactionAtoB = transactionRepository.saveAndFlush(transactionAtoB);
			transactionsBExpected.add(transactionAtoB);
	
			dateTransaction.add(Calendar.HOUR_OF_DAY, 1);
			dateTimeTransaction = LocalDateTime.ofInstant(dateTransaction.toInstant(), ZoneId.systemDefault());

			Transaction transactionBtoC = new Transaction();
			transactionBtoC.setDateTime(dateTimeTransaction);
			transactionBtoC.setAmount(100 * i);
			transactionBtoC.monetize();
			transactionBtoC = transactionRepository.saveAndFlush(transactionBtoC);
			transactionBtoC.setSender(registeredB);
			transactionBtoC.setReceiver(registeredC);
			transactionBtoC = transactionRepository.saveAndFlush(transactionBtoC);
			transactionsBExpected.add(transactionBtoC);

			dateTransaction.add(Calendar.HOUR_OF_DAY, 1);
			dateTransaction.add(Calendar.DATE, 8); // 1, 9, 17, 25, next month 1 time
		}
		transactionsBExpected.sort((t1,t2) -> t2.getDateTime().compareTo(t1.getDateTime())); //descending date order
		Pageable pageRequest = PageRequest.of(0, 3, Sort.by("date_time").descending());
		List<Page<Transaction>> pagesTransactionsBResult = new ArrayList<>();

		// WHEN
		Page<Transaction> pageTransaction;
		do {
			pageTransaction = transactionRepository.findAllTransactionsByEmailSenderOrReceiver("bbb@bbb.com", pageRequest);
			pagesTransactionsBResult.add(pageTransaction);
			pageRequest = pageTransaction.nextOrLastPageable();
		} while (pageTransaction.hasNext());

		//THEN
		assertThat(pagesTransactionsBResult.get(0).getContent()).containsExactlyElementsOf(transactionsBExpected.subList(0, 3));
		assertThat(pagesTransactionsBResult.get(1).getContent()).containsExactlyElementsOf(transactionsBExpected.subList(3, 6));
		assertThat(pagesTransactionsBResult.get(2).getContent()).containsExactlyElementsOf(transactionsBExpected.subList(6, 9));
		assertThat(pagesTransactionsBResult.get(3).getContent()).containsExactlyElementsOf(transactionsBExpected.subList(9, 10));		
	}
}
