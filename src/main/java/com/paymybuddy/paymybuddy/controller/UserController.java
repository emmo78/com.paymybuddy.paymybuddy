package com.paymybuddy.paymybuddy.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.paymybuddy.paymybuddy.dto.RegisteredDTO;
import com.paymybuddy.paymybuddy.dto.RegisteredForListDTO;
import com.paymybuddy.paymybuddy.dto.TransactionDTO;
import com.paymybuddy.paymybuddy.exception.ResourceConflictException;
import com.paymybuddy.paymybuddy.service.RegisteredService;
import com.paymybuddy.paymybuddy.service.RequestService;
import com.paymybuddy.paymybuddy.service.TransactionService;

import jakarta.servlet.ServletException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller to interface user use cases with DAL service
 * @author Olivier MOREL
 *
 */
@Controller
@Slf4j
@AllArgsConstructor
public class UserController {

	private final RegisteredService registeredService;
	
	private final TransactionService transactionService;

	private final RequestService requestService;
	
	/** 
	 * try to create registered from ModelAttribute
	 * 
	 * @param registeredDTO
	 * @param request
	 * @return
	 * @throws ResourceConflictException
	 * @throws UnexpectedRollbackException
	 */
	@PostMapping("/createregistered")
	public String createRegistered(@ModelAttribute RegisteredDTO registeredDTO, WebRequest request) throws ResourceConflictException, UnexpectedRollbackException {
		RegisteredDTO registeredDTOCreated = registeredService.createRegistered(registeredDTO, request);
		log.info("{} : {} : registered={} created and persisted",
				requestService.requestToString(request),
				((ServletWebRequest) request).getHttpMethod(),
				registeredDTOCreated.getEmail());
		return "redirect:/";
	}
	/**
	 * update registered from ModelAttribute
	 * 
	 * @param registeredDTO
	 * @param request
	 * @return
	 * @throws UnexpectedRollbackException
	 */
	@PostMapping("/user/updateregistered")
	public String updateRegistered(@ModelAttribute RegisteredDTO registeredDTO, WebRequest request) throws UnexpectedRollbackException {
		RegisteredDTO registeredDTOUpdated = registeredService.updateRegistered(registeredDTO, request);
		log.info("{} : {} : registered={} updated and persisted",
				requestService.requestToString(request),
				((ServletWebRequest) request).getHttpMethod(),
				registeredDTOUpdated.getEmail());
		return "redirect:/user/home/profile";
	}
	
	/**
	 * Delete current user
	 * @param user
	 * @param request
	 * @return
	 * @throws ServletException
	 */
	@GetMapping("/user/removeregistered")
	public String removeRegistered(Principal user, WebRequest request) throws ServletException {
		String email = user.getName();
		registeredService.removeRegistered(email, request);
		log.info("{} : {} : registered={} removed from base",
				requestService.requestToString(request),
				((ServletWebRequest) request).getHttpMethod(),
				email);
		((ServletWebRequest) request).getRequest().logout();
		return "redirect:/";
	}

	/**
	 * Log Out current user. Don't use any service
	 * @param user
	 * @param request
	 * @return
	 * @throws ServletException
	 */
	@GetMapping("/user/logoff")
	public String logoff(Principal user, WebRequest request) throws ServletException {
		log.info("{} : {} : registered={} log off",
				requestService.requestToString(request),
				((ServletWebRequest) request).getHttpMethod(),
				user.getName());
		((ServletWebRequest) request).getRequest().logout();
		return "redirect:/";
	}
	
	/**
	 * return success login page and home of user
	 * @param user
	 * @param model
	 * @param request
	 * @return
	 */
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
	
