package com.paymybuddy.paymybuddy.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import javax.transaction.Transactional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.paymybuddy.paymybuddy.model.Registered;
import com.paymybuddy.paymybuddy.model.Transaction;

@SpringBootTest
class TransactionRepositoryTest {

	@Autowired
	TransactionRepository transactionRepository;
	
	//Needed for testing
	@Autowired
	RegisteredRepository registeredRepository;

	Registered registeredA;
	Registered registeredB;
	Registered registeredC;

	@BeforeEach
	public void setUpPerTest() {
		registeredA = new Registered("aaa@aaa.com", "aaaPasswd", "Aaa", "AAA", Date.valueOf(LocalDate.parse("01/01/1991", DateTimeFormatter.ofPattern("dd/MM/yyyy"))), "aaaIban");
		registeredB = new Registered("bbb@bbb.com", "bbbPasswd", "Bbb", "BBB", Date.valueOf(LocalDate.parse("02/02/1992", DateTimeFormatter.ofPattern("dd/MM/yyyy"))), "bbbIban");
		registeredC = new Registered("ccc@ccc.com", "cccPasswd", "Ccc", "CCC", Date.valueOf(LocalDate.parse("03/03/1993", DateTimeFormatter.ofPattern("dd/MM/yyyy"))), "cccIban");
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
		//GIVEN
		Transaction transactionAtoB = new Transaction(Timestamp.valueOf(LocalDateTime.now()), 100);
		transactionAtoB = transactionRepository.save(transactionAtoB);
		registeredA.addSendedTransaction(transactionAtoB);
		registeredB.addReceivedTransaction(transactionAtoB);
		
		Transaction transactionAtoC = new Transaction(Timestamp.valueOf(LocalDateTime.now()), 200);
		transactionAtoC = transactionRepository.save(transactionAtoC);
		registeredA.addSendedTransaction(transactionAtoC);
		registeredC.addReceivedTransaction(transactionAtoC);
		
		List<Transaction> sendedTransactionsAExpected = Arrays.asList(transactionAtoB, transactionAtoC); 
		
		//WHEN
		transactionRepository.save(transactionAtoB);
		transactionRepository.save(transactionAtoC);
		
		//THEN
		assertThat(transactionRepository.count()).isEqualTo(2L);
		
		registeredA = registeredRepository.findById("aaa@aaa.com").get();
		registeredB = registeredRepository.findById("bbb@bbb.com").get();
		registeredC = registeredRepository.findById("ccc@ccc.com").get();
		
		assertThat(registeredA.getSendedTransactions()).containsExactlyInAnyOrderElementsOf(sendedTransactionsAExpected);
		assertThat(registeredB.getReceivedTransactions()).containsExactlyInAnyOrder(transactionAtoB);
		assertThat(registeredC.getReceivedTransactions()).containsExactlyInAnyOrder(transactionAtoC);
	}
	
	@Test
	@DisplayName("After sended to B and C A remove from application, FK for A should be null")
	//@Transactional - object references an unsaved transient instance - save the transient instance before flushing
	void afterSendedToBandCAremoveFromApplicationSoHisFKShouldBeNull() {
		//GIVEN
		Transaction transactionAtoB = new Transaction(Timestamp.valueOf(LocalDateTime.now()), 100);
		transactionAtoB = transactionRepository.save(transactionAtoB);
		registeredA.addSendedTransaction(transactionAtoB);
		registeredB.addReceivedTransaction(transactionAtoB);
		
		Transaction transactionAtoC = new Transaction(Timestamp.valueOf(LocalDateTime.now()), 200);
		transactionAtoC = transactionRepository.save(transactionAtoC);
		registeredA.addSendedTransaction(transactionAtoC);
		registeredC.addReceivedTransaction(transactionAtoC);

		transactionRepository.save(transactionAtoB);
		transactionRepository.save(transactionAtoC);
		
		//WHEN
		registeredRepository.deleteById("aaa@aaa.com");

		
		//THEN
		assertThat(transactionRepository.count()).isEqualTo(2L);
		transactionRepository.findAll().forEach(transaction -> assertThat(transaction.getSender()).isNull());
	}
	
