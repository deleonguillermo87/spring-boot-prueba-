package com.helpflow.supportticket.unit.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.helpflow.supportticket.application.dto.EscalarTicketsCommand;
import com.helpflow.supportticket.application.port.out.AsignacionRepositoryPort;
import com.helpflow.supportticket.application.port.out.DispatchUnavailableException;
import com.helpflow.supportticket.application.port.out.MetricasPort;
import com.helpflow.supportticket.application.port.out.TechnicianDispatchPort;
import com.helpflow.supportticket.application.port.out.TecnicoRepositoryPort;
import com.helpflow.supportticket.application.port.out.TicketRepositoryPort;
import com.helpflow.supportticket.application.usecase.EscalarTicketsService;
import com.helpflow.supportticket.domain.model.EstadoTicket;
import com.helpflow.supportticket.domain.model.Prioridad;
import com.helpflow.supportticket.domain.model.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EscalarTicketsServiceTest {

	@Mock TicketRepositoryPort ticketRepository;
	@Mock TecnicoRepositoryPort tecnicoRepository;
	@Mock AsignacionRepositoryPort asignacionRepository;
	@Mock TechnicianDispatchPort dispatchPort;
	@Mock MetricasPort metricas;
	private EscalarTicketsService service;

	@BeforeEach
	void setUp() {
		service = new EscalarTicketsService(
				ticketRepository, tecnicoRepository, asignacionRepository,
				dispatchPort, metricas);
	}

	@Test
	void escalatesOnlyUpcomingTicketsAndRecordsHistory() {
		Instant ahora = Instant.now();
		Ticket upcoming = ApplicationTestFixtures.ticket(
				EstadoTicket.ASIGNADO, ahora.plus(Duration.ofHours(1)));
		Ticket notUpcoming = ApplicationTestFixtures.ticket(
				EstadoTicket.ASIGNADO, ahora.plus(Duration.ofHours(6)));
		Ticket expired = ApplicationTestFixtures.ticket(
				EstadoTicket.ASIGNADO, ahora.minusSeconds(1));
		when(ticketRepository.buscarActivosProximosAVencer(any()))
				.thenReturn(List.of(notUpcoming, upcoming, expired));
		when(dispatchPort.despachar(upcoming.getId(), upcoming.getCategoria(), Prioridad.CRITICA))
				.thenThrow(new DispatchUnavailableException("Connection refused"));
		when(ticketRepository.guardar(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

		int escalados = service.ejecutar(new EscalarTicketsCommand(
				Duration.ofHours(2), ApplicationTestFixtures.adminActor()));

		assertEquals(1, escalados);
		assertEquals(2, upcoming.getHistorial().size());
		assertTrue(upcoming.getHistorial().getFirst().observacion().contains("Escalamiento automático"));
		assertEquals("SISTEMA", upcoming.getHistorial().getFirst().autor());
		assertEquals(EstadoTicket.ASIGNADO, notUpcoming.getEstado());
		verify(ticketRepository).guardar(upcoming);
		verify(ticketRepository, never()).guardar(notUpcoming);
		verify(metricas).ticketVencido();
		verify(tecnicoRepository, never()).buscarPorCodigo(any());
		verify(asignacionRepository, never()).guardar(any());
	}
}
