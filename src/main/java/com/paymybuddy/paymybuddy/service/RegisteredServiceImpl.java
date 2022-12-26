package com.paymybuddy.paymybuddy.service;

import java.util.Optional;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.WebRequest;

import com.paymybuddy.paymybuddy.dto.RegisteredDTO;
import com.paymybuddy.paymybuddy.dto.RegisteredForListDTO;
import com.paymybuddy.paymybuddy.dto.service.RegisteredDTOService;
import com.paymybuddy.paymybuddy.exception.ResourceConflictException;
import com.paymybuddy.paymybuddy.exception.ResourceNotFoundException;
import com.paymybuddy.paymybuddy.exception.WithdrawException;
import com.paymybuddy.paymybuddy.model.Registered;
import com.paymybuddy.paymybuddy.repository.RegisteredRepository;
import com.paymybuddy.paymybuddy.repository.RoleRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor
public class RegisteredServiceImpl implements RegisteredService {
		
	private final RegisteredDTOService registeredDTOService;
	
	private final RegisteredRepository registeredRepository;
	
	private final RoleRepository roleRepository;
	
	private final RequestService requestService;

	@Override
	@Transactional(readOnly = true, rollbackFor = UnexpectedRollbackException.class)
	public RegisteredDTO getRegistered(String email, WebRequest request) throws UnexpectedRollbackException {
		RegisteredDTO registeredDTO = null;
		try {
			//Throws ResourceNotFoundException | IllegalArgumentException
			registeredDTO = registeredDTOService.registeredToDTO(registeredRepository.findById(email).orElseThrow(() -> new ResourceNotFoundException("Registrered not found")));
		} catch(ResourceNotFoundException | IllegalArgumentException re) {
			log.error("{} : {} ", requestService.requestToString(request), re.toString());
			throw new UnexpectedRollbackException("Error while getting your profile");
		} catch (Exception e) {
			log.error("{} : {} ", requestService.requestToString(request), e.toString());
			throw new UnexpectedRollbackException("Error while getting your profile");
		}
		log.info("{} : registered={} gotten",  requestService.requestToString(request), registeredDTO.getEmail());
		return registeredDTO;
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
			registered.addRole(roleRepository.findById(1).orElseThrow(() -> new ResourceNotFoundException("Role not found")));
			//Throws IllegalArgumentException | OptimisticLockingFailureException
			createdRegisteredDTO = registeredDTOService.registeredToDTO(registeredRepository.save(registered));			
		} catch(ResourceConflictException rce) {
			log.error("{} : registered={} : {} ", requestService.requestToString(request), registeredDTO.getEmail(), rce.toString());
			throw new ResourceConflictException(rce.getMessage());
		} catch(ResourceNotFoundException | IllegalArgumentException | OptimisticLockingFailureException re) {
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
	@Transactional(rollbackFor = UnexpectedRollbackException.class)
	public void removeRegistered(String email, WebRequest request) throws UnexpectedRollbackException {
		try {		
			Registered registered = registeredRepository.findById(email).orElseThrow(() -> new ResourceNotFoundException("Registered not found"));
			registeredRepository.findAllAddedToEmail(email, Pageable.unpaged())
				.forEach(added -> registeredRepository.findById(added.getEmail()).orElseThrow(() -> new ResourceNotFoundException("Registered added not found"))
				.removeConnection(registered));
			registeredRepository.deleteById(email);
		} catch(IllegalArgumentException | ResourceNotFoundException re) {
			log.error("{} : {} ", requestService.requestToString(request), re.toString());
			throw new UnexpectedRollbackException("Error while removing your profile");
		} catch(Exception e) {
			log.error("{} : {} ", requestService.requestToString(request), e.toString());
			throw new UnexpectedRollbackException("Error while removing your profile");
		}
		log.info("{} : removed and deleted", requestService.requestToString(request));
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
		log.info("{} : page all add by, number : {} of {}",
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
		log.info("{} : page all not add by, number : {} of {}",
			requestService.requestToString(request),
			pageRegisteredForListDTO.getNumber()+1,
			pageRegisteredForListDTO.getTotalPages());
		return pageRegisteredForListDTO;
	}

	@Override
	@Transactional(readOnly = true, rollbackFor = UnexpectedRollbackException.class)
	public Page<RegisteredForListDTO> getAllAddedTo(String email, Pageable pageRequest, WebRequest request) throws UnexpectedRollbackException {
		Page<RegisteredForListDTO> pageRegisteredForListDTO = null;
		try {
			//throws NullPointerException if pageRequest is null
			pageRegisteredForListDTO = registeredRepository.findAllAddedToEmail(email, pageRequest)
					.map(registered -> registeredDTOService.registeredToForListDTO(registered));
		} catch(NullPointerException npe) {
			log.error("{} : {} ", requestService.requestToString(request), npe.toString());
			throw new UnexpectedRollbackException("Error while getting connected to you");
		} catch(Exception e) {
			log.error("{} : {} ", requestService.requestToString(request), e.toString());
			throw new UnexpectedRollbackException("Error while getting connected to you");
		}
		log.info("{} : page all connected to, number : {} of {}",
			requestService.requestToString(request),
			pageRegisteredForListDTO.getNumber()+1,
			pageRegisteredForListDTO.getTotalPages());
		return pageRegisteredForListDTO;
	}

	@Override
	@Transactional(rollbackFor = UnexpectedRollbackException.class)
	public void addConnection(String email, String emailToAdd, WebRequest request) throws UnexpectedRollbackException {
		try {
			Registered registered = registeredRepository.findById(email).orElseThrow(() -> new ResourceNotFoundException("Registered not found"));
			Registered registeredToAdd = registeredRepository.findById(emailToAdd).orElseThrow(() -> new ResourceNotFoundException("Registered to add not found"));
			registered.addConnection(registeredToAdd);
			registeredRepository.save(registered);
		} catch(IllegalArgumentException | OptimisticLockingFailureException | ResourceNotFoundException re) {
			log.error("{} : {} ", requestService.requestToString(request), re.toString());
			throw new UnexpectedRollbackException("Error while adding connection");
		} catch(Exception e) {
			log.error("{} : {} ", requestService.requestToString(request), e.toString());
			throw new UnexpectedRollbackException("Error while adding connection");
		}
		log.info("{} : added and persisted", requestService.requestToString(request));
	}

	@Override
	@Transactional(rollbackFor = UnexpectedRollbackException.class)
	public void removeConnection(String email, String emailToRemove, WebRequest request) throws UnexpectedRollbackException {
		try {
			Registered registered = registeredRepository.findById(email).orElseThrow(() -> new ResourceNotFoundException("Registered not found"));
			Registered registeredToRemove = registeredRepository.findById(emailToRemove).orElseThrow(() -> new ResourceNotFoundException("Registered to remove not found"));
			registered.removeConnection(registeredToRemove);
			registeredRepository.save(registered);
		} catch(IllegalArgumentException | OptimisticLockingFailureException | ResourceNotFoundException re) {
			log.error("{} : {} ", requestService.requestToString(request), re.toString());
			throw new UnexpectedRollbackException("Error while removing connection");
		} catch(Exception e) {
			log.error("{} : {} ", requestService.requestToString(request), e.toString());
			throw new UnexpectedRollbackException("Error while removing connection");
		}
		log.info("{} : removed and persisted", requestService.requestToString(request));
		
	}

	@Override
	@Transactional(rollbackFor = UnexpectedRollbackException.class)
	public void depositFromBank(String email, double amount, WebRequest request) throws UnexpectedRollbackException {
		try {		
			Registered registered = registeredRepository.findById(email).orElseThrow(() -> new ResourceNotFoundException("Registered not found"));
			registered.setBalance(registered.getBalance()+amount);
			registeredRepository.save(registered);
		} catch(IllegalArgumentException | OptimisticLockingFailureException | ResourceNotFoundException re) {
			log.error("{} : {} ", requestService.requestToString(request), re.toString());
			throw new UnexpectedRollbackException("Error while deposit money from bank. Canceled");
		} catch(Exception e) {
			log.error("{} : {} ", requestService.requestToString(request), e.toString());
			throw new UnexpectedRollbackException("Error while deposit money from bank. Canceled");
		}
		log.info("{} : deposit {} from bank persisted", requestService.requestToString(request), amount);
	}

	@Override
	@Transactional(rollbackFor = {UnexpectedRollbackException.class, WithdrawException.class})
	public void withdrawToBank(String email, double amount, WebRequest request) throws UnexpectedRollbackException, WithdrawException {
		try {		
			Registered registered = registeredRepository.findById(email).orElseThrow(() -> new ResourceNotFoundException("Registered not found"));
			double total = registered.getBalance() - amount;
			if (total<0d) {
				throw new WithdrawException("Insufficient funds for withdraw to bank");
			}
			registered.setBalance(total);
			registeredRepository.save(registered);
		} catch(IllegalArgumentException | OptimisticLockingFailureException | ResourceNotFoundException re) {
			log.error("{} : {} ", requestService.requestToString(request), re.toString());
			throw new UnexpectedRollbackException("Error while withdraw to bank. Canceled");
		} catch (WithdrawException we) {
			log.error("{} : {}", requestService.requestToString(request), we.toString());
			throw new WithdrawException(we.getMessage());
		} catch(Exception e) {
			log.error("{} : {} ", requestService.requestToString(request), e.toString());
			throw new UnexpectedRollbackException("Error while withdraw to bank. Canceled");
		}
		log.info("{} : withdraw to bank persisted", requestService.requestToString(request));
	}
	
	@Override
	public void resetRegisteredPassword(String email) {
		// TODO Auto-generated method stub
	}
}
