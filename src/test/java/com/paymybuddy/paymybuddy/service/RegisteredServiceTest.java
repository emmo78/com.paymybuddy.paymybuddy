package com.paymybuddy.paymybuddy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import com.paymybuddy.paymybuddy.exception.WithdrawException;
import com.paymybuddy.paymybuddy.model.Registered;
import com.paymybuddy.paymybuddy.model.Role;
import com.paymybuddy.paymybuddy.repository.RegisteredRepository;
import com.paymybuddy.paymybuddy.repository.RoleRepository;

@ExtendWith(MockitoExtension.class)
public class RegisteredServiceTest {
	
	@InjectMocks
	private RegisteredServiceImpl registeredService;
	
	@Mock
	private RegisteredDTOService registeredDTOService;
	
	@Mock
	private RegisteredRepository registeredRepository;
	
	@Mock
	private RoleRepository roleRepository;
	
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
		private Role role;
		
		@BeforeAll
		public void setUpForAllTests() {
			role = new Role();
			role.setRoleId(1);
			role.setRoleName("USER");
			requestMock = new MockHttpServletRequest();
			requestMock.setServerName("http://localhost:8080");
			requestMock.setRequestURI("/createRegistered");
			request = new ServletWebRequest(requestMock);
		}

		@AfterAll
		public void unSetForAllTests() {
			requestMock = null;
			request = null;
			role = null;
		}
		
