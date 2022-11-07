package com.paymybuddy.paymybuddy.dto.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.paymybuddy.paymybuddy.configuration.DateTimePatternProperties;
import com.paymybuddy.paymybuddy.dto.RegisteredDTO;
import com.paymybuddy.paymybuddy.dto.RegisteredForListDTO;
import com.paymybuddy.paymybuddy.model.Registered;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class RegisterdDTOServiceImplTest {
	
	@Autowired
	@InjectMocks
	private RegisteredDTOServiceImpl registerdDTOService;
	
	@Mock
	private DateTimePatternProperties dateStringPattern;
	
	@Test
	@DisplayName("test registeredToDTO should not map password")
	public void registeredToDTOTestShouldNotMapPassword() {
		//GIVEN
		Registered registered = new Registered("aaa@aaa.com", "aaaPasswd", "Aaa", "AAA", LocalDate.parse("01/21/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")), "aaaIban");
		registered.setBalance(100);
		when(dateStringPattern.getDateStringPattern()).thenReturn("MM/dd/yyyy");
		when(dateStringPattern.getLocalLanguage()).thenReturn("en");
		
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
						"01/21/1991",
						"aaaIban",
						"100.00");
	}
	
	@Test
	@DisplayName("test registeredToForListDTO should not map password")
	public void registeredToForListDTOTestShouldNotMapPassword() {
		//GIVEN
		Registered registered = new Registered("aaa@aaa.com", "aaaPasswd", "Aaa", "AAA", LocalDate.parse("01/21/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")), "aaaIban");
		
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
		registeredDTO.setBirthDate("01/21/1991");
		registeredDTO.setIban("aaaIban");
		registeredDTO.setBalance("100.00");
		when(dateStringPattern.getDateStringPattern()).thenReturn("MM/dd/yyyy");

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
						LocalDate.parse("01/21/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")),
						"aaaIban",
						100d);
	}

}
