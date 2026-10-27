package com.helpflow.supportticket.unit.application.usecase;

import java.time.Duration;
import java.util.Optional;

import com.helpflow.supportticket.application.dto.Actor;
import com.helpflow.supportticket.application.dto.RegistrarObservacionCommand;
import com.helpflow.supportticket.application.port.out.TicketRepositoryPort;
import com.helpflow.supportticket.application.usecase.RegistrarObservacionService;
import com.helpflow.supportticket.domain.exception.AccesoNoPermitidoException;
import com.helpflow.supportticket.domain.exception.TicketNoEncontradoException;
import com.helpflow.supportticket.domain.exception.TransicionInvalidaException;
import com.helpflow.supportticket.domain.model.EstadoTicket;
import com.helpflow.supportticket.domain.model.Rol;
import com.helpflow.supportticket.domain.model.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarObservacionServiceTest {

	@Mock
	private TicketRepositoryPort ticketRepository;

	private RegistrarObservacionService service;

	@BeforeEach
	void setUp() {
		service = new RegistrarObservacionService(ticketRepository);
	}

	@Test
	void adminPuedeRegistrarObservacion() {
		Ticket ticket = ApplicationTestFixtures.ticket(EstadoTicket.ASIGNADO, ApplicationTestFixtures.AHORA.plus(Duration.ofHours(8)));
		when(ticketRepository.buscarPorId(10L)).thenReturn(Optional.of(ticket));
		when(ticketRepository.guardar(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

		Ticket resultado = service.ejecutar(new RegistrarObservacionCommand(10L, "Nota de admin", ApplicationTestFixtures.adminActor()));

		assertEquals(1, resultado.getHistorial().size());
		assertEquals("Nota de admin", resultado.getHistorial().getFirst().observacion());
		assertEquals("99", resultado.getHistorial().getFirst().autor());
		verify(ticketRepository).guardar(ticket);
	}

	@Test
	void solicitantePuedeRegistrarObservacion() {
		Ticket ticket = ApplicationTestFixtures.ticket(EstadoTicket.ABIERTO, ApplicationTestFixtures.AHORA.plus(Duration.ofHours(8)));
		when(ticketRepository.buscarPorId(10L)).thenReturn(Optional.of(ticket));
		when(ticketRepository.guardar(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

		Ticket resultado = service.ejecutar(new RegistrarObservacionCommand(10L, "Nota del usuario", ApplicationTestFixtures.usuarioActor()));

		assertEquals(1, resultado.getHistorial().size());
		assertEquals("Nota del usuario", resultado.getHistorial().getFirst().observacion());
		assertEquals("1", resultado.getHistorial().getFirst().autor());
	}

	@Test
	void tecnicoAsignadoPuedeRegistrarObservacion() {
		Ticket ticket = ApplicationTestFixtures.ticket(EstadoTicket.ASIGNADO, ApplicationTestFixtures.AHORA.plus(Duration.ofHours(8)));
		when(ticketRepository.buscarPorId(10L)).thenReturn(Optional.of(ticket));
		when(ticketRepository.guardar(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

		Ticket resultado = service.ejecutar(new RegistrarObservacionCommand(10L, "Nota del tecnico", ApplicationTestFixtures.tecnicoActor()));

		assertEquals(1, resultado.getHistorial().size());
		assertEquals("Nota del tecnico", resultado.getHistorial().getFirst().observacion());
		assertEquals("114", resultado.getHistorial().getFirst().autor());
	}

	@Test
	void usuarioAjenoNoPuedeRegistrarObservacion() {
		Ticket ticket = ApplicationTestFixtures.ticket(EstadoTicket.ABIERTO, ApplicationTestFixtures.AHORA.plus(Duration.ofHours(8)));
		when(ticketRepository.buscarPorId(10L)).thenReturn(Optional.of(ticket));

		Actor otroUsuario = new Actor(999L, Rol.ROLE_USUARIO);
		assertThrows(AccesoNoPermitidoException.class, () ->
				service.ejecutar(new RegistrarObservacionCommand(10L, "Nota ajena", otroUsuario)));
	}

	@Test
	void tecnicoNoAsignadoNoPuedeRegistrarObservacion() {
		Ticket ticket = ApplicationTestFixtures.ticket(EstadoTicket.ASIGNADO, ApplicationTestFixtures.AHORA.plus(Duration.ofHours(8)));
		when(ticketRepository.buscarPorId(10L)).thenReturn(Optional.of(ticket));

		Actor otroTecnico = new Actor(999L, Rol.ROLE_TECNICO);
		assertThrows(AccesoNoPermitidoException.class, () ->
				service.ejecutar(new RegistrarObservacionCommand(10L, "Nota ajena", otroTecnico)));
	}

	@Test
	void ticketCerradoRechazaObservacion() {
		Ticket ticket = ApplicationTestFixtures.ticket(EstadoTicket.CERRADO, ApplicationTestFixtures.AHORA.plus(Duration.ofHours(8)));
		when(ticketRepository.buscarPorId(10L)).thenReturn(Optional.of(ticket));

		assertThrows(TransicionInvalidaException.class, () ->
				service.ejecutar(new RegistrarObservacionCommand(10L, "Nota tarde", ApplicationTestFixtures.adminActor())));
	}

	@Test
	void ticketCanceladoRechazaObservacion() {
		Ticket ticket = ApplicationTestFixtures.ticket(EstadoTicket.CANCELADO, ApplicationTestFixtures.AHORA.plus(Duration.ofHours(8)));
		when(ticketRepository.buscarPorId(10L)).thenReturn(Optional.of(ticket));

		assertThrows(TransicionInvalidaException.class, () ->
				service.ejecutar(new RegistrarObservacionCommand(10L, "Nota tarde", ApplicationTestFixtures.adminActor())));
	}

	@Test
	void ticketNoEncontradoLanzaExcepcion() {
		when(ticketRepository.buscarPorId(999L)).thenReturn(Optional.empty());

		assertThrows(TicketNoEncontradoException.class, () ->
				service.ejecutar(new RegistrarObservacionCommand(999L, "Nota", ApplicationTestFixtures.adminActor())));
	}
}

