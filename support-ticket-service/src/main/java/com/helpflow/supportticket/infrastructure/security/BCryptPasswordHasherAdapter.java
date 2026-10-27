package com.helpflow.supportticket.infrastructure.security;

import java.util.Objects;

import com.helpflow.supportticket.application.port.out.PasswordHasherPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordHasherAdapter implements PasswordHasherPort {

	private final PasswordEncoder passwordEncoder;

	public BCryptPasswordHasherAdapter(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public String hash(String raw) {
		return passwordEncoder.encode(Objects.requireNonNull(raw, "raw es obligatorio"));
	}

	@Override
	public boolean coincide(String raw, String hash) {
		return passwordEncoder.matches(
				Objects.requireNonNull(raw, "raw es obligatorio"),
				Objects.requireNonNull(hash, "hash es obligatorio"));
	}
}
