package com.paymybuddy.paymybuddy.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.WebRequest;

import com.paymybuddy.paymybuddy.dto.RegisteredDTO;
import com.paymybuddy.paymybuddy.dto.RegisteredForListDTO;
import com.paymybuddy.paymybuddy.dto.service.RegisteredDTOService;
import com.paymybuddy.paymybuddy.exception.ResourceConflictException;
import com.paymybuddy.paymybuddy.exception.ResourceNotFoundException;
import com.paymybuddy.paymybuddy.model.Registered;
import com.paymybuddy.paymybuddy.repository.RegisteredRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RegisteredServiceImpl implements RegisteredService {
		
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Autowired
	private RegisteredDTOService registeredDTOService;
	
	@Autowired
	private RegisteredRepository registeredRepository;
	
	@Autowired
	private RequestService requestService;

	@Override
	@Transactional(readOnly = true, rollbackFor = {ResourceNotFoundException.class, UnexpectedRollbackException.class})
	public Registered getRegistered(String email, WebRequest request) throws ResourceNotFoundException, UnexpectedRollbackException {
		Registered registered = null;
		try {
			//Throws ResourceNotFoundException | IllegalArgumentException
			registered = (registeredRepository.findById(email.toLowerCase()).orElseThrow(() -> new ResourceNotFoundException("Your email is not found")));
		} catch (ResourceNotFoundException rnfe) {
			log.error("{} : email={} : {} ", requestService.requestToString(request), email.toLowerCase(), rnfe.toString());
			throw new ResourceNotFoundException(rnfe.getMessage());
		} catch(IllegalArgumentException re) {
			log.error("{} : email={} : {} ", requestService.requestToString(request), email.toLowerCase(), re.toString());
			throw new UnexpectedRollbackException("Error while getting your profile");
		} catch (Exception e) {
			log.error("{} : email={} : {} ", requestService.requestToString(request), email.toLowerCase(), e.toString());
			throw new UnexpectedRollbackException("Error while getting your profile");
		}
		log.info("{} : Registered {} gotten",  requestService.requestToString(request), registered.getEmail());
		return registered;
	}

	@Override
	@Transactional(rollbackFor = {UnexpectedRollbackException.class, ResourceConflictException.class})
	public RegisteredDTO createRegistered(RegisteredDTO registeredDTO, WebRequest request) throws ResourceConflictException, UnexpectedRollbackException {
		RegisteredDTO createdRegisteredDTO = null;
		try {
			Registered registered = registeredDTOService.registeredFromDTO(registeredDTO);
			if (registeredRepository.existsById(registered.getEmail())) {
				throw new ResourceConflictException("User already exists");
			}
			//Throws IllegalArgumentException | OptimisticLockingFailureException
			createdRegisteredDTO = registeredDTOService.registeredToDTO(registeredRepository.save(registered));			
		} catch(ResourceConflictException rce) {
			log.error("{} : registered={} : {} ", requestService.requestToString(request), registeredDTO.getEmail(), rce.toString());
			throw new ResourceConflictException(rce.getMessage());
		} catch(IllegalArgumentException | OptimisticLockingFailureException re) {
			log.error("{} : registered={} : {} ", requestService.requestToString(request), registeredDTO.getEmail(), re.toString());
			throw new UnexpectedRollbackException("Error while creating your profile");
		} catch(Exception e) {
			log.error("{} : registered={} : {} ", requestService.requestToString(request), registeredDTO.getEmail(), e.toString());
			throw new UnexpectedRollbackException("Error while creating your profile");
		}
		log.info("{} : registered : {} created and persisted",
				requestService.requestToString(request),
				createdRegisteredDTO.getEmail());
		return createdRegisteredDTO;
	}

	@Override
	public RegisteredDTO updateRegistered(RegisteredDTO registeredDTO, WebRequest request) throws UnexpectedRollbackException {
		RegisteredDTO updatedRegisteredDTO = null;
		try {
			Registered registered = registeredDTOService.registeredFromDTO(registeredDTO);
			//Throws IllegalArgumentException
			Registered registeredToUpdate = registeredRepository.findById(registered.getEmail()).get();
			Optional.ofNullable(registered.getFirstName()).ifPresent(firstName -> {
				if (!firstName.equals(registeredToUpdate.getFirstName())) {
					registeredToUpdate.setFirstName(firstName);
				}
			});
			Optional.ofNullable(registered.getLastName()).ifPresent(lastName -> {
				if (!lastName.equals(registeredToUpdate.getLastName())) {
					registeredToUpdate.setLastName(lastName);
				}
			});
			Optional.ofNullable(registered.getBirthDate()).ifPresent(birthDate -> {
				if (!birthDate.equals(registeredToUpdate.getBirthDate())) {
					registeredToUpdate.setBirthDate(birthDate);
				}
			});
			String iban = registered.getIban();
			if (!iban.equals(registeredToUpdate.getIban())) {
				registeredToUpdate.setIban(iban);
			}
			//Throws IllegalArgumentException | OptimisticLockingFailureException
			updatedRegisteredDTO = registeredDTOService.registeredToDTO(registeredRepository.save(registeredToUpdate));
		} catch(IllegalArgumentException | OptimisticLockingFailureException re) {
			log.error("{} : registered={} : {} ", requestService.requestToString(request), registeredDTO.getEmail(), re.toString());
			throw new UnexpectedRollbackException("Error while creating your profile");
		} catch(Exception e) {
			log.error("{} : registered={} : {} ", requestService.requestToString(request), registeredDTO.getEmail(), e.toString());
			throw new UnexpectedRollbackException("Error while creating your profile");
		}
		log.info("{} : registered : {} updated and persisted",
				requestService.requestToString(request),
				updatedRegisteredDTO.getEmail());
		return updatedRegisteredDTO;
	}

	@Override
	public void removeRegistered(String email) {
		// TODO Auto-generated method stub

	}

	@Override
	public Page<RegisteredForListDTO> getAllRegistered(Pageable pageRequest) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Page<RegisteredForListDTO> getAllConnectedToARegistered(String email, Pageable pageRequest) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Page<RegisteredForListDTO> getAllNotConnectedToARegistered(String email, Pageable pageRequest) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<String> getRegisteredIban(String email) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String saveUpdateRegisteredIban(String email, String iban) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void resetRegisteredPassword(String email) {
		// TODO Auto-generated method stub
	}

}
