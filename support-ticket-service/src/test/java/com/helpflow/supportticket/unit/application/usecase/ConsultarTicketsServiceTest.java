package com.helpflow.supportticket.unit.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import com.helpflow.supportticket.application.dto.Actor;
import com.helpflow.supportticket.application.dto.ConsultarTicketsCommand;
import com.helpflow.supportticket.application.dto.FiltroTickets;
import com.helpflow.supportticket.application.dto.PaginaResultado;
import com.helpflow.supportticket.application.dto.PaginaSolicitud;
import com.helpflow.supportticket.application.port.out.MetricasPort;
import com.helpflow.supportticket.application.port.out.TicketRepositoryPort;
import com.helpflow.supportticket.application.usecase.ConsultarTicketsService;
import com.helpflow.supportticket.domain.exception.AccesoNoPermitidoException;
import com.helpflow.supportticket.domain.model.EstadoTicket;
import com.helpflow.supportticket.domain.model.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsultarTicketsServiceTest {

	@Mock TicketRepositoryPort ticketRepository;
	@Mock MetricasPort metricas;
	private ConsultarTicketsService service;

	@BeforeEach
	void setUp() {
		service = new ConsultarTicketsService(ticketRepository, metricas);
	}

	@Test
	void filtersTicketsForEachRole() {
		Ticket ticket = ApplicationTestFixtures.ticket(
				EstadoTicket.ASIGNADO, ApplicationTestFixtures.AHORA.plus(Duration.ofHours(8)));
		PaginaSolicitud pagina = new PaginaSolicitud(0, 20, "prioridad", false);
		FiltroTickets filtros = new FiltroTickets(EstadoTicket.ASIGNADO, null, null);
		PaginaResultado<Ticket> resultado = resultado(List.of(ticket), 0, 20, 45, 3);
		when(ticketRepository.buscarPorSolicitante(1L, pagina, filtros)).thenReturn(resultado);
		when(ticketRepository.buscarPorTecnico(14L, pagina, filtros)).thenReturn(resultado);
		when(ticketRepository.buscarTodos(pagina, filtros)).thenReturn(resultado);

		assertEquals(ticket.getId(), service.ejecutar(new ConsultarTicketsCommand(
				ApplicationTestFixtures.usuarioActor(), pagina, filtros)).contenido().getFirst().id());
		assertEquals(ticket.getId(), service.ejecutar(new ConsultarTicketsCommand(
				ApplicationTestFixtures.tecnicoActor(), pagina, filtros)).contenido().getFirst().id());
		assertEquals(ticket.getId(), service.ejecutar(new ConsultarTicketsCommand(
				ApplicationTestFixtures.adminActor(), pagina, filtros)).contenido().getFirst().id());

		verify(ticketRepository).buscarPorSolicitante(1L, pagina, filtros);
		verify(ticketRepository).buscarPorTecnico(14L, pagina, filtros);
		verify(ticketRepository).buscarTodos(pagina, filtros);
	}

	@Test
	void clampsRequestedPageSizeToMaximumBeforeQuerying() {
		PaginaSolicitud pagina = new PaginaSolicitud(-1, 500, null, false);
		assertEquals(0, pagina.pagina());
		assertEquals(100, pagina.tamano());
		assertEquals("fechaCreacion", pagina.ordenarPor());
		assertEquals(10, new PaginaSolicitud().tamano());
		assertTrue(new PaginaSolicitud().descendente());

		when(ticketRepository.buscarTodos(pagina, FiltroTickets.todos()))
				.thenReturn(resultado(List.of(), 0, 100, 0, 0));

		assertEquals(100, service.ejecutar(new ConsultarTicketsCommand(
				ApplicationTestFixtures.adminActor(), pagina, FiltroTickets.todos())).tamano());
		verify(ticketRepository).buscarTodos(pagina, FiltroTickets.todos());
	}

	@Test
	void outOfRangePageReturnsEmptyContent() {
		PaginaSolicitud pagina = new PaginaSolicitud(4, 10, "fechaCreacion", true);
		when(ticketRepository.buscarPorSolicitante(1L, pagina, FiltroTickets.todos()))
				.thenReturn(resultado(List.of(), 4, 10, 3, 1));

		var resultado = service.ejecutar(new ConsultarTicketsCommand(
				ApplicationTestFixtures.usuarioActor(), pagina, FiltroTickets.todos()));

		assertEquals(List.of(), resultado.contenido());
		assertEquals(4, resultado.pagina());
		assertEquals(3, resultado.totalElementos());
	}

	@Test
	void deniesAccessToTicketOwnedByAnotherUserAndRecordsMetric() {
		Ticket ticket = ApplicationTestFixtures.ticket(
				EstadoTicket.ABIERTO, ApplicationTestFixtures.AHORA.plus(Duration.ofHours(8)));
		when(ticketRepository.buscarPorId(10L)).thenReturn(Optional.of(ticket));

		assertThrows(AccesoNoPermitidoException.class, () -> service.ejecutar(
				new ConsultarTicketsCommand(10L, new Actor(2L,
						com.helpflow.supportticket.domain.model.Rol.ROLE_USUARIO))));

		verify(metricas).accesoDenegado();
	}

	private PaginaResultado<Ticket> resultado(
			List<Ticket> contenido,
			int pagina,
			int tamano,
			long totalElementos,
			int totalPaginas) {
		return new PaginaResultado<>(
				contenido,
				pagina,
				tamano,
				totalElementos,
				totalPaginas,
				pagina == 0,
				pagina >= totalPaginas - 1);
	}
}