	@Test
	@DisplayName("Test getFeeSum for a given month and a given registered sender should return")
	void testGetFeeSum() {
		//GIVEN
		Calendar dateTransaction = GregorianCalendar.getInstance();
		dateTransaction.set(Calendar.DATE, 1); //set date at the begin of month
		dateTransaction.add(Calendar.MONTH, -3); //three months ago, add rule example : 01/01/2022 Calling add(Calendar.MONTH, -1) sets the calendar to 01/12/2021
		Timestamp beginDate = new Timestamp(dateTransaction.getTimeInMillis());
        Calendar dateMonthEnd = (GregorianCalendar)dateTransaction.clone();
        dateMonthEnd.set(Calendar.DATE, dateMonthEnd.getActualMaximum(Calendar.DATE));//Go to the last day of Month 
		Timestamp endDate = new Timestamp(dateMonthEnd.getTimeInMillis());
		for(int i=1; i<=4; i++) { //loops 4 times
			Transaction transactionAtoB = new Transaction(new Timestamp(dateTransaction.getTimeInMillis()), 100*i); //100+200+300+400
			transactionAtoB = transactionRepository.save(transactionAtoB);
			registeredA.addSendedTransaction(transactionAtoB);
			registeredB.addReceivedTransaction(transactionAtoB);
			transactionAtoB = transactionRepository.save(transactionAtoB);
			
			Transaction transactionAtoC = new Transaction(new Timestamp(dateTransaction.getTimeInMillis()), 100*i); //SUM = 1000
			transactionAtoC = transactionRepository.save(transactionAtoC);
			registeredA.addSendedTransaction(transactionAtoC);
			registeredC.addReceivedTransaction(transactionAtoC);
			transactionAtoC = transactionRepository.save(transactionAtoC);

			Transaction transactionBtoC = new Transaction(new Timestamp(dateTransaction.getTimeInMillis()), 100*i); //Expected fee sum = 5*2
			transactionBtoC = transactionRepository.save(transactionBtoC);
			registeredB.addSendedTransaction(transactionBtoC);
			registeredC.addReceivedTransaction(transactionBtoC);
			transactionBtoC = transactionRepository.save(transactionBtoC);

			dateTransaction.roll(Calendar.DATE, 8); // 1, 9, 17, 25, (33) Roll rule : Larger fields (here MONTH) are unchanged after the call.
		}
		dateTransaction.set(Calendar.DATE, 4);
		dateTransaction.add(Calendar.MONTH, 1);
		Transaction transactionAtoB = new Transaction(new Timestamp(dateTransaction.getTimeInMillis()), 500);
		transactionAtoB = transactionRepository.save(transactionAtoB);
		registeredA.addSendedTransaction(transactionAtoB);
		registeredB.addReceivedTransaction(transactionAtoB);
		transactionAtoB = transactionRepository.save(transactionAtoB);
		
		Transaction transactionAtoC = new Transaction(new Timestamp(dateTransaction.getTimeInMillis()), 500);
		transactionAtoC = transactionRepository.save(transactionAtoC);
		registeredA.addSendedTransaction(transactionAtoC);
		registeredC.addReceivedTransaction(transactionAtoC);
		transactionAtoC = transactionRepository.save(transactionAtoC);

		Transaction transactionBtoC = new Transaction(new Timestamp(dateTransaction.getTimeInMillis()), 500);
		transactionBtoC = transactionRepository.save(transactionBtoC);
		registeredB.addSendedTransaction(transactionBtoC);
		registeredC.addReceivedTransaction(transactionBtoC);
		transactionBtoC = transactionRepository.save(transactionBtoC);
		
		//WHEN
		long result = transactionRepository.feeSumForARegistered(beginDate, endDate, "aaa@aaa.com");
		
		//THEN
		assertThat(result).isEqualTo(10);
	}
	

}
