package com.helpflow.supportticket.application.port.out;

public interface PasswordHasherPort {

	String hash(String raw);

	boolean coincide(String raw, String hash);
}
