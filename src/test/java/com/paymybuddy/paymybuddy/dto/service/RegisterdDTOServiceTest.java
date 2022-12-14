package com.paymybuddy.paymybuddy.dto.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.paymybuddy.paymybuddy.configuration.DateTimePatternProperties;
import com.paymybuddy.paymybuddy.dto.RegisteredDTO;
import com.paymybuddy.paymybuddy.dto.RegisteredForListDTO;
import com.paymybuddy.paymybuddy.model.Registered;

@ExtendWith(MockitoExtension.class)
class RegisterdDTOServiceTest {
	
	@InjectMocks
	private RegisteredDTOServiceImpl registerdDTOService;
	
	@Mock
	private DateTimePatternProperties dateStringPattern;
	
	@Spy
	ModelMapper modelMapper = new ModelMapper();
	
	@Spy
	PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	
	@Test
	@Tag("RegisterdDTOServiceTest")
	@DisplayName("test registeredToDTO should not map password")
	public void registeredToDTOTestShouldNotMapPassword() {
		//GIVEN
		Registered registered = new Registered();
		registered.setEmail("aaa@aaa.com");
		registered.setPassword("aaaPasswd");
		registered.setFirstName("Aaa");
		registered.setLastName("AAA");
		registered.setBirthDate(LocalDate.parse("01/21/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
		registered.setIban("aaaIban");
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
	@Tag("RegisterdDTOServiceTest")
	@DisplayName("test registeredToForListDTO should not map password")
	public void registeredToForListDTOTestShouldNotMapPassword() {
		//GIVEN
		Registered registered = new Registered();
		registered.setEmail("aaa@aaa.com");
		registered.setPassword("aaaPasswd");
		registered.setFirstName("Aaa");
		registered.setLastName("AAA");
		registered.setBirthDate(LocalDate.parse("01/21/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
		registered.setIban("aaaIban");
		registered.setBalance(100);
		
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
	@Tag("RegisterdDTOServiceTest")
	@DisplayName("test registeredFromDTO should encode password and adjust date")
	public void registeredFromDTOTestShouldNotEncodePasswordAndAdjustDate() {
		//GIVEN
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		RegisteredDTO registeredDTO = new RegisteredDTO();
		registeredDTO.setEmail("Aaa@Aaa.com");
		registeredDTO.setPassword("aaaPasswd");
		registeredDTO.setFirstName("Aaa");
		registeredDTO.setLastName("AAA");
		registeredDTO.setBirthDate("02/29/1991"); //1991 is not a leap year : 02/28/1991 expected
		registeredDTO.setIban(null);
		registeredDTO.setBalance(null);
		when(dateStringPattern.getDateStringPattern()).thenReturn("MM/dd/yyyy");

		//WHEN
		Registered registeredResult = registerdDTOService.registeredFromDTO(registeredDTO);
		
		//THEN
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
		assertThat(passwordEncoder.matches("aaaPasswd", registeredResult.getPassword())).isTrue();
	}
}
