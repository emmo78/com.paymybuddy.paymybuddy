package com.paymybuddy.paymybuddy.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.paymybuddy.paymybuddy.repository.RegisteredRepository;

@ExtendWith(MockitoExtension.class)
public class RegistredUserDetailsServiceTest {

	@InjectMocks
	RegistredUserDetailsService registredUserDetailsService;
	
	@Mock
	private RegisteredRepository registeredRepository;
	
	
}
