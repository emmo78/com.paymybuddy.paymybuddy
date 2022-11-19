package com.paymybuddy.paymybuddy.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.context.request.WebRequest;

import com.paymybuddy.paymybuddy.dto.RegisteredDTO;
import com.paymybuddy.paymybuddy.dto.RegisteredForListDTO;
import com.paymybuddy.paymybuddy.exception.ResourceConflictException;
import com.paymybuddy.paymybuddy.exception.ResourceNotFoundException;
import com.paymybuddy.paymybuddy.model.Registered;

public interface RegisteredService {
	Registered getRegistered(String email, WebRequest request) throws ResourceNotFoundException ,UnexpectedRollbackException;
	RegisteredDTO createRegistered(RegisteredDTO registeredDTO, WebRequest request) throws ResourceConflictException, UnexpectedRollbackException;
	RegisteredDTO updateRegistered(RegisteredDTO registeredDTO, WebRequest request) throws UnexpectedRollbackException;
	void removeRegistered(String email, WebRequest request) throws UnexpectedRollbackException;
	Page<RegisteredForListDTO> getRegistrants(Pageable pageRequest, WebRequest request); // for admin
	Page<RegisteredForListDTO> getAllConnectedToARegistered(String email, Pageable pageRequest);
	Page<RegisteredForListDTO> getAllNotConnectedToARegistered(String email, Pageable pageRequest);
	Optional<String> getRegisteredIban(String email);
	String saveUpdateRegisteredIban(String email, String iban);
	void resetRegisteredPassword(String email);
}
