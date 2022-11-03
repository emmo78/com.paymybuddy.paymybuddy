package com.paymybuddy.paymybuddy.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.transaction.Transactional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
	RegisteredRepository registeredRepository;

	Registered registeredA;
	Registered registeredB;
	Registered registeredC;

	@BeforeEach
	public void setUpPerTest() {
		registeredA = new Registered("aaa@aaa.com", "aaaPasswd", "Aaa", "AAA", LocalDate.parse("01/01/1991", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "aaaIban");
		registeredB = new Registered("bbb@bbb.com", "bbbPasswd", "Bbb", "BBB", LocalDate.parse("02/02/1992", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "bbbIban");
		registeredC = new Registered("ccc@ccc.com", "cccPasswd", "Ccc", "CCC", LocalDate.parse("03/03/1993", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "cccIban");

		registeredRepository.save(registeredA);
		registeredRepository.save(registeredB);
		registeredRepository.save(registeredC);
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
	@DisplayName("Transactions A send to B and C merge cascade should update their List")
	@Transactional
	void transactionsASendToBAndCMergeCascadeShouldUpdateTheirList() {
		// GIVEN
		Transaction transactionAtoB = new Transaction(LocalDateTime.now(), 100);
		transactionAtoB = transactionRepository.save(transactionAtoB);
		registeredA.addSendedTransaction(transactionAtoB);
		registeredB.addReceivedTransaction(transactionAtoB);

		Transaction transactionAtoC = new Transaction(LocalDateTime.now(), 200);
		transactionAtoC = transactionRepository.save(transactionAtoC);
		registeredA.addSendedTransaction(transactionAtoC);
		registeredC.addReceivedTransaction(transactionAtoC);

		List<Transaction> sendedTransactionsAExpected = Arrays.asList(transactionAtoB, transactionAtoC);

		// WHEN
		transactionRepository.save(transactionAtoB);
		transactionRepository.save(transactionAtoC);

		// THEN
		assertThat(transactionRepository.count()).isEqualTo(2L);

		registeredA = registeredRepository.findById("aaa@aaa.com").get();
		registeredB = registeredRepository.findById("bbb@bbb.com").get();
		registeredC = registeredRepository.findById("ccc@ccc.com").get();

		assertThat(registeredA.getSendedTransactions()).containsExactlyInAnyOrderElementsOf(sendedTransactionsAExpected);
		assertThat(registeredB.getReceivedTransactions()).containsExactlyInAnyOrder(transactionAtoB);
		assertThat(registeredC.getReceivedTransactions()).containsExactlyInAnyOrder(transactionAtoC);
	}

	@Test
	@DisplayName("After A sended to B and C all remove from application, FK for all should be null")
	// @Transactional - object references an unsaved transient instance - save the
	// transient instance before flushing
	void afterASendedToBandCAllremoveFromApplicationSoAllFKShouldBeNull() {
		// GIVEN
		Transaction transactionAtoB = new Transaction(LocalDateTime.now(), 100);
		transactionAtoB = transactionRepository.save(transactionAtoB);
		registeredA.addSendedTransaction(transactionAtoB);
		registeredB.addReceivedTransaction(transactionAtoB);

		Transaction transactionAtoC = new Transaction(LocalDateTime.now(), 200);
		transactionAtoC = transactionRepository.save(transactionAtoC);
		registeredA.addSendedTransaction(transactionAtoC);
		registeredC.addReceivedTransaction(transactionAtoC);

		transactionRepository.save(transactionAtoB);
		transactionRepository.save(transactionAtoC);

		// WHEN
		registeredRepository.deleteById("aaa@aaa.com");
		registeredRepository.deleteById("bbb@bbb.com");
		registeredRepository.deleteById("ccc@ccc.com");

		// THEN
		assertThat(transactionRepository.count()).isEqualTo(2L);
		transactionRepository.findAll().forEach(transaction -> assertThat(transaction.getSender()).isNull());
		transactionRepository.findAll().forEach(transaction -> assertThat(transaction.getReceiver()).isNull());
	}

	@Test
	@DisplayName("Test feeSumForARegisteredBetweenDate should return 10.0")
	@Transactional
	void testFeeSumForARegisteredBetweenDateShouldReturnTen() {
		// GIVEN
		Calendar dateTransaction = GregorianCalendar.getInstance();
		dateTransaction.set(Calendar.DATE, 1); // set date at the begin of month
		dateTransaction.add(Calendar.MONTH, -3); // three months ago, add rule example : 01/01/2022 Calling add(Calendar.MONTH,-1) sets the calendar to 01/12/2021

		Calendar dateMonthLimit = (GregorianCalendar) dateTransaction.clone();
		dateMonthLimit.set(Calendar.HOUR_OF_DAY, 0);
		dateMonthLimit.set(Calendar.MINUTE, 0);
		dateMonthLimit.set(Calendar.SECOND, 0);
		dateMonthLimit.set(Calendar.MILLISECOND, 0);		
		LocalDateTime beginDate = LocalDateTime.ofInstant(dateMonthLimit.toInstant(), dateMonthLimit.getTimeZone().toZoneId()); //(dateMonthLimit);
		dateMonthLimit.add(Calendar.MONTH, 1);
		LocalDateTime endDate = LocalDateTime.ofInstant(dateMonthLimit.toInstant(), dateMonthLimit.getTimeZone().toZoneId());
		LocalDateTime dateTimeTransaction = null;
		for (int i = 1; i <= 4; i++) { // loops 4 times
			dateTimeTransaction = LocalDateTime.ofInstant(dateTransaction.toInstant(), dateTransaction.getTimeZone().toZoneId());
			
			Transaction transactionAtoB = new Transaction(dateTimeTransaction, 100 * i); // 100+200+300+400 = 1000 fee = 5 A
			transactionAtoB = transactionRepository.save(transactionAtoB);
			registeredA.addSendedTransaction(transactionAtoB);
			registeredB.addReceivedTransaction(transactionAtoB);
			transactionAtoB = transactionRepository.save(transactionAtoB);

			Transaction transactionAtoC = new Transaction(dateTimeTransaction, 100 * i); // 100+200+300+400 = 1000 fee = 5 A
			transactionAtoC = transactionRepository.save(transactionAtoC);
			registeredA.addSendedTransaction(transactionAtoC);
			registeredC.addReceivedTransaction(transactionAtoC);
			transactionAtoC = transactionRepository.save(transactionAtoC);

			Transaction transactionBtoC = new Transaction(dateTimeTransaction, 100 * i); // 100+200+300+400 = 1000 fee = 5 B
			transactionBtoC = transactionRepository.save(transactionBtoC);
			registeredB.addSendedTransaction(transactionBtoC);
			registeredC.addReceivedTransaction(transactionBtoC);
			transactionBtoC = transactionRepository.save(transactionBtoC);

			dateTransaction.roll(Calendar.DATE, 8); // 1, 9, 17, 25, (33) Roll rule : Larger fields (here MONTH) are unchanged after
													// the call.
		}
		dateTransaction.set(Calendar.DATE, 4);
		dateTransaction.add(Calendar.MONTH, 1);
		dateTimeTransaction = LocalDateTime.ofInstant(dateTransaction.toInstant(), dateTransaction.getTimeZone().toZoneId());
		Transaction transactionAtoB = new Transaction(dateTimeTransaction, 500);
		transactionAtoB = transactionRepository.save(transactionAtoB);
		registeredA.addSendedTransaction(transactionAtoB);
		registeredB.addReceivedTransaction(transactionAtoB);
		transactionAtoB = transactionRepository.save(transactionAtoB);

		Transaction transactionAtoC = new Transaction(dateTimeTransaction, 500);
		transactionAtoC = transactionRepository.save(transactionAtoC);
		registeredA.addSendedTransaction(transactionAtoC);
		registeredC.addReceivedTransaction(transactionAtoC);
		transactionAtoC = transactionRepository.save(transactionAtoC);

		Transaction transactionBtoC = new Transaction(dateTimeTransaction, 500);
		transactionBtoC = transactionRepository.save(transactionBtoC);
		registeredB.addSendedTransaction(transactionBtoC);
		registeredC.addReceivedTransaction(transactionBtoC);
		transactionBtoC = transactionRepository.save(transactionBtoC);

		// WHEN
		double result = transactionRepository.feeSumForARegisteredBetweenDate(beginDate, endDate, "aaa@aaa.com");
		double resultAll = transactionRepository.feeSumBetweenDate(beginDate, endDate);
		// THEN
		assertThat(result).isEqualTo(10.0);
		assertThat(resultAll).isEqualTo(15.0);
	}

	@Test
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
			LocalDateTime dateTimeTransaction = LocalDateTime.ofInstant(dateTransaction.toInstant(), dateTransaction.getTimeZone().toZoneId());
			Transaction transactionAtoB = new Transaction(dateTimeTransaction, 100 * i); // 100+200+300+400+500
			transactionAtoB = transactionRepository.save(transactionAtoB);
			registeredA.addSendedTransaction(transactionAtoB);
			registeredB.addReceivedTransaction(transactionAtoB);
			transactionAtoB = transactionRepository.save(transactionAtoB);
			transactionsBExpected.add(transactionAtoB);
			
			dateTransaction.add(Calendar.HOUR_OF_DAY, 1);
			dateTimeTransaction = LocalDateTime.ofInstant(dateTransaction.toInstant(), dateTransaction.getTimeZone().toZoneId());
			Transaction transactionBtoC = new Transaction(dateTimeTransaction, 100 * i);
			transactionBtoC = transactionRepository.save(transactionBtoC);
			registeredB.addSendedTransaction(transactionBtoC);
			registeredC.addReceivedTransaction(transactionBtoC);
			transactionBtoC = transactionRepository.save(transactionBtoC);
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
