package com.paymybuddy.paymybuddy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.paymybuddy.paymybuddy.configuration.DateTimePatternProperties;
import com.paymybuddy.paymybuddy.dto.RegisteredDTO;
import com.paymybuddy.paymybuddy.dto.service.RegisteredDTOService;
import com.paymybuddy.paymybuddy.exception.ResourceConflictException;
import com.paymybuddy.paymybuddy.exception.ResourceNotFoundException;
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
			when(registeredRepository.existsById(anyString())).thenReturn(true);
			
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
			when(registeredRepository.existsById(anyString())).thenReturn(false);
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
			when(registeredRepository.existsById(anyString())).thenReturn(false);
			when(registeredRepository.save(any(Registered.class))).thenThrow(new OptimisticLockingFailureException(""));
			
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
				() -> registeredService.createRegistered(registeredDTO, request))
				.getMessage()).isEqualTo("Error while creating your profile");
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test createRegistered should throw UnexpectedRollbackException on any RuntimeException")
		public void createRegisteredTestShouldThrowUnexpectedRollbackExceptionOnAnyRuntimeException() {
			//GIVEN
			when(registeredDTOService.registeredFromDTO(any(RegisteredDTO.class))).thenReturn(registered);
			when(registeredRepository.existsById(anyString())).thenReturn(false);
			when(registeredRepository.save(any(Registered.class))).thenThrow(new RuntimeException(""));
			
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
				() -> registeredService.createRegistered(registeredDTO, request))
				.getMessage()).isEqualTo("Error while creating your profile");
		}

	}
	
	@Nested
	@Tag("getRegisteredTests")
	@DisplayName("Tests for method getRegistered")
	@TestInstance(Lifecycle.PER_CLASS)
	class GetRegisteredTests {
		
		@BeforeAll
		public void setUpForAllCreateATransactionTests() {
			requestMock = new MockHttpServletRequest();
			requestMock.setServerName("http://localhost:8080");
			requestMock.setRequestURI("/getRegistered");
			request = new ServletWebRequest(requestMock);
		}

		@AfterAll
		public void unSetForAllCreateATransactionTests() {
			requestMock = null;
			request = null;
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test getRegistered should throw ResourceNotFoundException")
		public void getRegisteredTestShouldThrowsResourceNotFoundException() {
			//GIVEN
			when(registeredRepository.findById(anyString())).thenReturn(Optional.ofNullable(null));
			
			//WHEN
			//THEN
			assertThat(assertThrows(ResourceNotFoundException.class,
				() -> registeredService.getRegistered("aaa@aaa.com", request))
				.getMessage()).isEqualTo("Your email is not found");
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test getRegistered should throw UnexpectedRollbackException on IllegalArgumentException")
		public void getRegisteredTestShouldThrowsUnexpectedRollbackExceptionOnIllegalArgumentException() {
			//GIVEN
			when(registeredRepository.findById(anyString())).thenThrow(new IllegalArgumentException());
			
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
				() -> registeredService.getRegistered("aaa@aaa.com", request))
				.getMessage()).isEqualTo("Error while getting your profile");
		}

		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test getRegistered should throw UnexpectedRollbackException on any RuntimeException")
		public void getRegisteredTestShouldThrowsUnexpectedRollbackExceptionOnAnyRuntimeException() {
			//GIVEN
			when(registeredRepository.findById(anyString())).thenThrow(new RuntimeException());
			
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
				() -> registeredService.getRegistered("aaa@aaa.com", request))
				.getMessage()).isEqualTo("Error while getting your profile");
		}
	}
	
	@Nested
	@Tag("UpdateRegisteredTests")
	@DisplayName("Tests for method updateRegistered")
	@TestInstance(Lifecycle.PER_CLASS)
	class UpdateRegisteredTests {
		
		@BeforeAll
		public void setUpForAllCreateATransactionTests() {
			requestMock = new MockHttpServletRequest();
			requestMock.setServerName("http://localhost:8080");
			requestMock.setRequestURI("/getRegistered");
			request = new ServletWebRequest(requestMock);
		}

		@AfterAll
		public void unSetForAllCreateATransactionTests() {
			requestMock = null;
			request = null;
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test UpdateRegistered should update not null and equal")
		public void updateRegisteredTestShouldUpdateNotNullAndEqual() {
			//GIVEN
			PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
			
			RegisteredDTO registeredDTO = new RegisteredDTO();
			registeredDTO.setEmail("Aaa@Aaa.com"); //NOT Updated
			registeredDTO.setPassword(null); //NOT Updated
			registeredDTO.setFirstName("Aaa"); //Equal -> expect not Updated
			registeredDTO.setLastName(null); //Null -> expect not updated
			registeredDTO.setBirthDate("02/02/1992"); //expect updated
			registeredDTO.setIban("FR7601234567890123456789"); //expect updated
			registeredDTO.setBalance(null); //NOT Updated
			
			Registered registered = new Registered(
					"aaa@aaa.com",
					null,
					"Aaa",
					null,
					LocalDate.parse("02/02/1992", DateTimeFormatter.ofPattern("MM/dd/yyyy")),
					"FR7601234567890123456789");
			registered.setBalance(0);
			
			Registered registeredToUpate =  new Registered(
					"aaa@aaa.com",
					passwordEncoder.encode("aaaPasswd"),
					"Aaa",
					"AAA",
					LocalDate.parse("01/01/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")),
					null);
			registeredToUpate.setBalance(100);
			
			when(registeredDTOService.registeredFromDTO(any(RegisteredDTO.class))).thenReturn(registered);
			when(registeredRepository.findById(anyString())).thenReturn(Optional.of(registeredToUpate));
			ArgumentCaptor<Registered> registeredResultCapt = ArgumentCaptor.forClass(Registered.class);
			when(registeredRepository.save(any(Registered.class))).thenReturn(registered);
			when(registeredDTOService.registeredToDTO(any(Registered.class))).thenReturn(registeredDTO);
			
			//WHEN
			registeredService.updateRegistered(registeredDTO, request);
			
			//THEN
			verify(registeredRepository, times(1)).save(registeredResultCapt.capture());
			assertThat(registeredResultCapt.getValue()).extracting(
					Registered::getEmail,
					Registered::getFirstName,
					Registered::getLastName,
					Registered::getBirthDate,
					Registered::getIban,
					Registered::getBalance).containsExactly(
							"aaa@aaa.com",
							"Aaa",
							"AAA",
							LocalDate.parse("02/02/1992", DateTimeFormatter.ofPattern("MM/dd/yyyy")),
							"FR7601234567890123456789",
							100d);
			assertThat(passwordEncoder.matches("aaaPasswd", registeredResultCapt.getValue().getPassword())).isTrue();
		}
	}
}
