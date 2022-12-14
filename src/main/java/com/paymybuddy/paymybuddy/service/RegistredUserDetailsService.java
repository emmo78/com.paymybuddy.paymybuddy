package com.paymybuddy.paymybuddy.service;

import java.util.List;
import java.util.stream.Collectors;

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

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring security - UserDetailService interface implementation
 * try to find registered from email and return a userDetail
 * 
 * @author Olivier MOREL
 *
 */
@Service
@Slf4j
@AllArgsConstructor
public class RegistredUserDetailsService implements UserDetailsService {

	private final RegisteredRepository registeredRepository;
	
	@Override
	@Transactional(readOnly = true, rollbackFor = {ResourceNotFoundException.class, UnexpectedRollbackException.class})
	public UserDetails loadUserByUsername(String email) throws ResourceNotFoundException, UnexpectedRollbackException {
		String emailLc = email.toLowerCase();
		Registered registered = null;
		try {
			registered = registeredRepository.findById(emailLc).orElseThrow(() -> new ResourceNotFoundException("Registered "+emailLc+" not found"));
		} catch (ResourceNotFoundException rnfe) {
			log.error("/login : {} : {} ", emailLc, rnfe.toString());
			throw new ResourceNotFoundException(rnfe.getMessage());
		} catch(IllegalArgumentException re) {
			log.error("/login : {} : {} ", emailLc, re.toString());
			throw new UnexpectedRollbackException("Error while getting your profile");
		} catch (Exception e) {
			log.error("/login : {} : {} ", emailLc, e.toString());
			throw new UnexpectedRollbackException("Error while getting your profile");
		}
		log.info("/login : registered={} found", emailLc);

		List<GrantedAuthority> grantedAuthorities = registered.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getRoleName())).collect(Collectors.toList());

		// org.springframework.security.core.userdetails.User;   
		return new User(registered.getEmail(), registered.getPassword(), grantedAuthorities);		
	}
}
