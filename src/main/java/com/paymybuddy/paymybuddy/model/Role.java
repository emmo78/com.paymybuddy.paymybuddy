package com.paymybuddy.paymybuddy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "role")
@Getter
@Setter
@NoArgsConstructor
public class Role {
	
	@Id
	@Column(name = "role_id")
	int roleId;
	
	@Column(name = "role_name")
	private String roleName;
}
