package com.paymybuddy.paymybuddy.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.context.request.WebRequest;

import com.paymybuddy.paymybuddy.dto.RegisteredDTO;
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
	public String createRegistered(@ModelAttribute RegisteredDTO registeredDTO, WebRequest request) {
		RegisteredDTO registeredDTOCreated = registeredService.createRegistered(registeredDTO, request);
		log.info("{} : registered={} created and persisted",
				requestService.requestToString(request),
				registeredDTOCreated.getEmail());
		return "redirect:/";
	}
	
	@GetMapping("/user/home")
	public String welcomePage(Principal user, Model model, WebRequest request) {
		String email = user.getName();
		log.info("{} : registered={} logged in",  requestService.requestToString(request), email);
		model.addAttribute("user", email);
		return "userhome";
	}
}
