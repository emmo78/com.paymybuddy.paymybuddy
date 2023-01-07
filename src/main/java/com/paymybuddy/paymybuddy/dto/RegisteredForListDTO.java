package com.paymybuddy.paymybuddy.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor //For Test
public class RegisteredForListDTO {
	@EqualsAndHashCode.Include
	private String email;
	private String firstName;
	private String lastName;
}
