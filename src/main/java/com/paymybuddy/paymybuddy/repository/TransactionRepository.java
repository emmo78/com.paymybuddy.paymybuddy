package com.paymybuddy.paymybuddy.repository;

import java.sql.Timestamp;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.paymybuddy.paymybuddy.model.Registered;
import com.paymybuddy.paymybuddy.model.Transaction;

public interface TransactionRepository extends CrudRepository<Transaction, Integer> {

	@Query(value = "SELECT SUM(t.fee) FROM transaction t WHERE (t.date_time between ?1 AND ?2) and t.email_sender = ?3", nativeQuery = true)
	long feeSumForARegistered(Timestamp beginDate, Timestamp endDate, String emailA);

}
