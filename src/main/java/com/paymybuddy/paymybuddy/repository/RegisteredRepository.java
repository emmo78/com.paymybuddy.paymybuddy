package com.paymybuddy.paymybuddy.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.paymybuddy.paymybuddy.model.Registered;

/**
 * Repository for Registered
 * CRUD because extends JpaRepository
 * @author Olivier MOREL
 *
 */
@Repository
public interface RegisteredRepository extends JpaRepository<Registered, String> {

	/**
	 * Return the contacts added by given email
	 * @param email : person who added the contacts
	 * @param pageRequest : the page format for result
	 * @return result page
	 */
	@Query(value = "SELECT * FROM registered r WHERE r.email IN "
				+ "(SELECT c.email_add FROM registered r INNER JOIN connection c ON r.email = c.email_added AND r.email = :email)",
			countQuery = "SELECT COUNT(*) FROM registered r WHERE r.email IN "
				+ "(SELECT c.email_add FROM registered r INNER JOIN connection c ON r.email = c.email_added AND r.email = :email)",
			nativeQuery = true)
	Page<Registered> findAllAddByEmail(@Param("email") String email, Pageable pageRequest);

	/**
	 * Return free people ready to be added to contacts by given email 
	 * @param email : person who does not have these contacts
	 * @param pageRequest : the page format for result
	 * @return result page
	 */
	@Query(value = "SELECT * FROM registered r WHERE (NOT r.email = :email) AND "
				+ "(r.email NOT IN (SELECT c.email_add FROM registered r INNER JOIN connection c ON r.email = c.email_added AND r.email = :email))",
			countQuery = "SELECT COUNT(*) FROM registered r WHERE (NOT r.email = :email) AND "
				+ "(r.email NOT IN (SELECT c.email_add FROM registered r INNER JOIN connection c ON r.email = c.email_added AND r.email = :email))",
			nativeQuery = true)
	Page<Registered> findAllNotAddByEmail(@Param("email") String email, Pageable pageRequest);
	
	/**
	 * Return the people who have added the given email to their contact
	 * @param email : the person added in contact
	 * @param pageRequest
	 * @return result page
	 */
	@Query(value = "SELECT * FROM registered r WHERE r.email IN "
				+ "(SELECT c.email_added FROM registered r INNER JOIN connection c ON r.email = c.email_add AND r.email = :email)",
			countQuery = "SELECT COUNT(*) FROM registered r WHERE r.email IN "
				+ "(SELECT c.email_added FROM registered r INNER JOIN connection c ON r.email = c.email_add AND r.email = :email)",
			nativeQuery = true)
	Page<Registered> findAllAddedToEmail(@Param("email") String email, Pageable pageRequest);
}
