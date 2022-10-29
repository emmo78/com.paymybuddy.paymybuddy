package com.paymybuddy.paymybuddy.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.paymybuddy.paymybuddy.model.Registered;

@Repository
public interface RegisteredRepository extends JpaRepository<Registered, String> {

	@Query(value = "SELECT * FROM registered r WHERE r.email IN (SELECT c.email_add FROM registered r INNER JOIN connection c ON r.email = c.email_added AND r.email = :email)",
			countQuery = "SELECT COUNT(*) FROM registered r WHERE r.email IN (SELECT c.email_add FROM registered r INNER JOIN connection c ON r.email = c.email_added AND r.email = :email)",
			nativeQuery = true)
	Page<Registered> findAllConnectedToEmail(@Param("email") String email, Pageable pageRequest);

	@Query(value = "SELECT * FROM registered r WHERE (NOT r.email = :email) AND (r.email NOT IN (SELECT c.email_add FROM registered r INNER JOIN connection c ON r.email = c.email_added AND r.email = :email))",
			countQuery = "SELECT COUNT(*) FROM registered r WHERE (NOT r.email = :email) AND (r.email NOT IN (SELECT c.email_add FROM registered r INNER JOIN connection c ON r.email = c.email_added AND r.email = :email))",
			nativeQuery = true)
	Page<Registered> findAllNotConnectedToEmail(@Param("email") String email, Pageable pageRequest);
}
