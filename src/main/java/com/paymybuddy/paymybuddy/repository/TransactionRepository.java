package com.paymybuddy.paymybuddy.repository;

import java.sql.Timestamp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.paymybuddy.paymybuddy.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

	@Query(value = "SELECT SUM(t.fee) FROM transaction t WHERE (t.date_time between :beginDate AND :endDate) and t.email_sender = :email", nativeQuery = true)
	long feeSumForARegisteredBetweenDate(@Param("beginDate")Timestamp beginDate, @Param("endDate")Timestamp endDate, @Param("email")String email);
}
