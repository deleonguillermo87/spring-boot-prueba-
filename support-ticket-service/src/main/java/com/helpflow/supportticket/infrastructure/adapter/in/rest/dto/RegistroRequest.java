package com.helpflow.supportticket.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegistroRequest(
		@NotBlank String documento,
		@NotBlank String nombre,
		@NotBlank @Email String correoCorporativo,
		@NotBlank String sede,
		@NotBlank String area,
		@NotBlank String password,
		String rol,
		String codigoTecnico) {
}
