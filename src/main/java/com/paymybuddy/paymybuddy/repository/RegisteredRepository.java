package com.paymybuddy.paymybuddy.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.paymybuddy.paymybuddy.model.Registered;

public interface RegisteredRepository extends JpaRepository<Registered, String> {

	@Query(value = "SELECT * FROM registered r INNER JOIN connection c ON NOT r.email = c.email_added WHERE NOT r.email = :email",
			countQuery = "SELECT COUNT(*) FROM registered r INNER JOIN connection c ON NOT r.email = c.email_added WHERE NOT r.email = :email", 
			nativeQuery = true)
	Page<Registered> findAllNotConnectedToId(@Param("email") String email, Pageable pageRequest);

}
