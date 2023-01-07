package com.paymybuddy.paymybuddy.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.context.request.WebRequest;

import com.paymybuddy.paymybuddy.dto.RegisteredDTO;
import com.paymybuddy.paymybuddy.dto.RegisteredForListDTO;
import com.paymybuddy.paymybuddy.exception.NoIbanProvidedException;
import com.paymybuddy.paymybuddy.exception.ResourceConflictException;
import com.paymybuddy.paymybuddy.exception.WithdrawException;

/**
 * Service interface for Registered DAL
 * @author olivi
 *
 */
public interface RegisteredService {
	/**
	 * Return RegisterdDTO (without password) for a given email or throws an UnexpectedRollbackException
	 * @param email
	 * @param request : needed to log request
	 * @return
	 * @throws UnexpectedRollbackException
	 */
	RegisteredDTO getRegistered(String email, WebRequest request) throws UnexpectedRollbackException;
	
	/**
	 * persist a registered from given registeredDTO (with password). Can throw ResourceConflictException, UnexpectedRollbackException 
	 * @param registeredDTO
	 * @param request : needed to log request
	 * @return
	 * @throws ResourceConflictException
	 * @throws UnexpectedRollbackException
	 */
	RegisteredDTO createRegistered(RegisteredDTO registeredDTO, WebRequest request) throws ResourceConflictException, UnexpectedRollbackException;
	
	/**
	 * Update a registered from a DTO
	 * @param registeredDTO
	 * @param request
	 * @return
	 * @throws UnexpectedRollbackException
	 */
	RegisteredDTO updateRegistered(RegisteredDTO registeredDTO, WebRequest request) throws UnexpectedRollbackException;
	
	/**
	 * Delete a Registered : remove all connection by/to him/her, nullify email in transaction (done by SGBD), remove him/her and logout 
	 * @param email
	 * @param request
	 * @throws UnexpectedRollbackException
	 */
	void removeRegistered(String email, WebRequest request) throws UnexpectedRollbackException;
	
	/**
	 * Return all registrants (for admin)
	 * @param pageRequest
	 * @param request
	 * @return
	 * @throws UnexpectedRollbackException
	 */
	Page<RegisteredForListDTO> getRegistrants(Pageable pageRequest, WebRequest request) throws UnexpectedRollbackException; // for admin
	
	/**
	 * Return all the contacts that the email has added
	 * @param email
	 * @param pageRequest
	 * @param request
	 * @return
	 * @throws UnexpectedRollbackException
	 */
	Page<RegisteredForListDTO> getAllAddBy(String email, Pageable pageRequest, WebRequest request) throws UnexpectedRollbackException;
	
	/**
	 * Return all the contacts that the email has not added
	 * @param email
	 * @param pageRequest
	 * @param request
	 * @return
	 * @throws UnexpectedRollbackException
	 */
	Page<RegisteredForListDTO> getAllNotAddBy(String email, Pageable pageRequest, WebRequest request) throws UnexpectedRollbackException;
	
	/**
	 * Return all users who have added the email to their contact
	 * @param email
	 * @param pageRequest
	 * @param request
	 * @return
	 * @throws UnexpectedRollbackException
	 */
	Page<RegisteredForListDTO> getAllAddedTo(String email, Pageable pageRequest, WebRequest request) throws UnexpectedRollbackException;
	
	/**
	 * add emailToAdd to email contacts 
	 * @param email
	 * @param emailToAdd
	 * @param request
	 * @throws UnexpectedRollbackException
	 */
	void addConnection(String email, String emailToAdd, WebRequest request) throws UnexpectedRollbackException;
	
	/**
	 * remove emailToRemove from email contacts
	 * @param email
	 * @param emailToRemove
	 * @param request
	 * @throws UnexpectedRollbackException
	 */
	void removeConnection(String email, String emailToRemove, WebRequest request) throws UnexpectedRollbackException;
	
	/**
	 * To depose money from bank to balance
	 * @param email
	 * @param amount
	 * @param request
	 * @throws UnexpectedRollbackException
	 */
	void depositFromBank(String email, double amount, WebRequest request) throws NoIbanProvidedException, UnexpectedRollbackException;
	
	/**
	 * To withdraw money from balance to bank
	 * @param email
	 * @param amount
	 * @param request
	 * @throws NoIbanProvidedException
	 * @throws UnexpectedRollbackException
	 * @throws WithdrawException
	 */
	void withdrawToBank(String email, double amount, WebRequest request) throws NoIbanProvidedException, UnexpectedRollbackException, WithdrawException;
	
	/**
	 * need a smtp to send reset password by email
	 * @param email
	 */
	void resetRegisteredPassword(String email);
}
