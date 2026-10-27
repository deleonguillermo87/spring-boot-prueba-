package com.helpflow.supportticket.application.port.out;

public class DispatchUnavailableException extends RuntimeException {

	public DispatchUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}

	public DispatchUnavailableException(String message) {
		super(message);
	}
}
