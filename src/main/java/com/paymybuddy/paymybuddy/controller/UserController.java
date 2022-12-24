package com.paymybuddy.paymybuddy.controller;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.paymybuddy.paymybuddy.dto.RegisteredDTO;
import com.paymybuddy.paymybuddy.dto.RegisteredForListDTO;
import com.paymybuddy.paymybuddy.dto.TransactionDTO;
import com.paymybuddy.paymybuddy.exception.ResourceConflictException;
import com.paymybuddy.paymybuddy.service.RegisteredService;
import com.paymybuddy.paymybuddy.service.RequestService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@AllArgsConstructor
public class UserController {

	private final RegisteredService registeredService;

	private final RequestService requestService;
	
	@PostMapping("/createRegistered")
	public String createRegistered(@ModelAttribute RegisteredDTO registeredDTO, WebRequest request) throws ResourceConflictException, UnexpectedRollbackException {
		RegisteredDTO registeredDTOCreated = registeredService.createRegistered(registeredDTO, request);
		log.info("{} : {} : registered={} created and persisted",
				requestService.requestToString(request),
				((ServletWebRequest) request).getHttpMethod(),
				registeredDTOCreated.getEmail());
		return "redirect:/";
	}
	
	@GetMapping("/user/home")
	public String welcomePage(Principal user, Model model, WebRequest request) {
		RegisteredDTO registeredDTO = registeredService.getRegistered(user.getName(), request);
		log.info("{} : {} : registered={} logged in user's home",
				requestService.requestToString(request),
				((ServletWebRequest) request).getHttpMethod(),
				registeredDTO.getEmail());
		model.addAttribute("user", registeredDTO);
		return "userhome";
	}
	
	@GetMapping("/user/home/transfert")
	public String transfertPage(Principal user, Model model, WebRequest request) {
		String email = user.getName();
		model.addAttribute("user", email);
		List<String> emails = registeredService.getAllAddBy(email, Pageable.unpaged(), request).stream().map(RegisteredForListDTO::getEmail).sorted().collect(Collectors.toList());
		model.addAttribute("allAddBy", emails);
		model.addAttribute("transactionDTO", new TransactionDTO(email));
		return "transfert";
	}
}
