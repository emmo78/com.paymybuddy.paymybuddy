package com.paymybuddy.paymybuddy.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Date;
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
		registeredA = new Registered("aaa@aaa.com", "aaaPasswd", "Aaa", "AAA", new Date(), "aaaIban");
		registeredB = new Registered("bbb@bbb.com", "bbbPasswd", "Bbb", "BBB", new Date(), "bbbIban");
		registeredC = new Registered("ccc@ccc.com", "cccPasswd", "Ccc", "CCC", new Date(), "cccIban");
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
		Transaction transactionAtoB = new Transaction(new Date(), 100);
		transactionAtoB = transactionRepository.save(transactionAtoB);
		registeredA.addSendedTransaction(transactionAtoB);
		registeredB.addReceivedTransaction(transactionAtoB);
		
		Transaction transactionAtoC = new Transaction(new Date(), 200);
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
	@DisplayName(" After sended to B and C A remove from application, FK for A should be null")
	//@Transactional - object references an unsaved transient instance - save the transient instance before flushing
	void afterSendedToBandCAremoveFromApplicationSoHisFKShouldBeNull() {
		//GIVEN
		Transaction transactionAtoB = new Transaction(new Date(), 100);
		transactionAtoB = transactionRepository.save(transactionAtoB);
		registeredA.addSendedTransaction(transactionAtoB);
		registeredB.addReceivedTransaction(transactionAtoB);
		
		Transaction transactionAtoC = new Transaction(new Date(), 200);
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

}
