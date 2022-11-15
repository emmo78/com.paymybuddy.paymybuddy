package com.paymybuddy.paymybuddy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.paymybuddy.paymybuddy.dto.RegisteredDTO;
import com.paymybuddy.paymybuddy.exception.ResourceConflictException;
import com.paymybuddy.paymybuddy.model.Registered;
import com.paymybuddy.paymybuddy.repository.RegisteredRepository;

@SpringBootTest
public class RegisteredServiceIT {
	
	@Autowired
	private RegisteredService registeredService;

	@Autowired
	private RegisteredRepository registeredRepository;
	
	@AfterEach
	public void undefPerTest() {
		registeredRepository.deleteAll();
	}
	
	@Nested
	@Tag("createRegisteredIT")
	@DisplayName("IT for method createRegistered")
	class CreateRegisteredIT {
		
		@Test
		@Tag("RegisteredServiceIT")
		@DisplayName("IT createARegisterd should commit it and return RegisterdDTO with no Password")
		public void createRegisteredITShouldCommitItAndReturnRegisterdDTOWithNoPassword() {
			
			//GIVEN
			PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
			RegisteredDTO registeredDTO = new RegisteredDTO();
			registeredDTO.setEmail("Aaa@Aaa.com");
			registeredDTO.setPassword("aaaPasswd");
			registeredDTO.setFirstName("Aaa");
			registeredDTO.setLastName("AAA");
			registeredDTO.setBirthDate("02/29/1991"); //1991 is not a leap year : 02/28/1991 expected
			registeredDTO.setIban(null);
			registeredDTO.setBalance(null);
			
			MockHttpServletRequest requestMock = new MockHttpServletRequest();
			requestMock.setServerName("http://localhost:8080");
			requestMock.setRequestURI("/createTransaction");
			WebRequest request = new ServletWebRequest(requestMock);

			//WHEN
			RegisteredDTO registeredDTOResult = registeredService.createRegistered(registeredDTO, request);
			Optional<Registered> registeredResultOpt = registeredRepository.findById("aaa@aaa.com");
			
			//THEN
			assertThat(registeredResultOpt).isNotEmpty();
			assertThat(passwordEncoder.matches("aaaPasswd", registeredResultOpt.get().getPassword())).isTrue();
			assertThat(registeredDTOResult).extracting(
					RegisteredDTO::getEmail,
					RegisteredDTO::getPassword,
					RegisteredDTO::getFirstName,
					RegisteredDTO::getLastName,
					RegisteredDTO::getBirthDate,
					RegisteredDTO::getIban,
					RegisteredDTO::getBalance).containsExactly(
							"aaa@aaa.com",
							null,
							"Aaa",
							"AAA",
							"02/28/1991",
							null,
							"0.00");
		}
		
		@Test
		@Tag("RegisteredServiceIT")
		@DisplayName("IT createARegisterd with ResourceConflictException should Rollback")
		public void createRegisteredITWithResourceConflictExceptionShouldRollback() {
			
			//GIVEN
			PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
			RegisteredDTO registeredDTO = new RegisteredDTO();
			registeredDTO.setEmail("Aaa@Aaa.com");
			registeredDTO.setPassword("aaaPasswd");
			registeredDTO.setFirstName("Aaa");
			registeredDTO.setLastName("AAA");
			registeredDTO.setBirthDate("02/29/1991"); //1991 is not a leap year : 02/28/1991 expected
			registeredDTO.setIban(null);
			registeredDTO.setBalance(null);
			
			MockHttpServletRequest requestMock = new MockHttpServletRequest();
			requestMock.setServerName("http://localhost:8080");
			requestMock.setRequestURI("/createTransaction");
			WebRequest request = new ServletWebRequest(requestMock);
			
			registeredService.createRegistered(registeredDTO, request);
			
			RegisteredDTO registeredDTOConflict = new RegisteredDTO();
			registeredDTOConflict.setEmail("Aaa@Aaa.com");
			registeredDTOConflict.setPassword("bbbPasswd");
			registeredDTOConflict.setFirstName("bbb");
			registeredDTOConflict.setLastName("BBB");
			registeredDTOConflict.setBirthDate("02/02/1992"); //1991 is not a leap year : 02/28/1991 expected
			registeredDTOConflict.setIban(null);
			registeredDTOConflict.setBalance(null);
			
			//WHEN
			//THEN
			assertThat(assertThrows(ResourceConflictException.class,
					() -> registeredService.createRegistered(registeredDTO, request))
					.getMessage()).isEqualTo("User already exists");
			Optional<Registered> registeredResultOpt = registeredRepository.findById("aaa@aaa.com");
			assertThat(passwordEncoder.matches("bbbPasswd", registeredResultOpt.get().getPassword())).isFalse();
		}
	}
}
