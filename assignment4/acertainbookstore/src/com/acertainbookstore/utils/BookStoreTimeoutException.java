package com.acertainbookstore.utils;

public class BookStoreTimeoutException extends BookStoreException {

	public BookStoreTimeoutException() {
		super();
	}

	public BookStoreTimeoutException(String message) {
		super(message);
	}

	public BookStoreTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public BookStoreTimeoutException(Throwable ex) {
		super(ex);
	}
}
