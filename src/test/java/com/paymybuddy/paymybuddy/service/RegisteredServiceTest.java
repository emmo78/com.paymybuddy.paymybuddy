package com.paymybuddy.paymybuddy.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.WebRequest;

import com.paymybuddy.paymybuddy.configuration.DateTimePatternProperties;
import com.paymybuddy.paymybuddy.dto.RegisteredDTO;
import com.paymybuddy.paymybuddy.dto.service.RegisteredDTOService;
import com.paymybuddy.paymybuddy.dto.service.RegisteredDTOServiceImpl;
import com.paymybuddy.paymybuddy.exception.ResourceConflictException;
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
	
	@Mock
	private RequestService requestService;
	
	@Mock
	private DateTimePatternProperties dateStringPattern;
	
	private WebRequest request = null;
	
	@Nested
	@Tag("createRegisteredTests")
	@DisplayName("Tests for method createRegistered")
	class CreateRegisteredTests {
		
		@Test
		@Tag("RegisteredServiceTest")
		@DisplayName("test createRegistered should throw ResourceConflictException")
		public void createRegisteredTestShouldThrowResourceConflictException() {
			//GIVEN
			RegisteredDTO registeredDTO = new RegisteredDTO();
			registeredDTO.setEmail("Aaa@Aaa.com");
			registeredDTO.setPassword("aaaPasswd");
			registeredDTO.setFirstName("Aaa");
			registeredDTO.setLastName("AAA");
			registeredDTO.setBirthDate("01/21/1991");
			registeredDTO.setIban("aaaIban");
			registeredDTO.setBalance("100.00");

			Registered registered = new Registered("aaa@aaa.com", "aaaPasswd", "Aaa", "AAA", LocalDate.parse("01/21/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")), "aaaIban");
			when(registeredDTOService.registeredFromDTO(any(RegisteredDTO.class))).thenReturn(registered);
			when(registeredRepository.existsById(any(String.class))).thenReturn(true);
			
			//WHEN
			//THEN
			assertThrows(ResourceConflictException.class,() -> registeredService.createRegistered(registeredDTO, request));
		}
	}
}
