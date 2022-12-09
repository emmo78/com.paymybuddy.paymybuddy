package com.paymybuddy.paymybuddy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.request.WebRequest;

import com.paymybuddy.paymybuddy.dto.RegisteredDTO;

@Controller
public class SecurityController {
	
	@GetMapping("/")
	public String homePage() {
		return "login";
	}

	@GetMapping("/login")
	public String loginPage() {
		return "login";
	}
	
	@GetMapping("/register")
	public String register(Model model, WebRequest request) {
		model.addAttribute("registeredDTO", new RegisteredDTO());
		return "register";
	}
}
