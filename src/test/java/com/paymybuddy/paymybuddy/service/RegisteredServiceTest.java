package com.paymybuddy.paymybuddy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.paymybuddy.paymybuddy.configuration.DateTimePatternProperties;
import com.paymybuddy.paymybuddy.dto.RegisteredDTO;
import com.paymybuddy.paymybuddy.dto.service.RegisteredDTOService;
import com.paymybuddy.paymybuddy.exception.ResourceConflictException;
import com.paymybuddy.paymybuddy.model.Registered;
import com.paymybuddy.paymybuddy.repository.RegisteredRepository;

@ExtendWith(MockitoExtension.class)
public class RegisteredServiceTest {
	
	@InjectMocks
	private RegisteredServiceImpl registeredService;
	
	@Mock
	private RegisteredDTOService registeredDTOService;
	
	@Mock
	private RegisteredRepository registeredRepository;
	
	@Spy
	private RequestService requestService = new RequestServiceImpl();
	
	@Mock
	private DateTimePatternProperties dateStringPattern;
	
	private MockHttpServletRequest requestMock;
	private WebRequest request;
	
	@Nested
	@Tag("createRegisteredTests")
	@DisplayName("Tests for method createRegistered")
	@TestInstance(Lifecycle.PER_CLASS)
	class CreateRegisteredTests {
		
		private RegisteredDTO registeredDTO;
		private Registered registered;
		
		@BeforeAll
		public void setUpForAllCreateATransactionTests() {
			requestMock = new MockHttpServletRequest();
			requestMock.setServerName("http://localhost:8080");
			requestMock.setRequestURI("/createRegistered");
			request = new ServletWebRequest(requestMock);
		}

		@AfterAll
		public void unSetForAllCreateATransactionTests() {
			requestMock = null;
			request = null;
		}
		
		@BeforeEach
		public void setUpForEachTests() {
			registeredDTO = new RegisteredDTO();
			registeredDTO.setEmail("Aaa@Aaa.com");
			registered = new Registered("aaa@aaa.com", "aaaPasswd", "Aaa", "AAA", LocalDate.parse("01/21/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")), "aaaIban");
		}
		
		@AfterEach
		public void unSetForEachTests() {
			registeredDTO = null;
			registered = null;
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test createRegistered should throw ResourceConflictException")
		public void createRegisteredTestShouldThrowResourceConflictException() {
			//GIVEN
			when(registeredDTOService.registeredFromDTO(any(RegisteredDTO.class))).thenReturn(registered);
			when(registeredRepository.existsById(any(String.class))).thenReturn(true);
			
			//WHEN
			//THEN
			assertThat(assertThrows(ResourceConflictException.class,
					() -> registeredService.createRegistered(registeredDTO, request))
					.getMessage()).isEqualTo("User already exists");
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test createRegistered should throw UnexpectedRollbackException on IllegalArgumentException")
		public void createRegisteredTestShouldThrowUnexpectedRollbackExceptionOnIllegalArgumentException() {
			//GIVEN
			when(registeredDTOService.registeredFromDTO(any(RegisteredDTO.class))).thenReturn(registered);
			when(registeredRepository.existsById(any(String.class))).thenReturn(false);
			when(registeredRepository.save(any(Registered.class))).thenThrow(new IllegalArgumentException());
			
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
				() -> registeredService.createRegistered(registeredDTO, request))
				.getMessage()).isEqualTo("Error while creating your profile");
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test createRegistered should throw UnexpectedRollbackException on OptimisticLockingFailureException")
		public void createRegisteredTestShouldThrowUnexpectedRollbackExceptionOnOptimisticLockingFailureException() {
			//GIVEN
			when(registeredDTOService.registeredFromDTO(any(RegisteredDTO.class))).thenReturn(registered);
			when(registeredRepository.existsById(any(String.class))).thenReturn(false);
			when(registeredRepository.save(any(Registered.class))).thenThrow(new OptimisticLockingFailureException(""));
			
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
				() -> registeredService.createRegistered(registeredDTO, request))
				.getMessage()).isEqualTo("Error while creating your profile");
		}
	}
}