	/**
	 * return transfer page
	 * @param pageNumberOpt
	 * @param user
	 * @param model
	 * @param request
	 * @return
	 */
	@GetMapping("/user/home/transfer")
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
		return "transfer";
	}
	
	/**
	 * return profile page 
	 * @param user
	 * @param model
	 * @param request
	 * @return
	 */
	@GetMapping("/user/home/profile")
	public String profilePage(Principal user, Model model, WebRequest request) {
		RegisteredDTO registeredDTO = registeredService.getRegistered(user.getName(), request);
		model.addAttribute("user", registeredDTO);
		return "profile";
	}
	
	/**
	 * return contact to add page and if emailToAdd present, add him to user's contacts
	 * @param emailToAddOpt
	 * @param pageNumberOpt
	 * @param user
	 * @param model
	 * @param request
	 * @return
	 */
	@GetMapping("/user/home/profile/add")
	public String profileAddPage(@RequestParam(name = "addEmail") Optional<String> emailToAddOpt, @RequestParam(name = "pageNumber") Optional<String> pageNumberOpt, Principal user, Model model, WebRequest request) {
		String email = user.getName();
		emailToAddOpt.ifPresent(emailToAdd -> {
			registeredService.addConnection(email, emailToAdd, request);
			log.info("{} : {} : {} add connection {} persisted",
					requestService.requestToString(request),
					((ServletWebRequest) request).getHttpMethod(),
					email,
					emailToAdd);
		});
		int index = Integer.parseInt(pageNumberOpt.orElseGet(()-> "0"));
		Pageable pageRequest = PageRequest.of(index, 3, Sort.by("last_name", "first_name").ascending());
		Page<RegisteredForListDTO> allNotAdd = registeredService.getAllNotAddBy(email, pageRequest, request);
		model.addAttribute("allNotAdd", allNotAdd);
		int lastPage = allNotAdd.getTotalPages()-1;
		model.addAttribute("pageInterval", pageInterval(index, lastPage));
		return "profileadd";
	}
	
	/**
	 * return all added by user contact page and if emailToRemove present remove it from user's contacts
	 * @param emailToRemoveOpt
	 * @param pageNumberOpt
	 * @param user
	 * @param model
	 * @param request
	 * @return
	 */
	@GetMapping("/user/home/profile/addby")
	public String profilAddByPage(@RequestParam(name = "removeEmail") Optional<String> emailToRemoveOpt, @RequestParam(name = "pageNumber") Optional<String> pageNumberOpt, Principal user, Model model, WebRequest request) {
		String email = user.getName();
		emailToRemoveOpt.ifPresent(emailToRemove -> {
			registeredService.removeConnection(email, emailToRemove, request);
			log.info("{} : {} : {} remove connection {} persisted",
					requestService.requestToString(request),
					((ServletWebRequest) request).getHttpMethod(),
					email,
					emailToRemove);		
		});
		int index = Integer.parseInt(pageNumberOpt.orElseGet(()-> "0"));
		Pageable pageRequest = PageRequest.of(index, 3, Sort.by("last_name", "first_name").ascending());
		Page<RegisteredForListDTO> allAddBy = registeredService.getAllAddBy(email, pageRequest, request);
		model.addAttribute("allAddBy", allAddBy);
		int lastPage = allAddBy.getTotalPages()-1;
		model.addAttribute("pageInterval", pageInterval(index, lastPage));
		return "profileaddby";
	}
	
	/**
	 * return all that added user to their contact page and if emailToRemove present remove user from its contact)
	 * @param emailToRemoveOpt
	 * @param pageNumberOpt
	 * @param user
	 * @param model
	 * @param request
	 * @return
	 */
	@GetMapping("/user/home/profile/addedto")
	public String profilAddedToPage(@RequestParam(name = "removeEmail") Optional<String> emailToRemoveOpt, @RequestParam(name = "pageNumber") Optional<String> pageNumberOpt, Principal user, Model model, WebRequest request) {
		String email = user.getName();
		emailToRemoveOpt.ifPresent(emailToRemove -> {
			registeredService.removeConnection(emailToRemove, email, request);
			log.info("{} : {} : {} remove connection {} persisted",
					requestService.requestToString(request),
					((ServletWebRequest) request).getHttpMethod(),
					emailToRemove,
					email);
		});
		int index = Integer.parseInt(pageNumberOpt.orElseGet(()-> "0"));
		Pageable pageRequest = PageRequest.of(index, 3, Sort.by("last_name", "first_name").ascending());
		Page<RegisteredForListDTO> allAddedTo = registeredService.getAllAddedTo(email, pageRequest, request);
		model.addAttribute("allAddedTo", allAddedTo);
		int lastPage = allAddedTo.getTotalPages()-1;
		model.addAttribute("pageInterval", pageInterval(index, lastPage));
		return "profileaddedto";
	}
	
	/**
	 * return transfer money from and to bank page
	 * @param user
	 * @param model
	 * @param request
	 * @return
	 */
	@GetMapping("/user/home/bank")
	public String profilTransferMoneyBank(Principal user, Model model, WebRequest request) {
		RegisteredDTO registeredDTO = registeredService.getRegistered(user.getName(), request);
		model.addAttribute("user", registeredDTO);
		return "profilbank";
	}
	
	/**
	 * manage money transfer from and to bank
	 * @param amountS
	 * @param action
	 * @param user
	 * @param request
	 * @return
	 */
	@PostMapping("/user/home/bank")
	public String transferMoneyBank(@RequestParam(name = "amount") String amountS, @RequestParam(name = "action") String action, Principal user, WebRequest request) {
		String email = user.getName();
		double amount = BigDecimal.valueOf(Double.parseDouble(amountS)).setScale(2, RoundingMode.HALF_UP).doubleValue();
		if (action.equals("deposit")&&amount>0) {
			registeredService.depositFromBank(email, amount, request);
		} else if(action.equals("withdraw")&&amount>0) {
			registeredService.withdrawToBank(email, amount, request);
		}
		return "redirect:/user/home/bank";
	}
	
	/**
	 * return contact page
	 * @param user
	 * @param model
	 * @param request
	 * @return
	 */
	@GetMapping("/user/home/contact")
	public String contact(Principal user, Model model, WebRequest request) {
		String email = user.getName();
		model.addAttribute("user", email);
		return "contact";
	}
	
	/**
	 * Manage contact post
	 * @param attributes
	 * @param contact
	 * @return
	 */
	@PostMapping("/user/home/contact")
	public String contactProcessing(RedirectAttributes attributes, @RequestParam(name = "contact") String contact) {
		attributes.addAttribute("contact", contact);
		return "redirect:/user/home/contact";
	}
	
	/**
	 * Calculation of the parameters for the creation of the page interval
	 * @param index
	 * @param lastPage
	 * @return
	 */
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
	
	/**
	 * Create page interval
	 * @param min
	 * @param max
	 * @return
	 */
	private List<Integer> createInterval(int min, int max) {
		List<Integer> interval = new ArrayList<>();
		for (int i = min, j=0; i <= max && j<5; i++,j++) {
			interval.add(j, i);
		}
		return interval;
	}
}
