package com.paymybuddy.paymybuddy.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.paymybuddy.paymybuddy.model.Role;

@SpringBootTest
public class RoleRepositoryIT {

	@Autowired
	RoleRepository roleRepository;
	
	@Test
	@Tag("TransactionRepositoryIT")
	@DisplayName("finByIdIT argument 1 should return an optional of Role with name USER")
	@Transactional
	public void finByIdITArgument1ShouldReturnAnOptionalOfRoleWithNameUSER() {
		//GIVEN
		Role roleExpected = new Role();
		roleExpected.setRoleId(1);
		roleExpected.setRoleName("ROLE_USER");
		
		//WHEN
		Role roleResulted = roleRepository.findById(1).get();
		
		//THEN
		assertThat(roleResulted).usingRecursiveComparison().isEqualTo(roleExpected);		
	}
}
