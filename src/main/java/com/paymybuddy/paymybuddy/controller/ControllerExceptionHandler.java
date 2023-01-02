package com.paymybuddy.paymybuddy.controller;

import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.paymybuddy.paymybuddy.exception.InsufficentFundsException;
import com.paymybuddy.paymybuddy.exception.NoIbanProvidedException;
import com.paymybuddy.paymybuddy.exception.ResourceConflictException;
import com.paymybuddy.paymybuddy.exception.WithdrawException;
import com.paymybuddy.paymybuddy.service.RequestService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
@AllArgsConstructor
public class ControllerExceptionHandler {
	
	private final RequestService requestService;
	
	@ExceptionHandler(ResourceConflictException.class)
    public String ressourceConflictException(ResourceConflictException ex, WebRequest request, RedirectAttributes attributes) {
		String errorMessage = ex.getMessage();
		log.error("{} : {} : {}",
				requestService.requestToString(request),
				((ServletWebRequest) request).getHttpMethod(),
				errorMessage);
		attributes.addAttribute("errorMessage", errorMessage);
        return "redirect:/register";
    }
	
	@ExceptionHandler(InsufficentFundsException.class)
	public String insufficentFundsException(InsufficentFundsException ex, WebRequest request,  RedirectAttributes attributes) {
		String errorMessage = ex.getMessage();
		log.error("{} : {} : {}",
				requestService.requestToString(request),
				((ServletWebRequest) request).getHttpMethod(),
				errorMessage);
		attributes.addAttribute("errorMessage", errorMessage);
        return "redirect:/user/home/transfer";
    }
	
	@ExceptionHandler(WithdrawException.class)
	public String withdrawExceptionException(WithdrawException ex, WebRequest request,  RedirectAttributes attributes) {
		String errorMessage = ex.getMessage();
		log.error("{} : {} : {}",
				requestService.requestToString(request),
				((ServletWebRequest) request).getHttpMethod(),
				errorMessage);
		attributes.addAttribute("errorMessage", errorMessage);
        return "redirect:/user/home/bank";
    }
	
	@ExceptionHandler(NoIbanProvidedException.class)
	public String noIbanProvidedException(NoIbanProvidedException ex, WebRequest request,  RedirectAttributes attributes) {
		String errorMessage = ex.getMessage();
		log.error("{} : {} : {}",
				requestService.requestToString(request),
				((ServletWebRequest) request).getHttpMethod(),
				errorMessage);
		attributes.addAttribute("errorMessage", errorMessage);
        return "redirect:/user/home/profile";
    }
	
	@ExceptionHandler(UnexpectedRollbackException.class)
    public String unexpectedRollbackException(UnexpectedRollbackException ex, WebRequest request, Model model) {
		String errorMessage = ex.getMessage();
		log.error("{} : {} : {}",
				requestService.requestToString(request),
				((ServletWebRequest) request).getHttpMethod(),
				errorMessage);
		model.addAttribute("errorMessage", errorMessage);
        return "error";
    }

}
