package com.paymybuddy.paymybuddy.exception;

public class ResourceConflictException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ResourceConflictException(String message) {
		super(message);
	}
}