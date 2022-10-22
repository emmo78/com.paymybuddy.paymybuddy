package com.paymybuddy.paymybuddy.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "registered")
@Getter
@Setter
public class Registered implements Serializable{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "email")
	private String email;
	
	@Column(name = "password")
	private String password;
	
	@Column(name = "first_name")
	private String firstName;
	
	@Column(name = "last_name")
	private String lastName;
	
	@Column(name = "birth_date")
	private Date birthDate;
	
	@Column(name = "iban")
	private String iban;
	
	@Column(name = "balance")
	private double balance;
}
