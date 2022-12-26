package com.paymybuddy.paymybuddy.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.paymybuddy.paymybuddy.dto.RegisteredDTO;
import com.paymybuddy.paymybuddy.dto.RegisteredForListDTO;
import com.paymybuddy.paymybuddy.dto.TransactionDTO;
import com.paymybuddy.paymybuddy.exception.ResourceConflictException;
import com.paymybuddy.paymybuddy.service.RegisteredService;
import com.paymybuddy.paymybuddy.service.RequestService;
import com.paymybuddy.paymybuddy.service.TransactionService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@AllArgsConstructor
public class UserController {

	private final RegisteredService registeredService;
	
	private final TransactionService transactionService;

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
	public String transfertPage(@RequestParam(name = "pageNumber") Optional<String> pageNumberOpt, Principal user, Model model, WebRequest request) {
		String email = user.getName();
		RegisteredDTO registeredDTO = registeredService.getRegistered(user.getName(), request);
		model.addAttribute("user", registeredDTO);
		model.addAttribute("allAddBy", registeredService.getAllAddBy(email, Pageable.unpaged(), request).stream().map(RegisteredForListDTO::getEmail).sorted().collect(Collectors.toList()));
		model.addAttribute("transactionDTO", new TransactionDTO(email));
		
		int index = Integer.parseInt(pageNumberOpt.orElseGet(()-> "0"));
		Pageable pageRequest = PageRequest.of(index, 3, Sort.by("date_time").descending());
		Page<TransactionDTO> transactions = transactionService.getRegisteredAllTransaction(email, pageRequest, request);
		model.addAttribute("transactions", transactions);
		int lastPage = transactions.getTotalPages()-1;
		model.addAttribute("pageInterval", pageInterval(index, lastPage));
		return "transfert";
	}
	
	@GetMapping("/user/home/profile/add")
	public String profilAddPage(@RequestParam(name = "addEmail") Optional<String> emailToAddOpt, @RequestParam(name = "pageNumber") Optional<String> pageNumberOpt, Principal user, Model model, WebRequest request) {
		String email = user.getName();
		emailToAddOpt.ifPresent(emailToAdd -> registeredService.addConnection(email, emailToAdd, request));
		int index = Integer.parseInt(pageNumberOpt.orElseGet(()-> "0"));
		Pageable pageRequest = PageRequest.of(0, 5, Sort.by("last_name", "first_name").ascending());
		Page<RegisteredForListDTO> allNotAdd = registeredService.getAllNotAddBy(email, pageRequest, request);
		model.addAttribute("allNotAdd", allNotAdd);
		int lastPage = allNotAdd.getTotalPages()-1;
		model.addAttribute("pageInterval", pageInterval(index, lastPage));
		return "profileadd";
	}
	
	private List<Integer> pageInterval(int index, int lastPage) {
		if (lastPage>=0) {
			if (index-2 <= 0) {
				return createInterval(1, lastPage+1);
			} else if (index+2 > lastPage) {
				if (lastPage-4 <= 0) {
					return createInterval(1, lastPage+1);
				} else {
					return createInterval(lastPage-3, lastPage+1);
				}
			} else {
				return createInterval(index-1, index+3);
			}
		} else {
			return null;
		}
	}
	
	private List<Integer> createInterval(int min, int max) {
		List<Integer> interval = new ArrayList<>();
		for (int i = min, j=0; i <= max && j<5; i++,j++) {
			interval.add(j, i);
		}
		return interval;
	}
}
