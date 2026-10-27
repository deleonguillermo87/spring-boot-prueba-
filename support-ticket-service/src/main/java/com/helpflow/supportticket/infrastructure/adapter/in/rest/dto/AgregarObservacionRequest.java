package com.helpflow.supportticket.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AgregarObservacionRequest(
		@NotBlank @Size(max = 500) String observacion) {
}

