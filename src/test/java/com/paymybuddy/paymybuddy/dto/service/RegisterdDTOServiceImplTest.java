package com.paymybuddy.paymybuddy.dto.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.paymybuddy.paymybuddy.dto.RegisteredDTO;
import com.paymybuddy.paymybuddy.dto.RegisteredForListDTO;
import com.paymybuddy.paymybuddy.model.Registered;

@SpringBootTest
class RegisterdDTOServiceImplTest {
	
	@Autowired
	private RegisteredDTOService registerdDTOService;
	
	@Test
	@DisplayName("test registeredToDTO should not map password")
	public void registeredToDTOTestShouldNotMapPassword() {
		//GIVEN
		Registered registered = new Registered("aaa@aaa.com", "aaaPasswd", "Aaa", "AAA", LocalDate.parse("21/01/1991", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "aaaIban");
		
		//WHEN
		RegisteredDTO registeredDTOResult = registerdDTOService.registeredToDTO(registered);
		
		//THEN
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
						"21/01/1991",
						"aaaIban",
						0.0);
	}
	
	@Test
	@DisplayName("test registeredToForListDTO should not map password")
	public void registeredToForListDTOTestShouldNotMapPassword() {
		//GIVEN
		Registered registered = new Registered("aaa@aaa.com", "aaaPasswd", "Aaa", "AAA", LocalDate.parse("21/01/1991", DateTimeFormatter.ofPattern("dd/MM/yyyy")), "aaaIban");
		
		//WHEN
		RegisteredForListDTO registeredForListDTOResult = registerdDTOService.registeredToForListDTO(registered);
		
		//THEN
		assertThat(registeredForListDTOResult).extracting(
				RegisteredForListDTO::getEmail,
				RegisteredForListDTO::getFirstName,
				RegisteredForListDTO::getLastName
				).containsExactly(
						"aaa@aaa.com",
						"Aaa",
						"AAA");
	}

	@Test
	@DisplayName("test registeredFromDTO should map password")
	public void registeredFromDTOTestShouldNotMapPassword() {
		//GIVEN
		RegisteredDTO registeredDTO = new RegisteredDTO();
		registeredDTO.setEmail("aaa@aaa.com");
		registeredDTO.setPassword("aaaPasswd");
		registeredDTO.setFirstName("Aaa");
		registeredDTO.setLastName("AAA");
		registeredDTO.setBirthDate("21/01/1991");
		registeredDTO.setIban("aaaIban");
		registeredDTO.setBalance(0.0);

		//WHEN
		Registered registeredResult = registerdDTOService.registeredFromDTO(registeredDTO);
		
		//THEN
		assertThat(registeredResult).extracting(
				Registered::getEmail,
				Registered::getPassword,
				Registered::getFirstName,
				Registered::getLastName,
				Registered::getBirthDate,
				Registered::getIban,
				Registered::getBalance).containsExactly(
						"aaa@aaa.com",
						"aaaPasswd",
						"Aaa",
						"AAA",
						LocalDate.parse("21/01/1991", DateTimeFormatter.ofPattern("dd/MM/yyyy")),
						"aaaIban",
						0.0);
	}

}
