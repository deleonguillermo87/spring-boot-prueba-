package com.helpflow.supportticket.unit.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import com.helpflow.supportticket.domain.exception.TecnicoNoDisponibleException;
import com.helpflow.supportticket.domain.exception.TransicionInvalidaException;
import com.helpflow.supportticket.domain.model.Categoria;
import com.helpflow.supportticket.domain.model.EstadoTicket;
import com.helpflow.supportticket.domain.model.EstadoUsuario;
import com.helpflow.supportticket.domain.model.Prioridad;
import com.helpflow.supportticket.domain.model.Tecnico;
import com.helpflow.supportticket.domain.model.Ticket;
import org.junit.jupiter.api.Test;

class TicketTest {

	@Test
	void assigningTechnicianChangesStateAndAddsHistory() {
		Ticket ticket = DomainTestFixtures.ticket(
				Prioridad.ALTA, EstadoTicket.ABIERTO,
				DomainTestFixtures.AHORA.plus(Duration.ofHours(8)));
		Tecnico tecnico = DomainTestFixtures.tecnicoDisponible();

		ticket.asignar(tecnico, "Asignación inicial");

		assertEquals(EstadoTicket.ASIGNADO, ticket.getEstado());
		assertEquals(tecnico, ticket.getTecnicoAsignado());
		assertEquals(1, ticket.getHistorial().size());
		assertEquals(EstadoTicket.ABIERTO, ticket.getHistorial().getFirst().estadoAnterior());
		assertEquals(EstadoTicket.ASIGNADO, ticket.getHistorial().getFirst().estadoNuevo());
		assertEquals("Asignación inicial", ticket.getHistorial().getFirst().observacion());
		assertEquals(DomainTestFixtures.AHORA, ticket.getHistorial().getFirst().fecha());
	}

	@Test
	void doesNotMutateTicketWhenTechnicianCannotBeAssigned() {
		Ticket ticket = DomainTestFixtures.ticket(
				Prioridad.ALTA, EstadoTicket.ABIERTO,
				DomainTestFixtures.AHORA.plus(Duration.ofHours(8)));
		Tecnico inactivo = DomainTestFixtures.tecnico(
				8L, "TEC-008", "CENTRAL", Set.of(Categoria.RED), 3, 0, EstadoUsuario.INACTIVO);

		assertThrows(TecnicoNoDisponibleException.class, () -> ticket.asignar(inactivo, "Intento"));
		assertEquals(EstadoTicket.ABIERTO, ticket.getEstado());
		assertNull(ticket.getTecnicoAsignado());
		assertTrue(ticket.getHistorial().isEmpty());
	}

	@Test
	void detectsExpiredSla() {
		Ticket ticket = DomainTestFixtures.ticket(
				Prioridad.ALTA, EstadoTicket.ABIERTO,
				DomainTestFixtures.AHORA.minusSeconds(1));

		assertTrue(ticket.estaVencido(DomainTestFixtures.AHORA));
		assertFalse(ticket.estaVencido(DomainTestFixtures.AHORA.minusSeconds(2)));
		assertTrue(ticket.estaVencido(DomainTestFixtures.AHORA.minusSeconds(1)));
	}

	@Test
	void detectsDeadlineWithinThresholdButNotAnAlreadyExpiredDeadline() {
		Instant deadline = DomainTestFixtures.AHORA.plus(Duration.ofHours(2));
		Ticket ticket = DomainTestFixtures.ticket(Prioridad.ALTA, EstadoTicket.ABIERTO, deadline);

		assertTrue(ticket.estaProximoAVencer(DomainTestFixtures.AHORA, Duration.ofHours(2)));
		assertFalse(ticket.estaProximoAVencer(DomainTestFixtures.AHORA, Duration.ofHours(1)));
		assertFalse(ticket.estaProximoAVencer(deadline, Duration.ofHours(2)));
	}

	@Test
	void changesStateAndRecordsAuthor() {
		Ticket ticket = DomainTestFixtures.ticket(
				Prioridad.ALTA, EstadoTicket.ASIGNADO,
				DomainTestFixtures.AHORA.plus(Duration.ofHours(8)));

		ticket.cambiarEstado(EstadoTicket.EN_PROGRESO, "Iniciado", "TEC-014");

		assertEquals(EstadoTicket.EN_PROGRESO, ticket.getEstado());
		assertEquals("TEC-014", ticket.getHistorial().getFirst().autor());
	}

	@Test
	void escalationRaisesPriorityOneLevel() {
		Ticket ticket = DomainTestFixtures.ticket(
				Prioridad.BAJA, EstadoTicket.ABIERTO,
				DomainTestFixtures.AHORA.plus(Duration.ofHours(8)));

		ticket.escalar("SLA próximo a vencer");
		assertEquals(Prioridad.MEDIA, ticket.getPrioridad());
		ticket.escalar("Escalamiento adicional");
		assertEquals(Prioridad.ALTA, ticket.getPrioridad());
		ticket.escalar("Último escalamiento");
		assertEquals(Prioridad.CRITICA, ticket.getPrioridad());
	}

	@Test
	void escalationKeepsCriticalPriority() {
		Ticket ticket = DomainTestFixtures.ticket(
				Prioridad.CRITICA, EstadoTicket.EN_PROGRESO,
				DomainTestFixtures.AHORA.plus(Duration.ofHours(8)));

		ticket.escalar("Ya está en prioridad máxima");

		assertEquals(Prioridad.CRITICA, ticket.getPrioridad());
	}

	@Test
	void escalationRecordsHistoryWithSystemAuthorAndObservation() {
		Ticket ticket = DomainTestFixtures.ticket(
				Prioridad.MEDIA, EstadoTicket.ASIGNADO,
				DomainTestFixtures.AHORA.plus(Duration.ofHours(8)));

		ticket.escalar("Escalado automáticamente");

		assertEquals(1, ticket.getHistorial().size());
		assertEquals(EstadoTicket.ASIGNADO, ticket.getHistorial().getFirst().estadoAnterior());
		assertEquals(EstadoTicket.ASIGNADO, ticket.getHistorial().getFirst().estadoNuevo());
		assertEquals("Escalado automáticamente", ticket.getHistorial().getFirst().observacion());
		assertEquals("SISTEMA", ticket.getHistorial().getFirst().autor());
		assertEquals(DomainTestFixtures.AHORA, ticket.getHistorial().getFirst().fecha());
	}

	@Test
	void escalationFailsForFinalStatesWithoutChangingTicket() {
		for (EstadoTicket estadoFinal : Set.of(
				EstadoTicket.RESUELTO, EstadoTicket.CERRADO, EstadoTicket.CANCELADO)) {
			Ticket ticket = DomainTestFixtures.ticket(
					Prioridad.ALTA, estadoFinal,
					DomainTestFixtures.AHORA.plus(Duration.ofHours(8)));

			assertThrows(TransicionInvalidaException.class, () -> ticket.escalar("Escalamiento inválido"));
			assertEquals(Prioridad.ALTA, ticket.getPrioridad());
			assertTrue(ticket.getHistorial().isEmpty());
		}
	}
}
