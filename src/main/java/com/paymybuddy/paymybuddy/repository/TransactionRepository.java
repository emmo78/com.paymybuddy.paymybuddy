package com.paymybuddy.paymybuddy.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.paymybuddy.paymybuddy.model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	@Query(value = "SELECT SUM(t.fee) FROM transaction t WHERE (t.date_time between :beginDate AND :endDate) and t.email_sender = :email", nativeQuery = true)
	double feeSumForARegisteredBetweenDate(@Param("beginDate") LocalDateTime beginDate, @Param("endDate") LocalDateTime endDate, @Param("email") String email);

	@Query(value = "SELECT SUM(t.fee) FROM transaction t WHERE (t.date_time between :beginDate AND :endDate)", nativeQuery = true)
	double feeSumBetweenDate(@Param("beginDate") LocalDateTime beginDate, @Param("endDate") LocalDateTime endDate);
	
	@Query(value = "SELECT * FROM transaction t WHERE t.email_sender = :email OR t.email_receiver = :email",
			countQuery = "SELECT COUNT(*) FROM transaction t WHERE t.email_sender = :email OR t.email_receiver = :email",
			nativeQuery = true)
	Page<Transaction> findAllTransactionsByEmailSenderOrReceiver(@Param("email") String email, Pageable pageRequest);
}
