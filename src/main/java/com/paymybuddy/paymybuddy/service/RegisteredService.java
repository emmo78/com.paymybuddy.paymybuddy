package com.paymybuddy.paymybuddy.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.paymybuddy.paymybuddy.dto.RegisteredDTO;
import com.paymybuddy.paymybuddy.dto.RegisteredForListDTO;
import com.paymybuddy.paymybuddy.model.Registered;

public interface RegisteredService {
	Optional<Registered> getRegistered(String email);
	RegisteredDTO createRegistered(RegisteredDTO registeredDTO);
	RegisteredDTO updateRegistered(RegisteredDTO registeredDTO);
	void removeRegistered(String email);
	Page<RegisteredForListDTO> getAllRegistered(Pageable pageRequest); // for admin
	Page<RegisteredForListDTO> getAllConnectedToARegistered(Pageable pageRequest);
	Page<RegisteredForListDTO> getAllNotConnectedToARegistered(Pageable pageRequest);
	Optional<String> getRegisteredIban(String email);
	String saveUpdateRegisteredIban(String email, String iban);
	void resetRegisteredPassword(String email);
}
