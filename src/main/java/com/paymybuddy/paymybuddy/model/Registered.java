package com.paymybuddy.paymybuddy.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "registered")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Registered implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "email")
	@EqualsAndHashCode.Include
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
	private double balance = 0;

	@ManyToMany(cascade = CascadeType.MERGE)
	@JoinTable(name = "connection", joinColumns = @JoinColumn(name = "email_add", referencedColumnName = "email", nullable = false), inverseJoinColumns = @JoinColumn(name = "email_added", referencedColumnName = "email", nullable = false))
	private Set<Registered> addConnections = new HashSet<>();
	
	@ManyToMany(cascade = CascadeType.MERGE, mappedBy ="addConnections")
	private Set<Registered> addedConnections = new HashSet<>();		
	
	@OneToMany(mappedBy = "sender", cascade = CascadeType.REFRESH)
	private List<Transaction> sendedTransactions = new ArrayList<>();

	@OneToMany(mappedBy = "receiver", cascade = CascadeType.REFRESH)
	private List<Transaction> receivedTransactions = new ArrayList<>();
	
	public Registered() {
		super();
	}

	public Registered(String email, String password, String firstName, String lastName, Date birthDate, String iban) {
		super();
		this.email = email;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
		this.birthDate = birthDate;
		this.iban = iban;
	}

	public void addConnection(Registered registered) {
		addConnections.add(registered);
		registered.getAddedConnections().add(this);
	}
	
	public void removeConnection(Registered registered) {
		addConnections.remove(registered);
		registered.getAddedConnections().remove(this);
	}
	
	public void addSendedTransaction(Transaction transaction) {
		sendedTransactions.add(transaction);
		transaction.setSender(this);
	}

	public void addReceivedTransaction(Transaction transaction) {
		receivedTransactions.add(transaction);
		transaction.setReceiver(this);
	}

}
