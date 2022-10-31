package com.paymybuddy.paymybuddy.dto;

import java.sql.Date;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class RegisteredDTO {
	@EqualsAndHashCode.Include
	private String email;
	private String password; //No mapping from Registered only to Registered
	private String firstName;
	private String lastName;
	//@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date birthDate;
	private String iban;
	private double balance;
}
