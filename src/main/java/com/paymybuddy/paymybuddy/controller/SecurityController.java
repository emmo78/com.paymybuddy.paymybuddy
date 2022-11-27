package com.paymybuddy.paymybuddy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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
}
