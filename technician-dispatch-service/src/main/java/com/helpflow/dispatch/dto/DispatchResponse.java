package com.helpflow.dispatch.dto;

public record DispatchResponse(
		Long ticketId,
		String tecnicoId,
		Disponibilidad disponibilidad,
		int tiempoEstimadoHoras) {
}
