package com.helpflow.supportticket.infrastructure.adapter.out.client.dto;

public record DispatchClientResponse(
		Long ticketId,
		String tecnicoId,
		String disponibilidad,
		int tiempoEstimadoHoras) {
}

