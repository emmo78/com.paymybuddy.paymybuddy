package com.paymybuddy.paymybuddy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.transaction.Transactional;

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
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.paymybuddy.paymybuddy.dto.RegisteredDTO;
import com.paymybuddy.paymybuddy.exception.ResourceConflictException;
import com.paymybuddy.paymybuddy.model.Registered;
import com.paymybuddy.paymybuddy.model.Role;
import com.paymybuddy.paymybuddy.repository.RegisteredRepository;
import com.paymybuddy.paymybuddy.repository.RoleRepository;

@SpringBootTest
public class RegisteredServiceIT {
	
	@Autowired
	private RegisteredService registeredService;

	@Autowired
	private RegisteredRepository registeredRepository;
	
	private MockHttpServletRequest requestMock;
	private WebRequest request;
	private RegisteredDTO registeredDTO;
	private PasswordEncoder passwordEncoder;
	
	@Nested
	@Tag("createRegisteredIT")
	@DisplayName("IT for method createRegistered")
	@TestInstance(Lifecycle.PER_CLASS)
	class CreateRegisteredIT {
		
		@BeforeAll
		public void setUpForAllTests() {
			passwordEncoder = new BCryptPasswordEncoder();
			requestMock = new MockHttpServletRequest();
			requestMock.setServerName("http://localhost:8080");
			requestMock.setRequestURI("/createRegistered");
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
		@Transactional
		public void createRegisteredITShouldCommitItAndReturnRegisterdDTOWithNoPassword() {
			
			//GIVEN
			Role roleExpected = new Role();
			roleExpected.setRoleId(1);
			roleExpected.setRoleName("USER");

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
			assertThat(registeredResultOpt.get().getRoles().get(0)).usingRecursiveComparison().isEqualTo(roleExpected);
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
			Registered registeredResult = registeredRepository.findById("aaa@aaa.com").get();
			assertThat(registeredResult).extracting(
					Registered::getEmail,
					Registered::getFirstName,
					Registered::getLastName,
					Registered::getBirthDate,
					Registered::getIban,
					Registered::getBalance).containsExactly(
							"aaa@aaa.com",
							"Aaa",
							"AAA",
							LocalDate.parse("02/28/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")),
							null,
							0d);
			assertThat(passwordEncoder.matches("bbbPasswd", registeredResult.getPassword())).isFalse();
			assertThat(passwordEncoder.matches("aaaPasswd", registeredResult.getPassword())).isTrue();
		}
	}
	
	@Nested
	@Tag("updateRegisteredIT")
	@DisplayName("IT for method updateRegistered")
	@TestInstance(Lifecycle.PER_CLASS)
	class UpdateRegisteredIT {
		
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
			requestMock=null;
			request=null;
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
		}
		
		@AfterEach
		public void unSetForEachTests() {
			registeredRepository.deleteAll();
			registeredDTO = null;
		}
		
		@Test
		@Tag("RegisteredServiceIT")
		@DisplayName("IT UpdateRegistered should update and commit")
		public void updateRegisteredITShouldUpdateAndCommit() {
			//GIVEN
			Registered registeredToUpate =  new Registered(
					"aaa@aaa.com",
					passwordEncoder.encode("aaaPasswd"),
					"Aaa",
					"AAA",
					LocalDate.parse("01/01/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")),
					null);
			registeredToUpate.setBalance(100);
			registeredRepository.saveAndFlush(registeredToUpate);
			
			//WHEN
			RegisteredDTO registerdDTOResult = registeredService.updateRegistered(registeredDTO, request);
			Registered registeredResult = registeredRepository.findById("aaa@aaa.com").get();
			
			//THEN
			assertThat(registerdDTOResult).extracting(
					RegisteredDTO::getEmail,
					RegisteredDTO::getFirstName,
					RegisteredDTO::getLastName,
					RegisteredDTO::getBirthDate,
					RegisteredDTO::getIban,
					RegisteredDTO::getBalance).containsExactly(
							"aaa@aaa.com",
							"Aaa",
							"AAA",
							"02/02/1992",
							"FR7601234567890123456789",
							"100.00");
			assertThat(registeredResult).extracting(
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
			assertThat(passwordEncoder.matches("aaaPasswd", registeredResult.getPassword())).isTrue();
		}

