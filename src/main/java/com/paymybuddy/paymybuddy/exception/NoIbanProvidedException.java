package com.paymybuddy.paymybuddy.exception;

/**
 * Thrown when a user try a transfer to or from bank without an iban registered
 * @author olivi
 *
 */
public class NoIbanProvidedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NoIbanProvidedException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

}
