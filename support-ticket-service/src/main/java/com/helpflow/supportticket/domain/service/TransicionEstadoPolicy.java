package com.helpflow.supportticket.domain.service;

import java.util.Map;
import java.util.Set;

import com.helpflow.supportticket.domain.exception.TransicionInvalidaException;
import com.helpflow.supportticket.domain.model.EstadoTicket;

public class TransicionEstadoPolicy {

	private static final Map<EstadoTicket, Set<EstadoTicket>> TRANSICIONES = Map.of(
			EstadoTicket.ABIERTO, Set.of(EstadoTicket.ASIGNADO, EstadoTicket.CANCELADO),
			EstadoTicket.ASIGNADO, Set.of(EstadoTicket.EN_PROGRESO, EstadoTicket.CANCELADO),
			EstadoTicket.EN_PROGRESO, Set.of(EstadoTicket.RESUELTO, EstadoTicket.CANCELADO),
			EstadoTicket.RESUELTO, Set.of(EstadoTicket.CERRADO, EstadoTicket.EN_PROGRESO),
			EstadoTicket.CERRADO, Set.of(),
			EstadoTicket.CANCELADO, Set.of());

	public boolean puedeTransicionar(EstadoTicket actual, EstadoTicket nuevo) {
		return actual != null && nuevo != null && TRANSICIONES.get(actual).contains(nuevo);
	}

	public void validar(EstadoTicket actual, EstadoTicket nuevo) {
		if (!puedeTransicionar(actual, nuevo)) {
			throw new TransicionInvalidaException(
					"No se permite la transición de " + actual + " a " + nuevo + ".");
		}
	}
}
