package com.paymybuddy.paymybuddy.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "registered")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
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
	private LocalDate birthDate;

	@Column(name = "iban")
	private String iban;

	@Column(name = "balance")
	private double balance = 0;

	@ManyToMany(cascade = CascadeType.MERGE)
	@JoinTable(name = "connection", joinColumns = @JoinColumn(name = "email_added", referencedColumnName = "email", nullable = false), inverseJoinColumns = @JoinColumn(name = "email_add", referencedColumnName = "email", nullable = false))
	private Set<Registered> addConnections = new HashSet<>();
	
	@ManyToMany
	@JoinTable(name = "registered_role", joinColumns = @JoinColumn(name = "email_role", referencedColumnName = "email", nullable = false), inverseJoinColumns = @JoinColumn(name = "granted_role", referencedColumnName = "role_id", nullable = false))
	List<Role> roles = new ArrayList<>();
	
	public void addConnection(Registered registered) {
		addConnections.add(registered);
	}
	
	public void removeConnection(Registered registered) {
		addConnections.remove(registered);
	}
		
	public void addRole(Role role) {
		roles.add(role);
	}
}
