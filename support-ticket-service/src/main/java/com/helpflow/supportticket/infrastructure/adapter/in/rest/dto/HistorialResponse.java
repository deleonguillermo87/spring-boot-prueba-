package com.helpflow.supportticket.infrastructure.adapter.in.rest.dto;

import java.time.Instant;

public record HistorialResponse(
		Long id,
		String estadoAnterior,
		String estadoNuevo,
		String observacion,
		String autor,
		Instant fecha) {
}

