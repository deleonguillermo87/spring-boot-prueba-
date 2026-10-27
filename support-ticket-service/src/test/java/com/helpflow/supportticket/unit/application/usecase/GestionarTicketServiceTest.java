package com.helpflow.supportticket.unit.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;

import com.helpflow.supportticket.application.dto.Actor;
import com.helpflow.supportticket.application.dto.GestionarTicketCommand;
import com.helpflow.supportticket.application.port.out.MetricasPort;
import com.helpflow.supportticket.application.port.out.TicketRepositoryPort;
import com.helpflow.supportticket.application.usecase.GestionarTicketService;
import com.helpflow.supportticket.domain.exception.AccesoNoPermitidoException;
import com.helpflow.supportticket.domain.exception.TransicionInvalidaException;
import com.helpflow.supportticket.domain.model.EstadoTicket;
import com.helpflow.supportticket.domain.model.Rol;
import com.helpflow.supportticket.domain.model.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GestionarTicketServiceTest {

	@Mock TicketRepositoryPort ticketRepository;
	@Mock MetricasPort metricas;
	private GestionarTicketService service;

	@BeforeEach
	void setUp() {
		service = new GestionarTicketService(ticketRepository, metricas);
	}

	@Test
	void appliesValidTransitionAndMeasuresFirstResponse() {
		Ticket ticket = ApplicationTestFixtures.ticket(
				EstadoTicket.ASIGNADO, ApplicationTestFixtures.AHORA.plus(Duration.ofHours(8)));
		when(ticketRepository.buscarPorId(10L)).thenReturn(Optional.of(ticket));
		when(ticketRepository.guardar(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

		service.ejecutar(command(EstadoTicket.EN_PROGRESO, ApplicationTestFixtures.tecnicoActor()));

		assertEquals(EstadoTicket.EN_PROGRESO, ticket.getEstado());
		verify(metricas).tiempoPrimeraRespuesta(any(Duration.class));
	}

	@Test
	void rejectsInvalidTransition() {
		Ticket ticket = ApplicationTestFixtures.ticket(
				EstadoTicket.ABIERTO, ApplicationTestFixtures.AHORA.plus(Duration.ofHours(8)));
		when(ticketRepository.buscarPorId(10L)).thenReturn(Optional.of(ticket));

		assertThrows(TransicionInvalidaException.class,
				() -> service.ejecutar(command(EstadoTicket.RESUELTO, ApplicationTestFixtures.adminActor())));
	}

	@Test
	void rejectsTechnicianWhoDoesNotOwnTicket() {
		Ticket ticket = ApplicationTestFixtures.ticket(
				EstadoTicket.ASIGNADO, ApplicationTestFixtures.AHORA.plus(Duration.ofHours(8)));
		when(ticketRepository.buscarPorId(10L)).thenReturn(Optional.of(ticket));

		assertThrows(AccesoNoPermitidoException.class,
				() -> service.ejecutar(command(EstadoTicket.EN_PROGRESO, new Actor(999L, Rol.ROLE_TECNICO))));
	}

	@Test
	void userCannotMoveOwnTicketToInProgress() {
		Ticket ticket = ApplicationTestFixtures.ticket(
				EstadoTicket.ABIERTO, ApplicationTestFixtures.AHORA.plus(Duration.ofHours(8)));
		when(ticketRepository.buscarPorId(10L)).thenReturn(Optional.of(ticket));

		assertThrows(AccesoNoPermitidoException.class,
				() -> service.ejecutar(command(EstadoTicket.EN_PROGRESO, ApplicationTestFixtures.usuarioActor())));
	}

	private GestionarTicketCommand command(EstadoTicket estado, Actor actor) {
		return new GestionarTicketCommand(10L, estado, "Actualización", actor);
	}
}
