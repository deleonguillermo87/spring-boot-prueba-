package com.helpflow.supportticket.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@NotBlank @Email String correoCorporativo,
		@NotBlank String password) {
}
