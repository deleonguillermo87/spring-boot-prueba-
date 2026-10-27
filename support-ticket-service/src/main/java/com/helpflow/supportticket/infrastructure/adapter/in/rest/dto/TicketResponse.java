package com.helpflow.supportticket.infrastructure.adapter.in.rest.dto;

import java.time.Instant;
import java.util.List;

public record TicketResponse(
		Long id,
		Long solicitanteId,
		String titulo,
		String descripcion,
		String categoria,
		String prioridad,
		String sede,
		String activoAfectado,
		Instant fechaCreacion,
		Instant fechaLimiteSla,
		String estado,
		Long tecnicoAsignadoId,
		String tecnicoAsignadoCodigo,
		List<HistorialResponse> historial) {

	public TicketResponse {
		historial = historial == null ? List.of() : List.copyOf(historial);
	}
}

