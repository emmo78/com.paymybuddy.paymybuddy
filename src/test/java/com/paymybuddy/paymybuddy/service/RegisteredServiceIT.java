package com.paymybuddy.paymybuddy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import org.springframework.data.domain.Pageable;
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
import com.paymybuddy.paymybuddy.model.Transaction;
import com.paymybuddy.paymybuddy.repository.RegisteredRepository;
import com.paymybuddy.paymybuddy.repository.RoleRepository;
import com.paymybuddy.paymybuddy.repository.TransactionRepository;

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
			Registered registeredToUpate = new Registered();
			registeredToUpate.setEmail("aaa@aaa.com");
			registeredToUpate.setPassword(passwordEncoder.encode("aaaPasswd"));
			registeredToUpate.setFirstName("Aaa");
			registeredToUpate.setLastName("AAA");
			registeredToUpate.setBirthDate(LocalDate.parse("01/21/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
			registeredToUpate.setIban(null);
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
				//No Registered to Update => Not Found
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
		private RoleRepository roleRepository;
		
		@Autowired
		private TransactionRepository transactionRepository;
		
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
		@DisplayName("test removeRegistered should throw remove registered and delete him")
		public void removeRegisteredTestShouldRemoveRegisteredAndDeleteHim() {
			
			//GIVEN
			
			Registered registeredA = new Registered();
			registeredA.setEmail("aaa@aaa.com");
			registeredA.setPassword("aaaPasswd");
			registeredA.setFirstName("Aaa");
			registeredA.setLastName("AAA");
			registeredA.setBirthDate(LocalDate.parse("01/21/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
			registeredA.setIban("aaaIban");
			registeredA.setBalance(100);

			Registered registeredB = new Registered();
			registeredB.setEmail("bbb@bbb.com");
			registeredB.setPassword("bbbPasswd");
			registeredB.setFirstName("Bbb");
			registeredB.setLastName("BBB");
			registeredB.setBirthDate(LocalDate.parse("02/22/1992", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
			registeredB.setIban("bbbIban");
			registeredB.setBalance(200);

			Registered registeredC = new Registered();
			registeredC.setEmail("ccc@ccc.com");
			registeredC.setPassword("cccPasswd");
			registeredC.setFirstName("Ccc");
			registeredC.setLastName("CCC");
			registeredC.setBirthDate(LocalDate.parse("03/23/1993", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
			registeredC.setIban("cccIban");
			registeredC.setBalance(300);
			
			registeredB.addRole(roleRepository.findById(1).get());
			
			registeredA.addConnection(registeredB);
			registeredA.addConnection(registeredC);
			registeredRepository.saveAndFlush(registeredA);
			
			registeredB.addConnection(registeredA);
			registeredB.addConnection(registeredC);
			registeredRepository.saveAndFlush(registeredB);

			registeredC.addConnection(registeredB);
			registeredRepository.saveAndFlush(registeredC);

			Set<Registered> addConnectionsExpectedA = new HashSet<>();
			addConnectionsExpectedA.add(registeredC);

			Set<Registered> addedConnectionsExpectedC = new HashSet<>();
			addedConnectionsExpectedC.add(registeredA);

			// addConnections Expected C size should be 0
			// addedConnections Expected A size should be 0
			
			Transaction transactionAtoB = new Transaction();
			transactionAtoB.setDateTime(LocalDateTime.now());
			transactionAtoB.setAmount(100);
			transactionAtoB.monetize();
			transactionAtoB = transactionRepository.saveAndFlush(transactionAtoB);
			transactionAtoB.setSender(registeredA);
			transactionAtoB.setReceiver(registeredB);
			transactionAtoB = transactionRepository.saveAndFlush(transactionAtoB);
			
			Transaction transactionAtoC = new Transaction();
			transactionAtoC.setDateTime(LocalDateTime.now());
			transactionAtoC.setAmount(200);
			transactionAtoC.monetize();
			transactionAtoC = transactionRepository.saveAndFlush(transactionAtoC);
			transactionAtoC.setSender(registeredA);
			transactionAtoC.setReceiver(registeredC);
			transactionAtoC = transactionRepository.saveAndFlush(transactionAtoC);
			
			Transaction transactionBtoA = new Transaction();
			transactionBtoA.setDateTime(LocalDateTime.now());
			transactionBtoA.setAmount(300);
			transactionBtoA.monetize();
			transactionBtoA = transactionRepository.saveAndFlush(transactionBtoA);
			transactionBtoA.setSender(registeredB);
			transactionBtoA.setReceiver(registeredA);
			transactionBtoA = transactionRepository.saveAndFlush(transactionBtoA);
			
			Transaction transactionBtoC = new Transaction();
			transactionBtoC.setDateTime(LocalDateTime.now());
			transactionBtoC.setAmount(400);
			transactionBtoC.monetize();
			transactionBtoC = transactionRepository.saveAndFlush(transactionBtoC);
			transactionBtoC.setSender(registeredB);
			transactionBtoC.setReceiver(registeredC);
			transactionBtoC = transactionRepository.saveAndFlush(transactionBtoC);
			
			Transaction transactionCtoB = new Transaction();
			transactionCtoB.setDateTime(LocalDateTime.now());
			transactionCtoB.setAmount(500);
			transactionCtoB.monetize();
			transactionCtoB = transactionRepository.saveAndFlush(transactionCtoB);
			transactionCtoB.setSender(registeredC);
			transactionCtoB.setReceiver(registeredB);
			transactionCtoB = transactionRepository.saveAndFlush(transactionCtoB);

			// WHEN
			registeredService.removeRegistered("bbb@bbb.com", request);
			
			// THEN
			assertThat(registeredRepository.findById("aaa@aaa.com")).isNotEmpty();
			assertThat(registeredRepository.findById("bbb@bbb.com")).isEmpty();
			assertThat(registeredRepository.findById("ccc@ccc.com")).isNotEmpty();

			assertThat(registeredRepository.findAllAddByEmail("aaa@aaa.com", Pageable.unpaged())).containsExactlyInAnyOrderElementsOf(addConnectionsExpectedA);
			assertThat(registeredRepository.findAllAddByEmail("ccc@ccc.com", Pageable.unpaged())).isEmpty();
			assertThat(registeredRepository.findAllAddedToEmail("aaa@aaa.com", Pageable.unpaged())).isEmpty();
			assertThat(registeredRepository.findAllAddedToEmail("ccc@ccc.com", Pageable.unpaged())).containsExactlyInAnyOrderElementsOf(addedConnectionsExpectedC);
			assertThat(transactionRepository.findAllTransactionsByEmailSenderOrReceiver("aaa@aaa.com", Pageable.unpaged()))
				.extracting(
						Transaction::getAmount,
						t -> Optional.ofNullable(t.getSender()).orElse(new Registered()).getEmail(),
						t -> Optional.ofNullable(t.getReceiver()).orElse(new Registered()).getEmail())
				.containsExactlyInAnyOrder(
						tuple(100.0, "aaa@aaa.com", null),
						tuple(200.0, "aaa@aaa.com", "ccc@ccc.com"),
						tuple(300.0, null, "aaa@aaa.com"));
			assertThat(transactionRepository.findAllTransactionsByEmailSenderOrReceiver("bbb@bbb.com", Pageable.unpaged())).isEmpty();
			assertThat(transactionRepository.findAllTransactionsByEmailSenderOrReceiver("ccc@ccc.com", Pageable.unpaged()))
				.extracting(
						Transaction::getAmount,
						t -> Optional.ofNullable(t.getSender()).orElse(new Registered()).getEmail(),
						t -> Optional.ofNullable(t.getReceiver()).orElse(new Registered()).getEmail())
				.containsExactlyInAnyOrder(
						tuple(200.0, "aaa@aaa.com", "ccc@ccc.com"),
						tuple(400.0, null, "ccc@ccc.com"),
						tuple(500.0, "ccc@ccc.com", null));
		}
	}
}