		@BeforeEach
		public void setUpForEachTests() {
			registeredDTO = new RegisteredDTO();
			registeredDTO.setEmail("Aaa@Aaa.com");
			registered = new Registered();
			registered.setEmail("aaa@aaa.com");
			registered.setPassword("aaaPasswd");
			registered.setFirstName("Aaa");
			registered.setLastName("AAA");
			registered.setBirthDate(LocalDate.parse("01/21/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
			registered.setIban("aaaIban");
			registered.setBalance(100);
		}
		
		@AfterEach
		public void unSetForEachTests() {
			registeredService=null;
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
		@DisplayName("test createRegistered should throw UnexpectedRollbackException on ResourceNotFoundException")
		public void createRegisteredTestShouldThrowUnexpectedRollbackExceptionOnResourceNotFoundException() {
			//GIVEN
			when(registeredDTOService.registeredFromDTO(any(RegisteredDTO.class))).thenReturn(registered);
			when(registeredRepository.existsById(anyString())).thenReturn(false);
			when(roleRepository.findById(anyInt())).thenReturn(Optional.ofNullable(null));
			
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
				() -> registeredService.createRegistered(registeredDTO, request))
				.getMessage()).isEqualTo("Error while creating your profile");
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test createRegistered should throw UnexpectedRollbackException on IllegalArgumentException")
		public void createRegisteredTestShouldThrowUnexpectedRollbackExceptionOnIllegalArgumentException() {
			//GIVEN
			when(registeredDTOService.registeredFromDTO(any(RegisteredDTO.class))).thenReturn(registered);
			when(registeredRepository.existsById(anyString())).thenReturn(false);
			when(roleRepository.findById(anyInt())).thenReturn(Optional.of(role));
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
			when(roleRepository.findById(anyInt())).thenReturn(Optional.of(role));
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
			when(roleRepository.findById(anyInt())).thenReturn(Optional.of(role));
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
		
		@AfterEach
		public void unSetForEachTests() {
			registeredService=null;
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test getRegistered should throw UnexpectedRollbackException on ResourceNotFoundException")
		public void getRegisteredTestShouldThrowsResourceNotFoundException() {
			//GIVEN
			when(registeredRepository.findById(anyString())).thenReturn(Optional.ofNullable(null));
			
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
				() -> registeredService.getRegistered("aaa@aaa.com", request))
				.getMessage()).isEqualTo("Error while getting your profile");
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
		
		private RegisteredDTO registeredDTO;
		private Registered registered;
		private PasswordEncoder passwordEncoder;
		private Registered registeredToUpate;
		
		@BeforeAll
		public void setUpForAllTests() {
			passwordEncoder = new BCryptPasswordEncoder();
			requestMock = new MockHttpServletRequest();
			requestMock.setServerName("http://localhost:8080");
			requestMock.setRequestURI("/updateRegistered");
			request = new ServletWebRequest(requestMock);
		}

		@AfterAll
		public void unSetForAllTests() {
			passwordEncoder = null;
			requestMock = null;
			request = null;
		}
		
		@BeforeEach
		public void setUpForEachTests() {
			registeredDTO = new RegisteredDTO();
			registeredDTO.setEmail("Aaa@Aaa.com"); //NOT Updated
			registeredDTO.setPassword(null); //NOT Updated
			registeredDTO.setFirstName("Aaa"); //Equal -> expect not Updated
			registeredDTO.setLastName(null); //Null -> expect not updated
			registeredDTO.setBirthDate("02/02/1992"); //expect updated
			registeredDTO.setIban("FR7601234567890123456789"); //expect updated
			registeredDTO.setBalance(null); //NOT Updated

			registered = new Registered();
			registered.setEmail("aaa@aaa.com");
			registered.setPassword(null);
			registered.setFirstName("Aaa");
			registered.setLastName(null);
			registered.setBirthDate(LocalDate.parse("02/02/1992", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
			registered.setIban("FR7601234567890123456789");
			registered.setBalance(0);

			registeredToUpate = new Registered();
			registeredToUpate.setEmail("aaa@aaa.com");
			registeredToUpate.setPassword(passwordEncoder.encode("aaaPasswd"));
			registeredToUpate.setFirstName("Aaa");
			registeredToUpate.setLastName("AAA");
			registeredToUpate.setBirthDate(LocalDate.parse("01/01/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
			registeredToUpate.setIban(null);
			registeredToUpate.setBalance(100);
		}
		
		@AfterEach
		public void unSetForEachTests() {
			registeredService=null;
			registeredDTO = null;
			registered = null;
			registeredToUpate = null;
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test updateRegistered should update not null and equal")
		public void updateRegisteredTestShouldUpdateNotNullAndEqual() {
			//GIVEN
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
			when(registeredDTOService.registeredFromDTO(any(RegisteredDTO.class))).thenReturn(registered);
			when(registeredRepository.findById(anyString())).thenReturn(Optional.ofNullable(null));
			
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
		
		@AfterEach
		public void unSetForEachTests() {
			registeredService=null;
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test getRegistrants should return page of RegisterdForListDTO")
		public void getRegistrantsTestShouldReturnPageOfRegisterdForListDTO() {
			//GIVEN
			Registered 	registeredA = new Registered();
			registeredA.setEmail("aaa@aaa.com");
			registeredA.setPassword("aaaPasswd");
			registeredA.setFirstName("Aaa");
			registeredA.setLastName("AAA");
			registeredA.setBirthDate(LocalDate.parse("01/21/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
			registeredA.setIban("aaaIban");
			registeredA.setBalance(100);

			Registered 	registeredB = new Registered();
			registeredB.setEmail("bbb@bbb.com");
			registeredB.setPassword("bbbPasswd");
			registeredB.setFirstName("Bbb");
			registeredB.setLastName("BBB");
			registeredB.setBirthDate(LocalDate.parse("02/22/1992", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
			registeredB.setIban("bbbIban");
			registeredB.setBalance(200);

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
		
		@AfterEach
		public void unSetForEachTests() {
			registeredService=null;
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test getAllAddBy should return page of RegisteredForListDTO")
		public void getAllAddByTestShouldReturnPageOfRegisterdForListDTO() {
			//GIVEN : C has added A and B
			Registered 	registeredA = new Registered();
			registeredA.setEmail("aaa@aaa.com");
			registeredA.setPassword("aaaPasswd");
			registeredA.setFirstName("Aaa");
			registeredA.setLastName("AAA");
			registeredA.setBirthDate(LocalDate.parse("01/21/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
			registeredA.setIban("aaaIban");
			registeredA.setBalance(100);

			Registered 	registeredB = new Registered();
			registeredB.setEmail("bbb@bbb.com");
			registeredB.setPassword("bbbPasswd");
			registeredB.setFirstName("Bbb");
			registeredB.setLastName("BBB");
			registeredB.setBirthDate(LocalDate.parse("02/22/1992", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
			registeredB.setIban("bbbIban");
			registeredB.setBalance(200);
			
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
		
		@AfterEach
		public void unSetForEachTests() {
			registeredService=null;
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test getAllNotAddBy should return page of RegisterdForListDTO")
		public void getAllNotAddByTestShouldReturnPageOfRegisterdForListDTO() {
			//GIVEN : C can add A and B
			Registered 	registeredA = new Registered();
			registeredA.setEmail("aaa@aaa.com");
			registeredA.setPassword("aaaPasswd");
			registeredA.setFirstName("Aaa");
			registeredA.setLastName("AAA");
			registeredA.setBirthDate(LocalDate.parse("01/21/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
			registeredA.setIban("aaaIban");
			registeredA.setBalance(100);

			Registered 	registeredB = new Registered();
			registeredB.setEmail("bbb@bbb.com");
			registeredB.setPassword("bbbPasswd");
			registeredB.setFirstName("Bbb");
			registeredB.setLastName("BBB");
			registeredB.setBirthDate(LocalDate.parse("02/22/1992", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
			registeredB.setIban("bbbIban");
			registeredB.setBalance(200);
			
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
		
		@AfterEach
		public void unSetForEachTests() {
			registeredService=null;
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test getAllAddedTo should return page of RegisterdForListDTO")
		public void getAllAddedToTestShouldReturnPageOfRegisterdForListDTO() {
			//GIVEN : A and B has added C so C was added by A and B
			Registered 	registeredA = new Registered();
			registeredA.setEmail("aaa@aaa.com");
			registeredA.setPassword("aaaPasswd");
			registeredA.setFirstName("Aaa");
			registeredA.setLastName("AAA");
			registeredA.setBirthDate(LocalDate.parse("01/21/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
			registeredA.setIban("aaaIban");
			registeredA.setBalance(100);

			Registered 	registeredB = new Registered();
			registeredB.setEmail("bbb@bbb.com");
			registeredB.setPassword("bbbPasswd");
			registeredB.setFirstName("Bbb");
			registeredB.setLastName("BBB");
			registeredB.setBirthDate(LocalDate.parse("02/22/1992", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
			registeredB.setIban("bbbIban");
			registeredB.setBalance(200);
			
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
	@Tag("addConnectionTests")
	@DisplayName("Tests for method addConnection")
	@TestInstance(Lifecycle.PER_CLASS)
	class AddConnectionTests {
		
		private Registered registeredA;
		private Registered registeredB;
		
		@BeforeAll
		public void setUpForAllTests() {
			requestMock = new MockHttpServletRequest();
			requestMock.setServerName("http://localhost:8080");
			requestMock.setRequestURI("/addConnection?email=aaa@aaa.com&emailToAdd=bbb@bbb.com");
			request = new ServletWebRequest(requestMock);
		}

		@AfterAll
		public void unSetForAllTests() {
			requestMock = null;
			request = null;
		}
		
		@BeforeEach
		public void setUpForEachTests() {
			registeredA = new Registered();
			registeredA.setEmail("aaa@aaa.com");
			registeredA.setPassword("aaaPasswd");
			registeredA.setFirstName("Aaa");
			registeredA.setLastName("AAA");
			registeredA.setBirthDate(LocalDate.parse("01/21/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
			registeredA.setIban("aaaIban");
			registeredA.setBalance(100);

			registeredB = new Registered();
			registeredB.setEmail("bbb@bbb.com");
			registeredB.setPassword("bbbPasswd");
			registeredB.setFirstName("Bbb");
			registeredB.setLastName("BBB");
			registeredB.setBirthDate(LocalDate.parse("02/22/1992", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
			registeredB.setIban("bbbIban");
			registeredB.setBalance(200);		}
		
		@AfterEach
		public void unSetForEachTests() {
			registeredService=null;
			registeredA = null;
			registeredB = null;
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test addConnection should fill their sets")
		public void addConnectionTestShouldFillTheirSets() {
			//GIVEN
			when(registeredRepository.findById(anyString())).thenReturn(Optional.of(registeredA)).thenReturn(Optional.of(registeredB));
			ArgumentCaptor<Registered> registeredResultCapt = ArgumentCaptor.forClass(Registered.class);
			when(registeredRepository.save(any(Registered.class))).thenReturn(registeredA);
			
			//WHEN
			registeredService.addConnection("aaa@aaa.com", "bbb@bbb.com", request);
			
			//THEN
			verify(registeredRepository, times(1)).save(registeredResultCapt.capture());
			Registered registeredAResult = registeredResultCapt.getValue();
			Registered registerdBResullt = registeredAResult.getAddConnections().stream().collect(Collectors.toList()).get(0);
			assertThat(registerdBResullt).isEqualTo(registeredB);
			//assertThat(registerdBResullt.getAddedConnections()).containsOnly(registeredA);
		}
		
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test addConnection should throw UnexpectedRollbackException on ResourceNotFoundException")
		public void addConnectionTestShouldThrowsUnexpectedRollbackExceptionOnResourceNotFoundException() {
			//GIVEN
			when(registeredRepository.findById(anyString())).thenReturn(Optional.ofNullable(null));
			
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.addConnection("aaa@aaa.com", "bbb@bbb.com", request))
					.getMessage()).isEqualTo("Error while adding connection");
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test addConnection should throw UnexpectedRollbackException on IllegalArgumentException")
		public void addConnectionTestShouldThrowsUnexpectedRollbackExceptionOnIllegalArgumentException() {
			//GIVEN
			when(registeredRepository.findById(anyString())).thenReturn(Optional.of(registeredA)).thenReturn(Optional.of(registeredB));
			when(registeredRepository.save(any(Registered.class))).thenThrow(new IllegalArgumentException());
			
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.addConnection("aaa@aaa.com", "bbb@bbb.com", request))
					.getMessage()).isEqualTo("Error while adding connection");
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test addConnection should throw UnexpectedRollbackException on OptimisticLockingFailureException")
		public void addConnectionTestShouldThrowsUnexpectedRollbackExceptionOnOptimisticLockingFailureException() {
			//GIVEN
			when(registeredRepository.findById(anyString())).thenReturn(Optional.of(registeredA)).thenReturn(Optional.of(registeredB));
			when(registeredRepository.save(any(Registered.class))).thenThrow(new OptimisticLockingFailureException(""));
			
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.addConnection("aaa@aaa.com", "bbb@bbb.com", request))
					.getMessage()).isEqualTo("Error while adding connection");
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test addConnection should throw UnexpectedRollbackException on any RuntimeException")
		public void addConnectionTestShouldThrowsUnexpectedRollbackExceptionOnAnyRuntimeException() {
			//GIVEN
			when(registeredRepository.findById(anyString())).thenReturn(Optional.of(registeredA)).thenReturn(Optional.of(registeredB));
			when(registeredRepository.save(any(Registered.class))).thenThrow(new RuntimeException(""));
			
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.addConnection("aaa@aaa.com", "bbb@bbb.com", request))
					.getMessage()).isEqualTo("Error while adding connection");
		}
	}
	
	@Nested
	@Tag("removeConnectionTests")
	@DisplayName("Tests for method removeConnection")
	@TestInstance(Lifecycle.PER_CLASS)
	class RemoveConnectionTests {
		
		private Registered registeredA;
		private Registered registeredB;
		
		@BeforeAll
		public void setUpForAllTests() {
			requestMock = new MockHttpServletRequest();
			requestMock.setServerName("http://localhost:8080");
			requestMock.setRequestURI("/removeConnection?email=aaa@aaa.com&emailToRemove=bbb@bbb.com");
			request = new ServletWebRequest(requestMock);
		}

		@AfterAll
		public void unSetForAllTests() {
			requestMock = null;
			request = null;
		}
		
		@BeforeEach
		public void setUpForEachTests() {
			registeredA = new Registered();
			registeredA.setEmail("aaa@aaa.com");
			registeredA.setPassword("aaaPasswd");
			registeredA.setFirstName("Aaa");
			registeredA.setLastName("AAA");
			registeredA.setBirthDate(LocalDate.parse("01/21/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
			registeredA.setIban("aaaIban");
			registeredA.setBalance(100);

			registeredB = new Registered();
			registeredB.setEmail("bbb@bbb.com");
			registeredB.setPassword("bbbPasswd");
			registeredB.setFirstName("Bbb");
			registeredB.setLastName("BBB");
			registeredB.setBirthDate(LocalDate.parse("02/22/1992", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
			registeredB.setIban("bbbIban");
			registeredB.setBalance(200);		}
		
		@AfterEach
		public void unSetForEachTests() {
			registeredService=null;
			registeredA = null;
			registeredB = null;
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test removeConnection should remove them from their sets")
		public void removeConnectionTestShouldRemoveThemFromTheirSets() {
			//GIVEN
			registeredA.addConnection(registeredB);
			// registeredB is a pointer to Object !
			when(registeredRepository.findById(anyString())).thenReturn(Optional.of(registeredA)).thenReturn(Optional.of(registeredB));
			ArgumentCaptor<Registered> registeredResultCapt = ArgumentCaptor.forClass(Registered.class);
			when(registeredRepository.save(any(Registered.class))).thenReturn(registeredA);
			
			//WHEN
			registeredService.removeConnection("aaa@aaa.com", "bbb@bbb.com", request);
			
			//THEN
			verify(registeredRepository, times(1)).save(registeredResultCapt.capture());
			Registered registeredAResult = registeredResultCapt.getValue();
			assertThat(registeredAResult.getAddConnections()).isEmpty();
			//assertThat(registeredB.getAddedConnections()).isEmpty();
		}
		
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test removeConnection should throw UnexpectedRollbackException on ResourceNotFoundException")
		public void removeConnectionTestShouldThrowsUnexpectedRollbackExceptionOnResourceNotFoundException() {
			//GIVEN
			when(registeredRepository.findById(anyString())).thenReturn(Optional.ofNullable(null));
			
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.removeConnection("aaa@aaa.com", "bbb@bbb.com", request))
					.getMessage()).isEqualTo("Error while removing connection");
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test removeConnection should throw UnexpectedRollbackException on IllegalArgumentException")
		public void removeConnectionTestShouldThrowsUnexpectedRollbackExceptionOnIllegalArgumentException() {
			//GIVEN
			when(registeredRepository.findById(anyString())).thenReturn(Optional.of(registeredA)).thenReturn(Optional.of(registeredB));
			when(registeredRepository.save(any(Registered.class))).thenThrow(new IllegalArgumentException());
			
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.removeConnection("aaa@aaa.com", "bbb@bbb.com", request))
					.getMessage()).isEqualTo("Error while removing connection");
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test removeConnection should throw UnexpectedRollbackException on OptimisticLockingFailureException")
		public void removeConnectionTestShouldThrowsUnexpectedRollbackExceptionOnOptimisticLockingFailureException() {
			//GIVEN
			when(registeredRepository.findById(anyString())).thenReturn(Optional.of(registeredA)).thenReturn(Optional.of(registeredB));
			when(registeredRepository.save(any(Registered.class))).thenThrow(new OptimisticLockingFailureException(""));
			
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.removeConnection("aaa@aaa.com", "bbb@bbb.com", request))
					.getMessage()).isEqualTo("Error while removing connection");
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test removeConnection should throw UnexpectedRollbackException on any RuntimeException")
		public void removeConnectionTestShouldThrowsUnexpectedRollbackExceptionOnAnyRuntimeException() {
			//GIVEN
			when(registeredRepository.findById(anyString())).thenReturn(Optional.of(registeredA)).thenReturn(Optional.of(registeredB));
			when(registeredRepository.save(any(Registered.class))).thenThrow(new RuntimeException(""));
			
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.removeConnection("aaa@aaa.com", "bbb@bbb.com", request))
					.getMessage()).isEqualTo("Error while removing connection");
		}
	}
	
	@Nested
	@Tag("removeRegisteredTests")
	@DisplayName("Tests for method removeRegistered")
	@TestInstance(Lifecycle.PER_CLASS)
	class RemoveRegisteredTests {
		
		private Registered registeredA;

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
		
		@BeforeEach
		public void setUpForEachTests() {
			registeredA = new Registered();
			registeredA.setEmail("aaa@aaa.com");
			registeredA.setPassword("aaaPasswd");
			registeredA.setFirstName("Aaa");
			registeredA.setLastName("AAA");
			registeredA.setBirthDate(LocalDate.parse("01/21/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
			registeredA.setIban("aaaIban");
			registeredA.setBalance(100);
		}
		
		@AfterEach
		public void unSetForEachTests() {
			registeredService=null;
			registeredA = null;
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test removeRegistered should throw UnexpectedRollbackException on ResourceNotFoundException")
		public void removeRegisteredTestShouldThrowsUnexpectedRollbackExceptionOnResourceNotFoundException() {
			//GIVEN
			when(registeredRepository.findById(anyString())).thenReturn(Optional.ofNullable(null));
			
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.removeRegistered("aaa@aaa.com", request))
					.getMessage()).isEqualTo("Error while removing your profile");
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test removeRegistered should throw UnexpectedRollbackException on IllegalArgumentException")
		public void removeRegisteredTestShouldThrowsUnexpectedRollbackExceptionOnIllegalArgumentException() {
			//GIVEN
			when(registeredRepository.findById(anyString())).thenReturn(Optional.of(registeredA));
			when(registeredRepository.findAllAddedToEmail(anyString(), any(Pageable.class))).thenReturn(new PageImpl<Registered>(new ArrayList<Registered>(), Pageable.unpaged(), 0));
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
			when(registeredRepository.findById(anyString())).thenReturn(Optional.of(registeredA));
			when(registeredRepository.findAllAddedToEmail(anyString(), any(Pageable.class))).thenReturn(new PageImpl<Registered>(new ArrayList<Registered>(), Pageable.unpaged(), 0));
			doThrow(new RuntimeException()).when(registeredRepository).deleteById(anyString());
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.removeRegistered("aaa@aaa.com", request))
					.getMessage()).isEqualTo("Error while removing your profile");
		}
	}
	
	@Nested
	@Tag("depositFromBankTests")
	@DisplayName("Tests for method depositFromBank")
	@TestInstance(Lifecycle.PER_CLASS)
	class DepositFromBankTests {
		
		private Registered registeredA;

		@BeforeAll
		public void setUpForAllTests() {
			requestMock = new MockHttpServletRequest();
			requestMock.setServerName("http://localhost:8080");
			requestMock.setRequestURI("/depositFromBank?email=aaa@aaa.com&amount=110");
			request = new ServletWebRequest(requestMock);
		}

		@AfterAll
		public void unSetForAllTests() {
			requestMock = null;
			request = null;
		}
		
		@BeforeEach
		public void setUpForEachTests() {
			registeredA = new Registered();
			registeredA.setEmail("aaa@aaa.com");
			registeredA.setPassword("aaaPasswd");
			registeredA.setFirstName("Aaa");
			registeredA.setLastName("AAA");
			registeredA.setBirthDate(LocalDate.parse("01/21/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
			registeredA.setIban("aaaIban");
			registeredA.setBalance(100);
		}
		
		@AfterEach
		public void unSetForEachTests() {
			registeredService=null;
			registeredA = null;
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test depositFromBank should sum balance and amount")
		public void depositFromBankTestShouldSumBalanceAndAmount() {
			//GIVEN
			when(registeredRepository.findById(anyString())).thenReturn(Optional.of(registeredA));
			ArgumentCaptor<Registered> registeredResultCapt = ArgumentCaptor.forClass(Registered.class);
			when(registeredRepository.save(any(Registered.class))).thenReturn(registeredA);
			
			//WHEN
			registeredService.depositFromBank("aaa@aaa.com", 110.00, request);
			//THEN
			verify(registeredRepository, times(1)).save(registeredResultCapt.capture());
			assertThat(registeredResultCapt.getValue().getBalance()).isEqualTo(210d);
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test depositFromBank should throw UnexpectedRollbackException on ResourceNotFoundException")
		public void depositFromBankTestShouldThrowsUnexpectedRollbackExceptionOnResourceNotFoundException() {
			//GIVEN
			when(registeredRepository.findById(anyString())).thenReturn(Optional.ofNullable(null));
			
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.depositFromBank("aaa@aaa.com", 110.0, request))
					.getMessage()).isEqualTo("Error while deposit money from bank. Canceled");
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test depositFromBank should throw UnexpectedRollbackException on IllegalArgumentException")
		public void depositFromBankTestShouldThrowsUnexpectedRollbackExceptionOnIllegalArgumentException() {
			//GIVEN
			when(registeredRepository.findById(anyString())).thenReturn(Optional.of(registeredA));
			when(registeredRepository.save(any(Registered.class))).thenThrow(new IllegalArgumentException());
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.depositFromBank("aaa@aaa.com", 110.0, request))
					.getMessage()).isEqualTo("Error while deposit money from bank. Canceled");
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test depositFromBank should throw UnexpectedRollbackException on OptimisticLockingFailureException")
		public void depositFromBankTestShouldThrowsUnexpectedRollbackExceptionOnOptimisticLockingFailureException() {
			//GIVEN
			when(registeredRepository.findById(anyString())).thenReturn(Optional.of(registeredA));
			when(registeredRepository.save(any(Registered.class))).thenThrow(new OptimisticLockingFailureException(""));
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.depositFromBank("aaa@aaa.com", 110.0, request))
					.getMessage()).isEqualTo("Error while deposit money from bank. Canceled");
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test depositFromBank should throw UnexpectedRollbackException on any RuntimeException")
		public void depositFromBankTestShouldThrowsUnexpectedRollbackExceptionOnAnyRuntimeException() {
			//GIVEN
			when(registeredRepository.findById(anyString())).thenReturn(Optional.of(registeredA));
			when(registeredRepository.save(any(Registered.class))).thenThrow(new RuntimeException());
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.depositFromBank("aaa@aaa.com", 110.0, request))
					.getMessage()).isEqualTo("Error while deposit money from bank. Canceled");
		}
	}
	
	@Nested
	@Tag("withdrawToBankTests")
	@DisplayName("Tests for method withdrawToBank")
	@TestInstance(Lifecycle.PER_CLASS)
	class WithdrawToBankTests {
		
		private Registered registeredA;

		@BeforeAll
		public void setUpForAllTests() {
			requestMock = new MockHttpServletRequest();
			requestMock.setServerName("http://localhost:8080");
			requestMock.setRequestURI("/withdrawToBank?email=aaa@aaa.com&amount=110");
			request = new ServletWebRequest(requestMock);
		}

		@AfterAll
		public void unSetForAllTests() {
			requestMock = null;
			request = null;
		}
		
		@BeforeEach
		public void setUpForEachTests() {
			registeredA = new Registered();
			registeredA.setEmail("aaa@aaa.com");
			registeredA.setPassword("aaaPasswd");
			registeredA.setFirstName("Aaa");
			registeredA.setLastName("AAA");
			registeredA.setBirthDate(LocalDate.parse("01/21/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
			registeredA.setIban("aaaIban");
			registeredA.setBalance(120);
		}
		
		@AfterEach
		public void unSetForEachTests() {
			registeredService=null;
			registeredA = null;
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test withdrawToBank should sum balance and amount")
		public void withdrawToBankTestShouldSumBalanceAndAmount() {
			//GIVEN
			when(registeredRepository.findById(anyString())).thenReturn(Optional.of(registeredA));
			ArgumentCaptor<Registered> registeredResultCapt = ArgumentCaptor.forClass(Registered.class);
			when(registeredRepository.save(any(Registered.class))).thenReturn(registeredA);
			
			//WHEN
			registeredService.withdrawToBank("aaa@aaa.com", 110.0, request);
			//THEN
			verify(registeredRepository, times(1)).save(registeredResultCapt.capture());
			assertThat(registeredResultCapt.getValue().getBalance()).isEqualTo(10d);
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test withdrawToBank with insuficient balance should throw WithdrawException")
		public void withdrawToBankTestWithInsufientBalanceShouldThrowsWithdrawException() {
			//GIVEN
			registeredA.setBalance(100);
			when(registeredRepository.findById(anyString())).thenReturn(Optional.of(registeredA));
			
			//WHEN
			//THEN
			assertThat(assertThrows(WithdrawException.class,
					() -> registeredService.withdrawToBank("aaa@aaa.com", 110.0, request))
					.getMessage()).isEqualTo("Insufficient funds for withdraw to bank");
		}		
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test withdrawToBank should throw UnexpectedRollbackException on ResourceNotFoundException")
		public void withdrawToBankTestShouldThrowsUnexpectedRollbackExceptionOnResourceNotFoundException() {
			//GIVEN
			when(registeredRepository.findById(anyString())).thenReturn(Optional.ofNullable(null));
			
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.withdrawToBank("aaa@aaa.com", 110.0, request))
					.getMessage()).isEqualTo("Error while withdraw to bank. Canceled");
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test withdrawToBank should throw UnexpectedRollbackException on IllegalArgumentException")
		public void withdrawToBankTestShouldThrowsUnexpectedRollbackExceptionOnIllegalArgumentException() {
			//GIVEN
			when(registeredRepository.findById(anyString())).thenReturn(Optional.of(registeredA));
			when(registeredRepository.save(any(Registered.class))).thenThrow(new IllegalArgumentException());
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.withdrawToBank("aaa@aaa.com", 110.0, request))
					.getMessage()).isEqualTo("Error while withdraw to bank. Canceled");
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test withdrawToBank should throw UnexpectedRollbackException on OptimisticLockingFailureException")
		public void withdrawToBankTestShouldThrowsUnexpectedRollbackExceptionOnOptimisticLockingFailureException() {
			//GIVEN
			when(registeredRepository.findById(anyString())).thenReturn(Optional.of(registeredA));
			when(registeredRepository.save(any(Registered.class))).thenThrow(new OptimisticLockingFailureException(""));
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.withdrawToBank("aaa@aaa.com", 110.0, request))
					.getMessage()).isEqualTo("Error while withdraw to bank. Canceled");
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test withdrawToBank should throw UnexpectedRollbackException on any RuntimeException")
		public void withdrawToBankTestShouldThrowsUnexpectedRollbackExceptionOnAnyRuntimeException() {
			//GIVEN
			when(registeredRepository.findById(anyString())).thenReturn(Optional.of(registeredA));
			when(registeredRepository.save(any(Registered.class))).thenThrow(new RuntimeException());
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.withdrawToBank("aaa@aaa.com", 110.0, request))
					.getMessage()).isEqualTo("Error while withdraw to bank. Canceled");
		}
	}
}
