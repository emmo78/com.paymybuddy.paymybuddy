package com.paymybuddy.paymybuddy.exception;

/**
 * thrown when the balance is insufficient for the withdraw to bank
 * @author Olivier MOREL
 *
 */
public class WithdrawException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public WithdrawException(String message) {
		super(message);
	}
}
