package com.helpflow.supportticket.infrastructure.adapter.in.rest.dto;

import com.helpflow.supportticket.domain.model.Categoria;
import com.helpflow.supportticket.domain.model.Prioridad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearTicketRequest(
		@NotBlank @Size(max = 120) String titulo,
		@NotBlank @Size(max = 2000) String descripcion,
		@NotNull Categoria categoria,
		@NotNull Prioridad prioridad,
		@NotBlank @Size(max = 60) String sede,
		@Size(max = 60) String activoAfectado) {
}

