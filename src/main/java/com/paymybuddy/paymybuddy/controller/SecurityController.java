package com.paymybuddy.paymybuddy.controller;

import java.security.Principal;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

import com.paymybuddy.paymybuddy.dto.RegisteredDTO;

@Controller
public class SecurityController {
	
	@GetMapping("/")
	public String homePage() {
		return "redirect:/login";
	}

	@GetMapping("/login")
	public String loginPage(Principal user) {
		if (isAuthenticated(user)) {
			return "redirect:/user/home";
		}
		return "login";
	}
	
	@GetMapping("/register")
	public String register(@RequestParam(name = "errorMessage") Optional<String> errorMessageOpt, Principal user, Model model, WebRequest request) {
		if (isAuthenticated(user)) {
			return "redirect:/user/home";
		}
		errorMessageOpt.ifPresent(em -> model.addAttribute("errorMessage",em));
		model.addAttribute("registeredDTO", new RegisteredDTO());
		return "register";
	}
	
	private boolean isAuthenticated(Principal user) {
		UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) user;
		if (token != null) {
			if (token.isAuthenticated()) {
				return true;
			}
		}
		return false;
	}
}
