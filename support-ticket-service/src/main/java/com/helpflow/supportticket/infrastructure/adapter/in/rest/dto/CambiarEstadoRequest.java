package com.helpflow.supportticket.infrastructure.adapter.in.rest.dto;

import com.helpflow.supportticket.domain.model.EstadoTicket;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.dto.validation.ValidCambioEstado;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@ValidCambioEstado
public record CambiarEstadoRequest(
		@NotNull EstadoTicket estado,
		@Size(max = 500) String observacion) {
}

