package com.paymybuddy.paymybuddy.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "registered")
@Getter
@Setter
public class Registered implements Serializable {
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
	
	@OneToMany(
			mappedBy="sender",
			cascade=CascadeType.REFRESH)
	private List<Transaction> sendedTransactions = new ArrayList<>();

	@OneToMany(
			mappedBy="receiver",
			cascade=CascadeType.REFRESH)
	private List<Transaction> receivedTransactions = new ArrayList<>();
	
	public void addSendedTransaction(Transaction transaction) {
		sendedTransactions.add(transaction);
		transaction.setSender(this);
	}
	
	public void addReceivedTransaction(Transaction transaction) {
		receivedTransactions.add(transaction);
		transaction.setReceiver(this);
	}

}
