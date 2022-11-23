package com.paymybuddy.paymybuddy.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.transaction.Transactional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.paymybuddy.paymybuddy.model.Registered;

@SpringBootTest
class RegisteredRepositoryIT {

	@Autowired
	private RegisteredRepository registeredRepository;

	private Registered registeredA;
	private Registered registeredB;
	private Registered registeredC;
	private Registered registeredD;
	private Registered registeredE;

	@BeforeEach
	public void setUpPerTest() {
		registeredA = new Registered("aaa@aaa.com", "aaaPasswd", "Aaa", "AAA", LocalDate.parse("01/01/1991", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "aaaIban");
		registeredB = new Registered("bbb@bbb.com", "bbbPasswd", "Bbb", "BBB", LocalDate.parse("02/02/1992", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "bbbIban");
		registeredC = new Registered("ccc@ccc.com", "cccPasswd", "Ccc", "CCC", LocalDate.parse("03/03/1993", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "cccIban");
		registeredD = new Registered("ddd@ddd.com", "dddPasswd", "Ddd", "DDD", LocalDate.parse("04/04/1994", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "dddIban");
		registeredE = new Registered("eee@ddd.com", "eeePasswd", "Eee", "DDD", LocalDate.parse("05/05/1994", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "eeeIban");

		registeredRepository.saveAndFlush(registeredA);
		registeredRepository.saveAndFlush(registeredB);
		registeredRepository.saveAndFlush(registeredE); // No respect of Alphabetical order
		registeredRepository.saveAndFlush(registeredD); // To test Sort Ascending
		registeredRepository.saveAndFlush(registeredC); // by LastName then FirstName
	}

	@AfterEach
	public void undefPerTest() {
		registeredRepository.deleteAll();
		registeredA = null;
		registeredB = null;
		registeredC = null;
		registeredD = null;
		registeredE = null;
	}

	@Test
	@Tag("RegisteredRepositoryIT")
	@DisplayName("registeredA connects B and C should fill their sets")
	@Transactional
	public void registeredAconnectsBandCShouldFillTheirSets() {
		// GIVEN
		Set<Registered> addConnectionsExpectedA = new HashSet<>();
		addConnectionsExpectedA.add(registeredB);
		addConnectionsExpectedA.add(registeredC);

		// WHEN
		registeredA.addConnection(registeredB);
		registeredA.addConnection(registeredC);
		registeredRepository.saveAndFlush(registeredA);

		// THEN
		Optional<Registered> registeredAResultOpt = registeredRepository.findById("aaa@aaa.com");
		Optional<Registered> registeredBResultOpt = registeredRepository.findById("bbb@bbb.com");
		Optional<Registered> registeredCResultOpt = registeredRepository.findById("ccc@ccc.com");

		assertThat(registeredAResultOpt).isNotEmpty();
		assertThat(registeredBResultOpt).isNotEmpty();
		assertThat(registeredCResultOpt).isNotEmpty();

		registeredAResultOpt.ifPresent(registeredAResult -> assertThat(registeredAResult.getAddConnections()).containsExactlyInAnyOrderElementsOf(addConnectionsExpectedA));
	}

	@Test
	@Tag("RegisteredRepositoryIT")
	@DisplayName("registeredA disconnects B and C should empty their sets")
	@Transactional
	public void registeredADisconnectsBandCShouldEmptyTheirSets() {
		// GIVEN
		registeredA.addConnection(registeredB);
		registeredA.addConnection(registeredC);
		registeredRepository.saveAndFlush(registeredA);

		// WHEN
		registeredA.removeConnection(registeredB);
		registeredA.removeConnection(registeredC);
		registeredRepository.saveAndFlush(registeredA);

		
		// THEN
		Optional<Registered> registeredAResultOpt = registeredRepository.findById("aaa@aaa.com");
		Optional<Registered> registeredBResultOpt = registeredRepository.findById("bbb@bbb.com");
		Optional<Registered> registeredCResultOpt = registeredRepository.findById("ccc@ccc.com");

		assertThat(registeredAResultOpt).isNotEmpty();
		assertThat(registeredBResultOpt).isNotEmpty();
		assertThat(registeredCResultOpt).isNotEmpty();

		registeredAResultOpt.ifPresent(registeredAResult -> assertThat(registeredAResult.getAddConnections()).isEmpty());
	}
	
	@Test
	@Tag("RegisteredRepositoryIT")
	@DisplayName("test findAllAddByEmail should return expected pages")
	@Transactional
	public void testFindAllAddByEmailShouldReturnExpectedPages() {

		// GIVEN
		registeredA.addConnection(registeredD);
		registeredA.addConnection(registeredC);
		registeredA.addConnection(registeredE);
		registeredRepository.saveAndFlush(registeredA);
		registeredB.addConnection(registeredA);
		registeredRepository.saveAndFlush(registeredB);
		registeredC.addConnection(registeredA);
		registeredC.addConnection(registeredB);
		registeredC.addConnection(registeredD);
		registeredRepository.saveAndFlush(registeredC);
		registeredD.addConnection(registeredA);
		registeredRepository.saveAndFlush(registeredD);
		
		List<Registered> registerdAddByAExpected = new ArrayList<>();
		registerdAddByAExpected.add(registeredC);
		registerdAddByAExpected.add(registeredD);
		registerdAddByAExpected.add(registeredE);
		Pageable pageRequest = PageRequest.of(0, 5, Sort.by("last_name", "first_name").ascending());
		
		// WHERE
		Page<Registered> pageRegisteredResult = registeredRepository.findAllAddByEmail("aaa@aaa.com", pageRequest);
		
		// THEN
		assertThat(pageRegisteredResult.getContent()).containsExactlyElementsOf(registerdAddByAExpected);	
	}

	@Test
	@Tag("RegisteredRepositoryIT")
	@DisplayName("test findAllNotAddByEmail should return expected pages")
	@Transactional
	public void testFindAllNotAddByEmailShouldReturnExpectedPages() {

		// GIVEN
		registeredA.addConnection(registeredB);
		registeredA.addConnection(registeredC);
		registeredRepository.saveAndFlush(registeredA);
		registeredB.addConnection(registeredA);
		registeredRepository.saveAndFlush(registeredB);
		registeredC.addConnection(registeredB);
		registeredC.addConnection(registeredD);
		registeredRepository.saveAndFlush(registeredC);
		registeredD.addConnection(registeredA);
		registeredRepository.saveAndFlush(registeredD);
		List<Registered> registerdNotConnectedToAExpected = new ArrayList<>();
		registerdNotConnectedToAExpected.add(registeredD);
		registerdNotConnectedToAExpected.add(registeredE);
		Pageable pageRequest = PageRequest.of(0, 5, Sort.by("last_name", "first_name").ascending());

		// WHERE
		Page<Registered> pageRegisteredResult = registeredRepository.findAllNotAddByEmail("aaa@aaa.com", pageRequest);

		// THEN		
		assertThat(pageRegisteredResult.getContent()).containsExactlyElementsOf(registerdNotConnectedToAExpected);
	}
	
	@Test
	@Tag("RegisteredRepositoryIT")
	@DisplayName("test findAllAddedToEmail should return expected pages")
	@Transactional
	public void testFindAllAddedToEmailShouldReturnExpectedPages() {

		// GIVEN
		registeredA.addConnection(registeredE);
		registeredA.addConnection(registeredD);
		registeredA.addConnection(registeredC);
		registeredRepository.saveAndFlush(registeredA);
		registeredD.addConnection(registeredA);
		registeredRepository.saveAndFlush(registeredD);
		registeredB.addConnection(registeredA);
		registeredRepository.saveAndFlush(registeredB);
		registeredC.addConnection(registeredA);
		registeredC.addConnection(registeredB);
		registeredC.addConnection(registeredD);
		registeredRepository.saveAndFlush(registeredC);
		
		List<Registered> registerdAddedToAExpected = new ArrayList<>();
		registerdAddedToAExpected.add(registeredB);
		registerdAddedToAExpected.add(registeredC);
		registerdAddedToAExpected.add(registeredD);
		Pageable pageRequest = PageRequest.of(0, 5, Sort.by("last_name", "first_name").ascending());
		
		// WHERE
		Page<Registered> pageRegisteredResult = registeredRepository.findAllAddedToEmail("aaa@aaa.com", pageRequest);
		
		// THEN
		assertThat(pageRegisteredResult).containsExactlyElementsOf(pageRegisteredResult);
	}

	@Test
	@Tag("RegisteredRepositoryIT")
	@DisplayName("registeredB after removing add and added should clean his FK in connection")
	@Transactional
	public void registeredBAfterRemovingAddAndAddedFromApplicationShouldCleanHisFKInConnection() {
		// GIVEN
		registeredA.addConnection(registeredB);
		registeredA.addConnection(registeredC); // A added to C
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

		// WHEN
		registeredB.removeConnection(registeredA);
		registeredB.removeConnection(registeredC);
		registeredRepository.saveAndFlush(registeredB);
		registeredRepository.findAllAddedToEmail("bbb@bbb.com", Pageable.unpaged())
			.forEach(added -> registeredRepository.findById(added.getEmail()).get()
			.removeConnection(registeredB));
		registeredRepository.flush();

		// THEN
		assertThat(registeredRepository.findById("aaa@aaa.com")).isNotEmpty();
		assertThat(registeredRepository.findById("bbb@bbb.com")).isNotEmpty();
		assertThat(registeredRepository.findById("ccc@ccc.com")).isNotEmpty();

		assertThat(registeredRepository.findAllAddByEmail("aaa@aaa.com", Pageable.unpaged())).containsExactlyInAnyOrderElementsOf(addConnectionsExpectedA);
		assertThat(registeredRepository.findAllAddByEmail("bbb@bbb.com", Pageable.unpaged())).isEmpty();
		assertThat(registeredRepository.findAllAddByEmail("ccc@ccc.com", Pageable.unpaged())).isEmpty();
		assertThat(registeredRepository.findAllAddedToEmail("aaa@aaa.com", Pageable.unpaged())).isEmpty();
		assertThat(registeredRepository.findAllAddedToEmail("bbb@bbb.com", Pageable.unpaged())).isEmpty();
		assertThat(registeredRepository.findAllAddedToEmail("ccc@ccc.com", Pageable.unpaged())).containsExactlyInAnyOrderElementsOf(addedConnectionsExpectedC);
	}

	@Test
	@Tag("RegisteredRepositoryIT")
	@DisplayName("registeredB after removing added and himself from application should clean his FK in connection")
	@Transactional
	public void registeredBAfterRemovingAddedAndHimselfFromApplicationShouldCleanHisFKInConnection() {
		// GIVEN
		Set<Registered> addConnectionsExpectedA = new HashSet<>();
		addConnectionsExpectedA.add(registeredC);

		// addConnections Expected C size should be 0
		
		Set<Registered> addedConnectionsExpectedC = new HashSet<>();
		addedConnectionsExpectedC.add(registeredA);
		
		// addedConnections Expected A size should be 0

		registeredA.addConnection(registeredB);
		registeredA.addConnection(registeredC); // A added to C
		registeredRepository.saveAndFlush(registeredA);
		
		registeredB.addConnection(registeredA);
		registeredB.addConnection(registeredC);
		registeredRepository.saveAndFlush(registeredB);

		registeredC.addConnection(registeredB);
		registeredRepository.saveAndFlush(registeredC);

		// WHEN
		registeredRepository.findAllAddedToEmail("bbb@bbb.com", Pageable.unpaged())
			.forEach(added -> registeredRepository.findById(added.getEmail()).get()
			.removeConnection(registeredB));
		registeredRepository.deleteById("bbb@bbb.com");
		registeredRepository.flush();

		// THEN
		assertThat(registeredRepository.findById("aaa@aaa.com")).isNotEmpty();
		assertThat(registeredRepository.findById("bbb@bbb.com")).isEmpty();
		assertThat(registeredRepository.findById("ccc@ccc.com")).isNotEmpty();

		assertThat(registeredRepository.findAllAddByEmail("aaa@aaa.com", Pageable.unpaged())).containsExactlyInAnyOrderElementsOf(addConnectionsExpectedA);
		assertThat(registeredRepository.findAllAddByEmail("ccc@ccc.com", Pageable.unpaged())).isEmpty();
		assertThat(registeredRepository.findAllAddedToEmail("aaa@aaa.com", Pageable.unpaged())).isEmpty();
		assertThat(registeredRepository.findAllAddedToEmail("ccc@ccc.com", Pageable.unpaged())).containsExactlyInAnyOrderElementsOf(addedConnectionsExpectedC);
	}
}
