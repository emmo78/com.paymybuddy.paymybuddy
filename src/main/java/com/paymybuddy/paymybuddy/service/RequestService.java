package com.paymybuddy.paymybuddy.service;

import org.springframework.web.context.request.WebRequest;

/**
 * Single responsibility for request treatment
 * @author Olivier MOREL
 *
 */
public interface RequestService {

	/**
	 * Uppercase first letter
	 * @param word to treat
	 * @return : Word
	 */
	String upperCasingFirstLetter(String word);

	/**
	 * To string all the parmeters of a Web request
	 * @param request to String
	 * @return String of parameters
	 */
	String requestToString(WebRequest request);
}
