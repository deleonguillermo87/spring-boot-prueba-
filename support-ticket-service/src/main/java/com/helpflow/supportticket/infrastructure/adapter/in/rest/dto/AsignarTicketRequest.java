package com.helpflow.supportticket.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AsignarTicketRequest(
		@NotBlank @Size(max = 30) String codigoTecnico,
		@NotBlank @Size(max = 500) String observacion) {
}

