package com.paymybuddy.paymybuddy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.paymybuddy.paymybuddy.dto.RegisteredDTO;
import com.paymybuddy.paymybuddy.dto.TransactionDTO;
import com.paymybuddy.paymybuddy.exception.ResourceConflictException;
import com.paymybuddy.paymybuddy.model.Registered;
import com.paymybuddy.paymybuddy.model.Transaction;
import com.paymybuddy.paymybuddy.repository.RegisteredRepository;

@SpringBootTest
public class RegisteredServiceIT {
	
	@Autowired
	private RegisteredService registeredService;

	@Autowired
	private RegisteredRepository registeredRepository;
	
	private static MockHttpServletRequest requestMock;
	private static WebRequest request;
	
	@Nested
	@Tag("createRegisteredIT")
	@DisplayName("IT for method createRegistered")
	@TestInstance(Lifecycle.PER_CLASS)
	class CreateRegisteredIT {
		
		private PasswordEncoder passwordEncoder;
		private RegisteredDTO registeredDTO;
		
		@BeforeAll
		public void setUpForAllTests() {
			passwordEncoder = new BCryptPasswordEncoder();
			requestMock = new MockHttpServletRequest();
			requestMock.setServerName("http://localhost:8080");
			requestMock.setRequestURI("/createregistered");
			request = new ServletWebRequest(requestMock);
		}			

		@AfterAll
		public void unSetForAllTests() {
			passwordEncoder = null;
			requestMock=null;
			request=null;
		}
		
		@BeforeEach
		public void setUpForEachTests() {
			registeredDTO = new RegisteredDTO();
			registeredDTO.setEmail("Aaa@Aaa.com");
			registeredDTO.setPassword("aaaPasswd");
			registeredDTO.setFirstName("Aaa");
			registeredDTO.setLastName("AAA");
			registeredDTO.setBirthDate("02/29/1991"); //1991 is not a leap year : 02/28/1991 expected
			registeredDTO.setIban(null);
			registeredDTO.setBalance(null);
		}
		
		@AfterEach
		public void unSetForEachTests() {
			registeredRepository.deleteAll();
			registeredDTO = null;
		}
		
		@Test
		@Tag("RegisteredServiceIT")
		@DisplayName("IT createARegisterd should commit it and return RegisterdDTO with no Password")
		public void createRegisteredITShouldCommitItAndReturnRegisterdDTOWithNoPassword() {
			
			//GIVEN

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
			registeredService.createRegistered(registeredDTO, request);
			
			RegisteredDTO registeredDTOConflict = new RegisteredDTO();
			registeredDTOConflict.setEmail("Aaa@Aaa.com");
			registeredDTOConflict.setPassword("bbbPasswd");
			registeredDTOConflict.setFirstName("bbb");
			registeredDTOConflict.setLastName("BBB");
			registeredDTOConflict.setBirthDate("02/02/1992");
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
