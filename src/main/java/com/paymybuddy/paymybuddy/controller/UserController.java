package com.paymybuddy.paymybuddy.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.request.WebRequest;

@Controller
public class UserController {
	@GetMapping("/user/home")
	public String welcomePage(Model model, WebRequest request) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		model.addAttribute("user", auth.getPrincipal());

		return "userhome";
	}
}
