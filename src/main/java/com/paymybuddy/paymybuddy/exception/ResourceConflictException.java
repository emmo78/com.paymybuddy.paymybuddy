package com.paymybuddy.paymybuddy.exception;

/**
 * Thrown when a new user try to register with a email already known
 * @author olivi
 *
 */
public class ResourceConflictException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ResourceConflictException(String message) {
		super(message);
	}
}
