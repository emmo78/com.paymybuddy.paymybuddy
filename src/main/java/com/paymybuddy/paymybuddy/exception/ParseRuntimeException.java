package com.paymybuddy.paymybuddy.exception;

/**
 * To handle Exception management in DTO Service
 * @author Olivier MOREL
 *
 */
public class ParseRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ParseRuntimeException(String message) {
		super(message);
	}
}
