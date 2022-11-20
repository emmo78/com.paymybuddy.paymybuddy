package com.paymybuddy.paymybuddy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.paymybuddy.paymybuddy.configuration.DateTimePatternProperties;
import com.paymybuddy.paymybuddy.dto.RegisteredDTO;
import com.paymybuddy.paymybuddy.dto.RegisteredForListDTO;
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
		public void setUpForAllTests() {
			requestMock = new MockHttpServletRequest();
			requestMock.setServerName("http://localhost:8080");
			requestMock.setRequestURI("/createRegistered");
			request = new ServletWebRequest(requestMock);
		}

		@AfterAll
		public void unSetForAllTests() {
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
		public void setUpForAllTests() {
			requestMock = new MockHttpServletRequest();
			requestMock.setServerName("http://localhost:8080");
			requestMock.setRequestURI("/getRegistered?email=aaa@aaa.com");
			request = new ServletWebRequest(requestMock);
		}

		@AfterAll
		public void unSetForAllTests() {
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
		public void setUpForAllTests() {
			requestMock = new MockHttpServletRequest();
			requestMock.setServerName("http://localhost:8080");
			requestMock.setRequestURI("/updateRegistered");
			request = new ServletWebRequest(requestMock);
		}

		@AfterAll
		public void unSetForAllTests() {
			requestMock = null;
			request = null;
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test opdateRegistered should update not null and equal")
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
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test updateRegistered should throw UnexpectedRollbackException on ResourceNotFoundException")
		public void updateRegisteredTestShouldThrowsUnexpectedRollbackExceptionOnResourceNotFoundException() {
			//GIVEN
		
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
			
			when(registeredDTOService.registeredFromDTO(any(RegisteredDTO.class))).thenReturn(registered);
			when(registeredRepository.findById(anyString())).thenThrow(new ResourceNotFoundException(""));
			
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.updateRegistered(registeredDTO, request))
					.getMessage()).isEqualTo("Error while updating your profile");
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test updateRegistered should throw UnexpectedRollbackException on IllegalArgumentException")
		public void updateRegisteredTestShouldThrowsUnexpectedRollbackExceptionOnIllegalArgumentException() {
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
			when(registeredRepository.save(any(Registered.class))).thenThrow(new IllegalArgumentException());
			
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.updateRegistered(registeredDTO, request))
					.getMessage()).isEqualTo("Error while updating your profile");
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test updateRegistered should throw UnexpectedRollbackException on OptimisticLockingFailureException")
		public void updateRegisteredTestShouldThrowsUnexpectedRollbackExceptionOnOptimisticLockingFailureException() {
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
			when(registeredRepository.save(any(Registered.class))).thenThrow(new OptimisticLockingFailureException(""));
			
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.updateRegistered(registeredDTO, request))
					.getMessage()).isEqualTo("Error while updating your profile");
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test updateRegistered should throw UnexpectedRollbackException on any RuntimeException")
		public void updateRegisteredTestShouldThrowsUnexpectedRollbackExceptionOnAnyRuntimeException() {
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
			when(registeredRepository.save(any(Registered.class))).thenThrow(new RuntimeException(""));
			
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.updateRegistered(registeredDTO, request))
					.getMessage()).isEqualTo("Error while updating your profile");
		}
	}
	
	@Nested
	@Tag("getRegistrantsTests")
	@DisplayName("Tests for method getRegistrants")
	@TestInstance(Lifecycle.PER_CLASS)
	class GetRegistrantsTests {
		
		private Pageable pageRequest;

		@BeforeAll
		public void setUpForAllTests() {
			pageRequest = PageRequest.of(0, 3);
			requestMock = new MockHttpServletRequest();
			requestMock.setServerName("http://localhost:8080");
			requestMock.setRequestURI("/getRegistrants");
			request = new ServletWebRequest(requestMock);
		}

		@AfterAll
		public void unSetForAllTests() {
			pageRequest = null;
			requestMock = null;
			request = null;
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test getRegistrants should return page of RegisterdForListDTO")
		public void getRegistrantsTestShouldReturnPageOfRegisterdForListDTO() {
			//GIVEN
			Registered registeredA = new Registered("aaa@aaa.com", "aaaPasswd", "Aaa", "AAA", LocalDate.parse("01/01/1991", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "aaaIban");
			Registered registeredB = new Registered("bbb@bbb.com", "bbbPasswd", "Bbb", "BBB", LocalDate.parse("02/02/1992", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "bbbIban");
			RegisteredForListDTO registeredForListDTOA = new RegisteredForListDTO("aaa@aaa.com", "Aaa", "AAA");
			RegisteredForListDTO registeredForListDTOB = new RegisteredForListDTO("bbb@bbb.com", "Bbb", "BBB");
			List<RegisteredForListDTO> registrantsDTOExpected = Arrays.asList(registeredForListDTOA, registeredForListDTOB);
			when(registeredRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<Registered>(Arrays.asList(registeredA, registeredB), pageRequest, 2));
			when(registeredDTOService.registeredToForListDTO(any(Registered.class))).thenReturn(registeredForListDTOA).thenReturn(registeredForListDTOB);
			
			//WHEN			
			Page<RegisteredForListDTO> pageRegistrantsDTOResult = registeredService.getRegistrants(pageRequest, request);
			
			///THEN
			assertThat(pageRegistrantsDTOResult.getContent()).containsExactlyElementsOf(registrantsDTOExpected);
			assertThat(pageRegistrantsDTOResult.getPageable().getPageSize()).isEqualTo(3);
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test getRegistrants should throw UnexpectedRollbackException on NullPointerException")
		public void getRegistrantsTestShouldThrowsUnexpectedRollbackExceptionOnNullPointerException() {
			//GIVEN
			when(registeredRepository.findAll(any(Pageable.class))).thenThrow(new NullPointerException());
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.getRegistrants(pageRequest, request))
					.getMessage()).isEqualTo("Error while getting Registrants");
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test getRegistrants should throw UnexpectedRollbackException on any RuntimeException")
		public void getRegistrantsTestShouldThrowsUnexpectedRollbackExceptionOnAnyRuntimeException() {
			//GIVEN
			when(registeredRepository.findAll(any(Pageable.class))).thenThrow(new RuntimeException());
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.getRegistrants(pageRequest, request))
					.getMessage()).isEqualTo("Error while getting Registrants");
		}
	}
	
	@Nested
	@Tag("getAllAddByTests")
	@DisplayName("Tests for method getAllAddBy")
	@TestInstance(Lifecycle.PER_CLASS)
	class GetAllAddByTests {
		
		private Pageable pageRequest;

		@BeforeAll
		public void setUpForAllTests() {
			pageRequest = PageRequest.of(0, 3);
			requestMock = new MockHttpServletRequest();
			requestMock.setServerName("http://localhost:8080");
			requestMock.setRequestURI("/getAllAddBy?email=ccc@ccc.com");
			request = new ServletWebRequest(requestMock);
		}

		@AfterAll
		public void unSetForAllTests() {
			pageRequest = null;
			requestMock = null;
			request = null;
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test getAllAddBy should return page of RegisterdForListDTO")
		public void getAllAddByTestShouldReturnPageOfRegisterdForListDTO() {
			//GIVEN : C add A and B
			Registered registeredA = new Registered("aaa@aaa.com", "aaaPasswd", "Aaa", "AAA", LocalDate.parse("01/01/1991", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "aaaIban");
			Registered registeredB = new Registered("bbb@bbb.com", "bbbPasswd", "Bbb", "BBB", LocalDate.parse("02/02/1992", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "bbbIban");
			RegisteredForListDTO registeredForListDTOA = new RegisteredForListDTO("aaa@aaa.com", "Aaa", "AAA");
			RegisteredForListDTO registeredForListDTOB = new RegisteredForListDTO("bbb@bbb.com", "Bbb", "BBB");
			List<RegisteredForListDTO> registrantsDTOExpected = Arrays.asList(registeredForListDTOA, registeredForListDTOB);
			when(registeredRepository.findAllAddByEmail(anyString(), any(Pageable.class))).thenReturn(new PageImpl<Registered>(Arrays.asList(registeredA, registeredB), pageRequest, 2));
			when(registeredDTOService.registeredToForListDTO(any(Registered.class))).thenReturn(registeredForListDTOA).thenReturn(registeredForListDTOB);
			
			//WHEN			
			Page<RegisteredForListDTO> pageRegistrantsDTOResult = registeredService.getAllAddBy("ccc@ccc.com", pageRequest, request);
			
			///THEN
			assertThat(pageRegistrantsDTOResult.getContent()).containsExactlyElementsOf(registrantsDTOExpected);
			assertThat(pageRegistrantsDTOResult.getPageable().getPageSize()).isEqualTo(3);
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test getAllAddBy should throw UnexpectedRollbackException on NullPointerException")
		public void getAllAddByTestShouldThrowsUnexpectedRollbackExceptionOnNullPointerException() {
			//GIVEN
			when(registeredRepository.findAllAddByEmail(anyString(), any(Pageable.class))).thenThrow(new NullPointerException());
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.getAllAddBy("ccc@ccc.com", pageRequest, request))
					.getMessage()).isEqualTo("Error while getting connections you added");
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test getAllAddBys should throw UnexpectedRollbackException on any RuntimeException")
		public void getAllAddByTestShouldThrowsUnexpectedRollbackExceptionOnAnyRuntimeException() {
			//GIVEN
			when(registeredRepository.findAllAddByEmail(anyString(), any(Pageable.class))).thenThrow(new RuntimeException());
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.getAllAddBy("ccc@ccc.com", pageRequest, request))
					.getMessage()).isEqualTo("Error while getting connections you added");
		}
	}
	
	@Nested
	@Tag("getAllNotAddByTests")
	@DisplayName("Tests for method getAllNotAddBy")
	@TestInstance(Lifecycle.PER_CLASS)
	class GetAllNotAddByTests {
		
		private Pageable pageRequest;

		@BeforeAll
		public void setUpForAllTests() {
			pageRequest = PageRequest.of(0, 3);
			requestMock = new MockHttpServletRequest();
			requestMock.setServerName("http://localhost:8080");
			requestMock.setRequestURI("/getAllNotAddBy?email=ccc@ccc.com");
			request = new ServletWebRequest(requestMock);
		}

		@AfterAll
		public void unSetForAllTests() {
			pageRequest = null;
			requestMock = null;
			request = null;
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test getAllNotAddBy should return page of RegisterdForListDTO")
		public void getAllNotAddByTestShouldReturnPageOfRegisterdForListDTO() {
			//GIVEN : C can add A and B
			Registered registeredA = new Registered("aaa@aaa.com", "aaaPasswd", "Aaa", "AAA", LocalDate.parse("01/01/1991", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "aaaIban");
			Registered registeredB = new Registered("bbb@bbb.com", "bbbPasswd", "Bbb", "BBB", LocalDate.parse("02/02/1992", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "bbbIban");
			RegisteredForListDTO registeredForListDTOA = new RegisteredForListDTO("aaa@aaa.com", "Aaa", "AAA");
			RegisteredForListDTO registeredForListDTOB = new RegisteredForListDTO("bbb@bbb.com", "Bbb", "BBB");
			List<RegisteredForListDTO> registrantsDTOExpected = Arrays.asList(registeredForListDTOA, registeredForListDTOB);
			when(registeredRepository.findAllNotAddByEmail(anyString(), any(Pageable.class))).thenReturn(new PageImpl<Registered>(Arrays.asList(registeredA, registeredB), pageRequest, 2));
			when(registeredDTOService.registeredToForListDTO(any(Registered.class))).thenReturn(registeredForListDTOA).thenReturn(registeredForListDTOB);
			
			//WHEN			
			Page<RegisteredForListDTO> pageRegistrantsDTOResult = registeredService.getAllNotAddBy("ccc@ccc.com", pageRequest, request);
			
			///THEN
			assertThat(pageRegistrantsDTOResult.getContent()).containsExactlyElementsOf(registrantsDTOExpected);
			assertThat(pageRegistrantsDTOResult.getPageable().getPageSize()).isEqualTo(3);
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test getAllNotAddBy should throw UnexpectedRollbackException on NullPointerException")
		public void getAllNotAddByTestShouldThrowsUnexpectedRollbackExceptionOnNullPointerException() {
			//GIVEN
			when(registeredRepository.findAllNotAddByEmail(anyString(), any(Pageable.class))).thenThrow(new NullPointerException());
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.getAllNotAddBy("ccc@ccc.com", pageRequest, request))
					.getMessage()).isEqualTo("Error while getting connections you can add");	
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test getAllNotAddBy should throw UnexpectedRollbackException on any RuntimeException")
		public void getAllNotAddByTestShouldThrowsUnexpectedRollbackExceptionOnAnyRuntimeException() {
			//GIVEN
			when(registeredRepository.findAllNotAddByEmail(anyString(), any(Pageable.class))).thenThrow(new RuntimeException());
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.getAllNotAddBy("ccc@ccc.com", pageRequest, request))
					.getMessage()).isEqualTo("Error while getting connections you can add");
		}
	}
	
	@Nested
	@Tag("getAllAddedToTests")
	@DisplayName("Tests for method getAllAddedTo")
	@TestInstance(Lifecycle.PER_CLASS)
	class GetAllAddedTo {
		
		private Pageable pageRequest;

		@BeforeAll
		public void setUpForAllTests() {
			pageRequest = PageRequest.of(0, 3);
			requestMock = new MockHttpServletRequest();
			requestMock.setServerName("http://localhost:8080");
			requestMock.setRequestURI("/getAllAddedTo?email=ccc@ccc.com");
			request = new ServletWebRequest(requestMock);
		}

		@AfterAll
		public void unSetForAllTests() {
			pageRequest = null;
			requestMock = null;
			request = null;
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test getAllAddedTo should return page of RegisterdForListDTO")
		public void getAllAddedToTestShouldReturnPageOfRegisterdForListDTO() {
			//GIVEN : A and B add C so C was added by A and B
			Registered registeredA = new Registered("aaa@aaa.com", "aaaPasswd", "Aaa", "AAA", LocalDate.parse("01/01/1991", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "aaaIban");
			Registered registeredB = new Registered("bbb@bbb.com", "bbbPasswd", "Bbb", "BBB", LocalDate.parse("02/02/1992", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "bbbIban");
			RegisteredForListDTO registeredForListDTOA = new RegisteredForListDTO("aaa@aaa.com", "Aaa", "AAA");
			RegisteredForListDTO registeredForListDTOB = new RegisteredForListDTO("bbb@bbb.com", "Bbb", "BBB");
			List<RegisteredForListDTO> registrantsDTOExpected = Arrays.asList(registeredForListDTOA, registeredForListDTOB);
			when(registeredRepository.findAllAddedToEmail(anyString(), any(Pageable.class))).thenReturn(new PageImpl<Registered>(Arrays.asList(registeredA, registeredB), pageRequest, 2));
			when(registeredDTOService.registeredToForListDTO(any(Registered.class))).thenReturn(registeredForListDTOA).thenReturn(registeredForListDTOB);
			
			//WHEN			
			Page<RegisteredForListDTO> pageRegistrantsDTOResult = registeredService.getAllAddedTo("ccc@ccc.com", pageRequest, request);
			
			///THEN
			assertThat(pageRegistrantsDTOResult.getContent()).containsExactlyElementsOf(registrantsDTOExpected);
			assertThat(pageRegistrantsDTOResult.getPageable().getPageSize()).isEqualTo(3);
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test getAllAddedTo should throw UnexpectedRollbackException on NullPointerException")
		public void getAllAddedToTestShouldThrowsUnexpectedRollbackExceptionOnNullPointerException() {
			//GIVEN
			when(registeredRepository.findAllAddedToEmail(anyString(), any(Pageable.class))).thenThrow(new NullPointerException());
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.getAllAddedTo("ccc@ccc.com", pageRequest, request))
					.getMessage()).isEqualTo("Error while getting connected to you");	
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test getAllAddedTo should throw UnexpectedRollbackException on any RuntimeException")
		public void getAllAddedToTestShouldThrowsUnexpectedRollbackExceptionOnAnyRuntimeException() {
			//GIVEN
			when(registeredRepository.findAllAddedToEmail(anyString(), any(Pageable.class))).thenThrow(new RuntimeException());
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.getAllAddedTo("ccc@ccc.com", pageRequest, request))
					.getMessage()).isEqualTo("Error while getting connected to you");
		}
	}

	
	@Nested
	@Tag("removeRegisteredTests")
	@DisplayName("Tests for method removeRegistered")
	@TestInstance(Lifecycle.PER_CLASS)
	class RemoveRegisteredTests {

		@BeforeAll
		public void setUpForAllTests() {
			requestMock = new MockHttpServletRequest();
			requestMock.setServerName("http://localhost:8080");
			requestMock.setRequestURI("/removeRegistered?email=aaa@aaa.com");
			request = new ServletWebRequest(requestMock);
		}

		@AfterAll
		public void unSetForAllTests() {
			requestMock = null;
			request = null;
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test removeRegistered should throw UnexpectedRollbackException on IllegalArgumentException")
		public void removeRegisteredTestShouldThrowsUnexpectedRollbackExceptionOnIllegalArgumentException() {
			//GIVEN
			doThrow(new IllegalArgumentException()).when(registeredRepository).deleteById(anyString());
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.removeRegistered("aaa@aaa.com", request))
					.getMessage()).isEqualTo("Error while removing your profile");
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test removeRegistered should throw UnexpectedRollbackException on any RuntimeException")
		public void removeRegisteredTestShouldThrowsUnexpectedRollbackExceptionOnAnyRuntimeException() {
			//GIVEN
			doThrow(new RuntimeException()).when(registeredRepository).deleteById(anyString());
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.removeRegistered("aaa@aaa.com", request))
					.getMessage()).isEqualTo("Error while removing your profile");
		}
	}
}
