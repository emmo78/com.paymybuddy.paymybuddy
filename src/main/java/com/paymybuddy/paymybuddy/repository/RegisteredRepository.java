package com.paymybuddy.paymybuddy.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paymybuddy.paymybuddy.model.Registered;

public interface RegisteredRepository extends JpaRepository<Registered, String> {

}
