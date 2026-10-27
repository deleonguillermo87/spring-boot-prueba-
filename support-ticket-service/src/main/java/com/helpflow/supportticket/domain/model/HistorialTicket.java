package com.helpflow.supportticket.domain.model;

import java.time.Instant;
import java.util.Objects;

public record HistorialTicket(
		Long id,
		EstadoTicket estadoAnterior,
		EstadoTicket estadoNuevo,
		String observacion,
		String autor,
		Instant fecha) {

	public HistorialTicket {
		Objects.requireNonNull(estadoNuevo, "estadoNuevo es obligatorio");
		Objects.requireNonNull(observacion, "observacion es obligatoria");
		Objects.requireNonNull(autor, "autor es obligatorio");
		Objects.requireNonNull(fecha, "fecha es obligatoria");
	}
}
