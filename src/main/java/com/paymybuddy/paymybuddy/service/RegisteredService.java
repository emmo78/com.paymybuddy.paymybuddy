package com.paymybuddy.paymybuddy.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.context.request.WebRequest;

import com.paymybuddy.paymybuddy.dto.RegisteredDTO;
import com.paymybuddy.paymybuddy.dto.RegisteredForListDTO;
import com.paymybuddy.paymybuddy.exception.ResourceConflictException;
import com.paymybuddy.paymybuddy.exception.ResourceNotFoundException;
import com.paymybuddy.paymybuddy.exception.WithdrawException;
import com.paymybuddy.paymybuddy.model.Registered;

public interface RegisteredService {
	Registered getRegistered(String email, WebRequest request) throws ResourceNotFoundException ,UnexpectedRollbackException;
	RegisteredDTO createRegistered(RegisteredDTO registeredDTO, WebRequest request) throws ResourceConflictException, UnexpectedRollbackException;
	RegisteredDTO updateRegistered(RegisteredDTO registeredDTO, WebRequest request) throws UnexpectedRollbackException;
	void removeRegistered(String email, WebRequest request) throws UnexpectedRollbackException;
	Page<RegisteredForListDTO> getRegistrants(Pageable pageRequest, WebRequest request) throws UnexpectedRollbackException; // for admin
	Page<RegisteredForListDTO> getAllAddBy(String email, Pageable pageRequest, WebRequest request) throws UnexpectedRollbackException;
	Page<RegisteredForListDTO> getAllNotAddBy(String email, Pageable pageRequest, WebRequest request) throws UnexpectedRollbackException;
	Page<RegisteredForListDTO> getAllAddedTo(String email, Pageable pageRequest, WebRequest request) throws UnexpectedRollbackException;
	void addConnection(String email, String emailToAdd, WebRequest request) throws UnexpectedRollbackException;
	void removeConnection(String email, String emailToRemove, WebRequest request) throws UnexpectedRollbackException;
	void depositFromBank(String email, double amount, WebRequest request) throws UnexpectedRollbackException;
	void withdrawToBank(String email, double amount, WebRequest request) throws UnexpectedRollbackException, WithdrawException;
	void resetRegisteredPassword(String email);
}
