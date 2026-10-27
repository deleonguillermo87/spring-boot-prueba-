package com.helpflow.supportticket.application.port.out;

import java.util.Objects;

import com.helpflow.supportticket.domain.model.Disponibilidad;

public record DespachoSugerido(String tecnicoCodigo, Disponibilidad disponibilidad, int tiempoEstimadoHoras) {

	public DespachoSugerido {
		Objects.requireNonNull(tecnicoCodigo, "tecnicoCodigo es obligatorio");
		Objects.requireNonNull(disponibilidad, "disponibilidad es obligatoria");
		if (tiempoEstimadoHoras < 0) {
			throw new IllegalArgumentException("tiempoEstimadoHoras no puede ser negativo");
		}
	}
}
