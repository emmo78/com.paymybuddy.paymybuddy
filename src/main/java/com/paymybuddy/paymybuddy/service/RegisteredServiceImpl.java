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
			registered = registeredRepository.findById(email.toLowerCase()).orElseThrow(() -> new ResourceNotFoundException("Your email is not found"));
		} catch (ResourceNotFoundException rnfe) {
			log.error("{} : {} ", requestService.requestToString(request), rnfe.toString());
			throw new ResourceNotFoundException(rnfe.getMessage());
		} catch(IllegalArgumentException re) {
			log.error("{} : {} ", requestService.requestToString(request), re.toString());
			throw new UnexpectedRollbackException("Error while getting your profile");
		} catch (Exception e) {
			log.error("{} : {} ", requestService.requestToString(request), e.toString());
			throw new UnexpectedRollbackException("Error while getting your profile");
		}
		log.info("{} : registered={} gotten",  requestService.requestToString(request), registered.getEmail());
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
		log.info("{} : registered={} created and persisted",
				requestService.requestToString(request),
				createdRegisteredDTO.getEmail());
		return createdRegisteredDTO;
	}

	@Override
	@Transactional(rollbackFor = UnexpectedRollbackException.class)
	public RegisteredDTO updateRegistered(RegisteredDTO registeredDTO, WebRequest request) throws UnexpectedRollbackException {
		RegisteredDTO updatedRegisteredDTO = null;
		try {
			Registered registered = registeredDTOService.registeredFromDTO(registeredDTO);
			//Throws IllegalArgumentException | ResourceNotFoundException
			Registered registeredToUpdate = registeredRepository.findById(registered.getEmail()).orElseThrow(() -> new ResourceNotFoundException("Registered not found for update"));
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
		} catch(IllegalArgumentException | OptimisticLockingFailureException | ResourceNotFoundException re) {
			log.error("{} : registered={} : {} ", requestService.requestToString(request), registeredDTO.getEmail(), re.toString());
			throw new UnexpectedRollbackException("Error while updating your profile");
		} catch(Exception e) {
			log.error("{} : registered={} : {} ", requestService.requestToString(request), registeredDTO.getEmail(), e.toString());
			throw new UnexpectedRollbackException("Error while updating your profile");
		}
		log.info("{} : registered={} updated and persisted", requestService.requestToString(request), updatedRegisteredDTO.getEmail());
		return updatedRegisteredDTO;
	}

	@Override
	@Transactional(readOnly = true, rollbackFor = UnexpectedRollbackException.class)
	public Page<RegisteredForListDTO> getRegistrants(Pageable pageRequest, WebRequest request) throws UnexpectedRollbackException{
		Page<RegisteredForListDTO> pageRegisteredForListDTO = null;
		try {
			//throws NullPointerException if pageRequest is null
			pageRegisteredForListDTO = registeredRepository.findAll(pageRequest)
					.map(registered -> registeredDTOService.registeredToForListDTO(registered));
		} catch(NullPointerException npe) {
			log.error("{} : {} ", requestService.requestToString(request), npe.toString());
			throw new UnexpectedRollbackException("Error while getting Registrants");
		} catch(Exception e) {
			log.error("{} : {} ", requestService.requestToString(request), e.toString());
			throw new UnexpectedRollbackException("Error while getting Registrants");
		}
		log.info("{} : page registrants number : {} of {}",
			requestService.requestToString(request),
			pageRegisteredForListDTO.getNumber()+1,
			pageRegisteredForListDTO.getTotalPages());
		return pageRegisteredForListDTO;
	}

	@Override
	@Transactional(readOnly = true, rollbackFor = UnexpectedRollbackException.class)
	public Page<RegisteredForListDTO> getAllAddBy(String email, Pageable pageRequest, WebRequest request) throws UnexpectedRollbackException{
		Page<RegisteredForListDTO> pageRegisteredForListDTO = null;
		try {
			//throws NullPointerException if pageRequest is null
			pageRegisteredForListDTO = registeredRepository.findAllAddByEmail(email, pageRequest)
					.map(registered -> registeredDTOService.registeredToForListDTO(registered));
		} catch(NullPointerException npe) {
			log.error("{} : {} ", requestService.requestToString(request), npe.toString());
			throw new UnexpectedRollbackException("Error while getting connections you added");
		} catch(Exception e) {
			log.error("{} : {} ", requestService.requestToString(request), e.toString());
			throw new UnexpectedRollbackException("Error while getting connections you added");
		}
		log.info("{} : page all add by number : {} of {}",
			requestService.requestToString(request),
			pageRegisteredForListDTO.getNumber()+1,
			pageRegisteredForListDTO.getTotalPages());
		return pageRegisteredForListDTO;
	}

	@Override
	@Transactional(readOnly = true, rollbackFor = UnexpectedRollbackException.class)
	public Page<RegisteredForListDTO> getAllNotAddBy(String email, Pageable pageRequest, WebRequest request) throws UnexpectedRollbackException{
		Page<RegisteredForListDTO> pageRegisteredForListDTO = null;
		try {
			//throws NullPointerException if pageRequest is null
			pageRegisteredForListDTO = registeredRepository.findAllNotAddByEmail(email, pageRequest)
					.map(registered -> registeredDTOService.registeredToForListDTO(registered));
		} catch(NullPointerException npe) {
			log.error("{} : {} ", requestService.requestToString(request), npe.toString());
			throw new UnexpectedRollbackException("Error while getting connections you can add");
		} catch(Exception e) {
			log.error("{} : {} ", requestService.requestToString(request), e.toString());
			throw new UnexpectedRollbackException("Error while getting connections you can add");
		}
		log.info("{} : page all not add by number : {} of {}",
			requestService.requestToString(request),
			pageRegisteredForListDTO.getNumber()+1,
			pageRegisteredForListDTO.getTotalPages());
		return pageRegisteredForListDTO;
	}

	@Override
	@Transactional(rollbackFor = UnexpectedRollbackException.class)
	public void removeRegistered(String email, WebRequest request) throws UnexpectedRollbackException {
		try {		
			//throws IllegalArgumentException
			
			registeredRepository.deleteById(email);
		} catch(IllegalArgumentException re) {
			log.error("{} : {} ", requestService.requestToString(request), re.toString());
			throw new UnexpectedRollbackException("Error while removing your profile");
		} catch(Exception e) {
			log.error("{} : {} ", requestService.requestToString(request), e.toString());
			throw new UnexpectedRollbackException("Error while removing your profile");
		}
		log.info("{} : registered={} removed and deleted", requestService.requestToString(request), email);
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
