package com.paymybuddy.paymybuddy.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

	Registered registeredA;
	Registered registeredB;
	Registered registeredC;
	Registered registeredD;
	Registered registeredE;

	@BeforeEach
	public void setUpPerTest() {
		registeredA = new Registered("aaa@aaa.com", "aaaPasswd", "Aaa", "AAA", LocalDate.parse("01/01/1991", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "aaaIban");
		registeredB = new Registered("bbb@bbb.com", "bbbPasswd", "Bbb", "BBB", LocalDate.parse("02/02/1992", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "bbbIban");
		registeredC = new Registered("ccc@ccc.com", "cccPasswd", "Ccc", "CCC", LocalDate.parse("03/03/1993", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "cccIban");
		registeredD = new Registered("ddd@ddd.com", "dddPasswd", "Ddd", "DDD", LocalDate.parse("04/04/1994", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "dddIban");
		registeredE = new Registered("eee@ddd.com", "eeePasswd", "Eee", "DDD", LocalDate.parse("05/05/1994", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "eeeIban");

		registeredRepository.save(registeredA);
		registeredRepository.save(registeredB);
		registeredRepository.save(registeredE); // No respect of Alphabetical order
		registeredRepository.save(registeredD); // To test Sort Ascending
		registeredRepository.save(registeredC); // by LastName then FirstName
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

		Set<Registered> addedConnectionsExpectedB = new HashSet<>();
		addedConnectionsExpectedB.add(registeredA);

		Set<Registered> addedConnectionsExpectedC = new HashSet<>();
		addedConnectionsExpectedC.add(registeredA);

		// WHEN
		registeredA.addConnection(registeredB);
		registeredA.addConnection(registeredC);
		registeredRepository.save(registeredA);

		// THEN
		Optional<Registered> registeredAResultOpt = registeredRepository.findById("aaa@aaa.com");
		Optional<Registered> registeredBResultOpt = registeredRepository.findById("bbb@bbb.com");
		Optional<Registered> registeredCResultOpt = registeredRepository.findById("ccc@ccc.com");

		assertThat(registeredAResultOpt).isNotEmpty();
		assertThat(registeredBResultOpt).isNotEmpty();
		assertThat(registeredCResultOpt).isNotEmpty();

		registeredAResultOpt.ifPresent(registeredAResult -> assertThat(registeredAResult.getAddConnections()).containsExactlyInAnyOrderElementsOf(addConnectionsExpectedA));
		registeredBResultOpt.ifPresent(registeredBResult -> assertThat(registeredBResult.getAddedConnections()).containsExactlyInAnyOrderElementsOf(addedConnectionsExpectedB));
		registeredCResultOpt.ifPresent(registeredCResult -> assertThat(registeredCResult.getAddedConnections()).containsExactlyInAnyOrderElementsOf(addedConnectionsExpectedC));
	}

	@Test
	@Tag("RegisteredRepositoryIT")
	@DisplayName("registeredB disconnects added A and C should remove them from their sets")
	@Transactional
	public void registeredBDisconnectsAddedAandCShouldRemoveThemFromTheirSets() {
		// GIVEN
		Set<Registered> addConnectionsExpectedA = new HashSet<>();
		addConnectionsExpectedA.add(registeredC);

		Set<Registered> addedConnectionsExpectedC = new HashSet<>();
		addedConnectionsExpectedC.add(registeredA);

		// addedConnections Expected B size should be 0
		// addConnections Expected C size should be 0

		registeredA.addConnection(registeredB);
		registeredA.addConnection(registeredC);
		registeredRepository.save(registeredA);

		registeredC.addConnection(registeredB);
		registeredRepository.save(registeredC);

		// WHEN
		registeredB = registeredRepository.findById("bbb@bbb.com").get();
		// if the set is modified at any time after the iterator is created, in any way
		// except through the iterator's own remove method, the Iterator throws a
		// ConcurrentModificationException.
		Set<Registered> addedToB = registeredB.getAddedConnections().stream().collect(Collectors.toSet());
		addedToB.forEach(added -> added.removeConnection(registeredB));
		registeredRepository.save(registeredB);

		// THEN
		Optional<Registered> registeredAResultOpt = registeredRepository.findById("aaa@aaa.com");
		Optional<Registered> registeredBResultOpt = registeredRepository.findById("bbb@bbb.com");
		Optional<Registered> registeredCResultOpt = registeredRepository.findById("ccc@ccc.com");

		assertThat(registeredAResultOpt).isNotEmpty();
		assertThat(registeredBResultOpt).isNotEmpty();
		assertThat(registeredCResultOpt).isNotEmpty();

		registeredAResultOpt.ifPresent(registeredAResult -> assertThat(registeredAResult.getAddConnections()).containsExactlyInAnyOrderElementsOf(addConnectionsExpectedA));
		registeredCResultOpt.ifPresent(registeredCResult -> assertThat(registeredCResult.getAddedConnections()).containsExactlyInAnyOrderElementsOf(addedConnectionsExpectedC));
		registeredBResultOpt.ifPresent(registeredBResult -> assertThat(registeredBResult.getAddedConnections()).hasSize(0));
		registeredCResultOpt.ifPresent(registeredCResult -> assertThat(registeredCResult.getAddConnections()).hasSize(0));
	}

	@Test
	@Tag("RegisteredRepositoryIT")
	@DisplayName("registeredB remove from application removing added A and C should remove them from their sets")
	@Transactional
	public void registeredBRemoveFromApplicationRemovingAddedAandCshouldRemoveThemFromTheirSets() {
		// GIVEN
		Set<Registered> addConnectionsExpectedA = new HashSet<>();
		addConnectionsExpectedA.add(registeredC);

		Set<Registered> addedConnectionsExpectedC = new HashSet<>();
		addedConnectionsExpectedC.add(registeredA);

		// addedConnections Expected B size should be 0
		// addConnections Expected C size should be 0

		registeredA.addConnection(registeredB);
		registeredA.addConnection(registeredC);
		registeredRepository.save(registeredA);

		registeredC.addConnection(registeredB);
		registeredRepository.save(registeredC);

		// WHEN
		registeredB = registeredRepository.findById("bbb@bbb.com").get();
		// if the set is modified at any time after the iterator is created, in any way
		// except through the iterator's own remove method, the Iterator throws a
		// ConcurrentModificationException.
		Set<Registered> addedToB = registeredB.getAddedConnections().stream().collect(Collectors.toSet());
		addedToB.forEach(added -> added.removeConnection(registeredB));
		registeredRepository.deleteById("bbb@bbb.com");

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

	@Test
	@Tag("RegisteredRepositoryIT")
	@DisplayName("test findAllConnectedToEmail should return expected pages")
	@Transactional
	public void testFindAllConnectedToEmailShouldReturnExpectedPages() {

		// GIVEN
		registeredA.addConnection(registeredE);
		registeredA.addConnection(registeredD);
		registeredA.addConnection(registeredC);
		registeredRepository.save(registeredA);
		registeredC.addConnection(registeredA);
		registeredC.addConnection(registeredB);
		registeredC.addConnection(registeredD);
		registeredRepository.save(registeredC);
		registeredD.addConnection(registeredA);
		registeredRepository.save(registeredD);
		
		List<Registered> registerdConnectedToAExpected = new ArrayList<>();
		registerdConnectedToAExpected.add(registeredC);
		registerdConnectedToAExpected.add(registeredD);
		registerdConnectedToAExpected.add(registeredE);
		Pageable pageRequest = PageRequest.of(0, 5, Sort.by("last_name", "first_name").ascending());
		
		// WHERE
		Page<Registered> pageRegisteredResult = registeredRepository.findAllConnectedToEmail("aaa@aaa.com", pageRequest);
		
		// THEN
		assertThat(pageRegisteredResult.getContent()).containsExactlyElementsOf(registerdConnectedToAExpected);	

	}

	@Test
	@Tag("RegisteredRepositoryIT")
	@DisplayName("test findAllNotConnectedToEmail should return expected pages")
	@Transactional
	public void testFindAllNotConnectedToEmailShouldReturnExpectedPages() {

		// GIVEN
		registeredA.addConnection(registeredB);
		registeredA.addConnection(registeredC);
		registeredRepository.save(registeredA);
		registeredC.addConnection(registeredB);
		registeredC.addConnection(registeredD);
		registeredRepository.save(registeredC);
		registeredD.addConnection(registeredA);
		registeredRepository.save(registeredD);
		List<Registered> registerdNotConnectedToAExpected = new ArrayList<>();
		registerdNotConnectedToAExpected.add(registeredD);
		registerdNotConnectedToAExpected.add(registeredE);
		Pageable pageRequest = PageRequest.of(0, 5, Sort.by("last_name", "first_name").ascending());

		// WHERE
		Page<Registered> pageRegisteredResult = registeredRepository.findAllNotConnectedToEmail("aaa@aaa.com", pageRequest);

		// THEN		
		assertThat(pageRegisteredResult.getContent()).containsExactlyElementsOf(registerdNotConnectedToAExpected);
	}

}
