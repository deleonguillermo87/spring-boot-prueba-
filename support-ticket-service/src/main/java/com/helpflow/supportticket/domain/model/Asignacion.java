package com.helpflow.supportticket.domain.model;

import java.time.Instant;
import java.util.Objects;

public record Asignacion(
		Long id,
		Long ticketId,
		Long tecnicoId,
		Instant fecha,
		int tiempoEstimadoHoras,
		Disponibilidad disponibilidad) {

	public Asignacion {
		Objects.requireNonNull(ticketId, "ticketId es obligatorio");
		Objects.requireNonNull(tecnicoId, "tecnicoId es obligatorio");
		Objects.requireNonNull(fecha, "fecha es obligatoria");
		Objects.requireNonNull(disponibilidad, "disponibilidad es obligatoria");
		if (tiempoEstimadoHoras < 0) {
			throw new IllegalArgumentException("tiempoEstimadoHoras no puede ser negativo");
		}
	}
}
