package com.paymybuddy.paymybuddy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.paymybuddy.paymybuddy.dto.TransactionDTO;
import com.paymybuddy.paymybuddy.exception.InsufficentFundsException;
import com.paymybuddy.paymybuddy.service.RequestService;
import com.paymybuddy.paymybuddy.service.TransactionService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller to interface transfer use cases with service
 * @author olivi
 *
 */
@Controller
@Slf4j
@AllArgsConstructor
public class TransferController {
	
	private final TransactionService transactionService;
	
	private final RequestService requestService;

	@PostMapping("/createtransfer")
	public String createTransfert(@ModelAttribute TransactionDTO transactionDTO, WebRequest request) throws InsufficentFundsException, UnexpectedRollbackException {
		TransactionDTO transactionDTOCreated = transactionService.createATransaction(transactionDTO, request);
		log.info("{} : {} : transaction sender={} receiver={} amount={} fee={} persisted",
				requestService.requestToString(request),
				((ServletWebRequest) request).getHttpMethod(),
				transactionDTOCreated.getEmailSender(),
				transactionDTOCreated.getEmailReceiver(),
				transactionDTOCreated.getAmount(),
				transactionDTOCreated.getFee());	
		return "redirect:/user/home/transfer";
	}

}
