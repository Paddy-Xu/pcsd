package com.acertainbank;

public class CommunicationException extends Exception {

	private static final long serialVersionUID = 1L;

	public CommunicationException() {}

	public CommunicationException(String message) {
		super(message);
	}

}
