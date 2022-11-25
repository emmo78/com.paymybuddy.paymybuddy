package com.paymybuddy.paymybuddy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.paymybuddy.paymybuddy.model.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

}
