package com.paymybuddy.paymybuddy.exception;

/**
 * thrown when the balance is insufficient for the transfer
 * @author Olivier MOREL
 *
 */
public class InsufficentFundsException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InsufficentFundsException(String message) {
		super(message);
	}

}
