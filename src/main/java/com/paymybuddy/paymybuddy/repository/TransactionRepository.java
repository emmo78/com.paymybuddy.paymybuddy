package com.paymybuddy.paymybuddy.repository;

import org.springframework.data.repository.CrudRepository;

import com.paymybuddy.paymybuddy.model.Transaction;

public interface TransactionRepository extends CrudRepository<Transaction, Integer> {

}
