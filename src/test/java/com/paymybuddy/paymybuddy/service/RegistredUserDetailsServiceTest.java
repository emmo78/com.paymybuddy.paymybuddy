package com.paymybuddy.paymybuddy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.UnexpectedRollbackException;

import com.paymybuddy.paymybuddy.exception.ResourceNotFoundException;
import com.paymybuddy.paymybuddy.model.Registered;
import com.paymybuddy.paymybuddy.model.Role;
import com.paymybuddy.paymybuddy.repository.RegisteredRepository;

@ExtendWith(MockitoExtension.class)
public class RegistredUserDetailsServiceTest {

	@InjectMocks
	RegistredUserDetailsService registredUserDetailsService;
	
	@Mock
	private RegisteredRepository registeredRepository;
	
	@Test
	@Tag("RegistredUserDetailsServiceTest")
	@DisplayName("test loadUserByUsername should return the expected User")
	public void loadUserByUsernameTestShouldReturnTheExpectedUser() {
		//GIVEN
		Role role = new Role();
		role.setRoleId(1);
		role.setRoleName("ROLE_USER");
		Registered registered = new Registered();
		registered.setEmail("aaa@aaa.com");
		registered.setPassword("aaaPasswd");
		registered.setFirstName("Aaa");
		registered.setLastName("AAA");
		registered.setBirthDate(LocalDate.parse("01/21/1991", DateTimeFormatter.ofPattern("MM/dd/yyyy")));
		registered.setIban("aaaIban");
		registered.setBalance(100);
		registered.setRoles(Arrays.asList(role));
		when(registeredRepository.findById(anyString())).thenReturn(Optional.of(registered));
		
		//WHEN
		UserDetails user = registredUserDetailsService.loadUserByUsername("aaa@aaa.com");
		
		//THEN
		assertThat(user).extracting(
				u -> u.getAuthorities().stream().collect(Collectors.toList()).get(0).getAuthority(),
				UserDetails::getUsername,
				UserDetails::getPassword).containsExactly(
						"ROLE_USER",
						"aaa@aaa.com",
						"aaaPasswd");
	}
	
	@Test
	@Tag("RegistredUserDetailsServiceTest")
	@DisplayName("test loadUserByUsername should throw ResourceNotFoundException")
	public void loadUserByUsernameTestShouldThrowsResourceNotFoundException() {
		//GIVEN
		when(registeredRepository.findById(anyString())).thenReturn(Optional.ofNullable(null));
		//WHEN
		//THEN
		assertThat(assertThrows(ResourceNotFoundException.class,
				() -> registredUserDetailsService.loadUserByUsername("aaa@aaa.com"))
				.getMessage()).isEqualTo("Registered aaa@aaa.com not found");
	}
	
	@Test
	@Tag("RegisteredServiceTest")
	@DisplayName("test loadUserByUsername should throw UnexpectedRollbackException on IllegalArgumentException")
	public void loadUserByUsernameTestShouldThrowsUnexpectedRollbackExceptionOnIllegalArgumentException() {
		//GIVEN
		when(registeredRepository.findById(anyString())).thenThrow(new IllegalArgumentException());
		
		//WHEN
		//THEN
		assertThat(assertThrows(UnexpectedRollbackException.class,
			() -> registredUserDetailsService.loadUserByUsername("aaa@aaa.com"))
			.getMessage()).isEqualTo("Error while getting your profile");
	}
	
	@Test
	@Tag("RegisteredServiceTest")
	@DisplayName("test loadUserByUsername should throw UnexpectedRollbackException on any RuntimeException")
	public void loadUserByUsernameTestShouldThrowsUnexpectedRollbackExceptionOnAnyRuntimeException() {
		//GIVEN
		when(registeredRepository.findById(anyString())).thenThrow(new RuntimeException());
		
		//WHEN
		//THEN
		assertThat(assertThrows(UnexpectedRollbackException.class,
			() -> registredUserDetailsService.loadUserByUsername("aaa@aaa.com"))
			.getMessage()).isEqualTo("Error while getting your profile");
	}
}
