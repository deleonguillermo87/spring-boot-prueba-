package com.helpflow.supportticket.unit.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.Set;

import com.helpflow.supportticket.domain.exception.TransicionInvalidaException;
import com.helpflow.supportticket.domain.model.EstadoTicket;
import com.helpflow.supportticket.domain.service.TransicionEstadoPolicy;
import org.junit.jupiter.api.Test;

class TransicionEstadoPolicyTest {

	private final TransicionEstadoPolicy policy = new TransicionEstadoPolicy();

	@Test
	void acceptsEveryAllowedTransition() {
		Map<EstadoTicket, Set<EstadoTicket>> permitidas = Map.of(
				EstadoTicket.ABIERTO, Set.of(EstadoTicket.ASIGNADO, EstadoTicket.CANCELADO),
				EstadoTicket.ASIGNADO, Set.of(EstadoTicket.EN_PROGRESO, EstadoTicket.CANCELADO),
				EstadoTicket.EN_PROGRESO, Set.of(EstadoTicket.RESUELTO, EstadoTicket.CANCELADO),
				EstadoTicket.RESUELTO, Set.of(EstadoTicket.CERRADO, EstadoTicket.EN_PROGRESO));

		permitidas.forEach((actual, siguientes) -> siguientes.forEach(
				nuevo -> assertDoesNotThrow(() -> policy.validar(actual, nuevo))));
	}

	@Test
	void rejectsEveryTransitionNotExplicitlyAllowed() {
		Map<EstadoTicket, Set<EstadoTicket>> permitidas = Map.of(
				EstadoTicket.ABIERTO, Set.of(EstadoTicket.ASIGNADO, EstadoTicket.CANCELADO),
				EstadoTicket.ASIGNADO, Set.of(EstadoTicket.EN_PROGRESO, EstadoTicket.CANCELADO),
				EstadoTicket.EN_PROGRESO, Set.of(EstadoTicket.RESUELTO, EstadoTicket.CANCELADO),
				EstadoTicket.RESUELTO, Set.of(EstadoTicket.CERRADO, EstadoTicket.EN_PROGRESO),
				EstadoTicket.CERRADO, Set.of(),
				EstadoTicket.CANCELADO, Set.of());

		for (EstadoTicket actual : EstadoTicket.values()) {
			for (EstadoTicket nuevo : EstadoTicket.values()) {
				if (!permitidas.get(actual).contains(nuevo)) {
					assertThrows(TransicionInvalidaException.class, () -> policy.validar(actual, nuevo));
				}
			}
		}
	}

	@Test
	void rejectsNullStates() {
		assertThrows(TransicionInvalidaException.class, () -> policy.validar(null, EstadoTicket.ABIERTO));
		assertThrows(TransicionInvalidaException.class, () -> policy.validar(EstadoTicket.ABIERTO, null));
	}
}
