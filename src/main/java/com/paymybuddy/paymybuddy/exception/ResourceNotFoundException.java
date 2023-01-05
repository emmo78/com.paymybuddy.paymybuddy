package com.paymybuddy.paymybuddy.exception;

/**
 * Thrown when user not found in database
 * @author olivi
 *
 */
public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ResourceNotFoundException(String message) {
		super(message);
	}

}
