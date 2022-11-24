package com.paymybuddy.paymybuddy.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Transactional;

import com.paymybuddy.paymybuddy.exception.ResourceNotFoundException;
import com.paymybuddy.paymybuddy.model.Registered;
import com.paymybuddy.paymybuddy.repository.RegisteredRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RegistredUserDetailsService implements UserDetailsService {

	@Autowired
	private RegisteredRepository registeredRepository;
	
	@Override
	@Transactional(readOnly = true, rollbackFor = {ResourceNotFoundException.class, UnexpectedRollbackException.class})
	public UserDetails loadUserByUsername(String email) throws ResourceNotFoundException, UnexpectedRollbackException {
		String emailLc = email.toLowerCase();
		Registered registered = null;
		try {
			registered = registeredRepository.findById(emailLc).orElseThrow(() -> new ResourceNotFoundException("Registered "+emailLc+" not found"));
		} catch (ResourceNotFoundException rnfe) {
			log.error("Login : {} : {} ", emailLc, rnfe.toString());
			throw new ResourceNotFoundException(rnfe.getMessage());
		} catch(IllegalArgumentException re) {
			log.error("Login : {} : {} ", emailLc, re.toString());
			throw new UnexpectedRollbackException("Error while getting your profile");
		} catch (Exception e) {
			log.error("Login : {} : {} ", emailLc, e.toString());
			throw new UnexpectedRollbackException("Error while getting your profile");
		}
		log.info("Login : registered={} : success", emailLc);

		List<GrantedAuthority> grantedAuthorities = registered.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getRoleName())).collect(Collectors.toList());

		// org.springframework.security.core.userdetails.User;   
		return new User(registered.getEmail(), registered.getPassword(), grantedAuthorities);		
	}
}