		@Test
		@Tag("RegisteredServiceIT")
		@DisplayName("IT UpdateRegistered throws UnexpectedRollbackException should rollback")
		public void updateRegisteredITthrowsUnexpectedRollbackExceptionShouldRollback() {
			//GIVEN
			//WHEN
			//THEN
			assertThat(assertThrows(UnexpectedRollbackException.class,
					() -> registeredService.updateRegistered(registeredDTO, request))
					.getMessage()).isEqualTo("Error while updating your profile");
			assertThat(registeredRepository.findById("aaa@aaa.com")).isEmpty();
		}
	}
	
	@Nested
	@Tag("removeRegisteredIT")
	@DisplayName("IT for method removeRegistered")
	@TestInstance(Lifecycle.PER_CLASS)
	class RemoveRegisteredTests {
		
		@Autowired
		RoleRepository roleRepository; 
		
		@BeforeAll
		public void setUpForAllTests() {
			requestMock = new MockHttpServletRequest();
			requestMock.setServerName("http://localhost:8080");
			requestMock.setRequestURI("/removeRegistered?email=bbb@bbb.com");
			request = new ServletWebRequest(requestMock);
		}

		@AfterAll
		public void unSetForAllTests() {
			requestMock = null;
			request = null;
		}
		
		@AfterEach
		public void unSetForEachTests() {
			registeredRepository.deleteAll();
		}
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test removeRegistered should throw UnexpectedRollbackException on ResourceNotFoundException")
		@Transactional
		public void removeRegisteredTestShouldRemoveRegisteredAndDeleteHim() {
			
			//GIVEN
			
			Registered registeredA = new Registered("aaa@aaa.com", "aaaPasswd", "Aaa", "AAA", LocalDate.parse("01/01/1991", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "aaaIban");
			Registered registeredB = new Registered("bbb@bbb.com", "bbbPasswd", "Bbb", "BBB", LocalDate.parse("02/02/1992", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "bbbIban");
			Registered registeredC = new Registered("ccc@ccc.com", "cccPasswd", "Ccc", "CCC", LocalDate.parse("03/03/1993", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "cccIban");

			registeredB.addRole(roleRepository.findById(1).get());
			registeredRepository.saveAndFlush(registeredA);
			registeredRepository.saveAndFlush(registeredB);
			registeredRepository.saveAndFlush(registeredC);
			
			Set<Registered> addConnectionsExpectedA = new HashSet<>();
			addConnectionsExpectedA.add(registeredC);

			Set<Registered> addedConnectionsExpectedC = new HashSet<>();
			addedConnectionsExpectedC.add(registeredA);

			// addConnections Expected C size should be 0

			registeredA.addConnection(registeredB);
			registeredA.addConnection(registeredC);
			registeredRepository.saveAndFlush(registeredA);

			registeredC.addConnection(registeredB);
			registeredRepository.saveAndFlush(registeredC);

			// WHEN
			registeredService.removeRegistered("bbb@bbb.com", request);
			
			// THEN
			Optional<Registered> registeredAResultOpt = registeredRepository.findById("aaa@aaa.com");
			Optional<Registered> registeredBResultOpt = registeredRepository.findById("bbb@bbb.com");
			Optional<Registered> registeredCResultOpt = registeredRepository.findById("ccc@ccc.com");

			assertThat(registeredAResultOpt).isNotEmpty();
			assertThat(registeredBResultOpt).isEmpty();
			assertThat(registeredCResultOpt).isNotEmpty();

			registeredAResultOpt.ifPresent(registeredAResult -> assertThat(registeredAResult.getAddConnections()).containsExactlyInAnyOrderElementsOf(addConnectionsExpectedA));
			registeredCResultOpt.ifPresent(registeredCResult -> assertThat(registeredCResult.getAddedConnections()).containsExactlyInAnyOrderElementsOf(addedConnectionsExpectedC));
			registeredCResultOpt.ifPresent(registeredCResult -> assertThat(registeredCResult.getAddConnections()).hasSize(0));
		}
	}
}
