package com.helpflow.supportticket.unit.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.helpflow.supportticket.application.dto.CrearTicketCommand;
import com.helpflow.supportticket.application.port.out.AsignacionRepositoryPort;
import com.helpflow.supportticket.application.port.out.DespachoSugerido;
import com.helpflow.supportticket.application.port.out.DispatchUnavailableException;
import com.helpflow.supportticket.application.port.out.MetricasPort;
import com.helpflow.supportticket.application.port.out.SlaConfigPort;
import com.helpflow.supportticket.application.port.out.TechnicianDispatchPort;
import com.helpflow.supportticket.application.port.out.TecnicoRepositoryPort;
import com.helpflow.supportticket.application.port.out.TicketRepositoryPort;
import com.helpflow.supportticket.application.port.out.UsuarioRepositoryPort;
import com.helpflow.supportticket.application.usecase.CrearTicketService;
import com.helpflow.supportticket.domain.exception.TecnicoNoDisponibleException;
import com.helpflow.supportticket.domain.exception.UsuarioInactivoException;
import com.helpflow.supportticket.domain.model.Categoria;
import com.helpflow.supportticket.domain.model.Disponibilidad;
import com.helpflow.supportticket.domain.model.EstadoTicket;
import com.helpflow.supportticket.domain.model.EstadoUsuario;
import com.helpflow.supportticket.domain.model.Prioridad;
import com.helpflow.supportticket.domain.model.Tecnico;
import com.helpflow.supportticket.domain.model.Ticket;
import com.helpflow.supportticket.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CrearTicketServiceTest {

	@Mock TicketRepositoryPort ticketRepository;
	@Mock UsuarioRepositoryPort usuarioRepository;
	@Mock TecnicoRepositoryPort tecnicoRepository;
	@Mock AsignacionRepositoryPort asignacionRepository;
	@Mock TechnicianDispatchPort dispatchPort;
	@Mock MetricasPort metricas;
	@Mock SlaConfigPort slaConfigPort;

	private CrearTicketService service;
	private Ticket guardado;

	@BeforeEach
	void setUp() {
		service = new CrearTicketService(
				ticketRepository, usuarioRepository, tecnicoRepository, asignacionRepository,
				dispatchPort, metricas, slaConfigPort);
		guardado = new Ticket(
				10L, ApplicationTestFixtures.usuario(), "Sin conexión", "Descripción",
				Categoria.RED, Prioridad.ALTA, "CENTRAL", null,
				ApplicationTestFixtures.AHORA, ApplicationTestFixtures.AHORA.plus(Duration.ofHours(8)),
				EstadoTicket.ABIERTO, null, List.of(), ApplicationTestFixtures.CLOCK);
		lenient().when(usuarioRepository.buscarPorId(1L)).thenReturn(Optional.of(ApplicationTestFixtures.usuario()));
		lenient().when(slaConfigPort.obtener()).thenReturn(ApplicationTestFixtures.SLA);
		lenient().when(ticketRepository.guardar(any(Ticket.class))).thenReturn(guardado);
	}

	@Test
	void createsTicketAssignsSuggestedTechnicianAndPersistsAssignment() {
		Tecnico tecnico = ApplicationTestFixtures.tecnicoDisponible();
		when(dispatchPort.despachar(10L, Categoria.RED, Prioridad.ALTA))
				.thenReturn(new DespachoSugerido("TEC-014", Disponibilidad.MEDIA, 4));
		when(tecnicoRepository.buscarCandidatos(Categoria.RED, "CENTRAL")).thenReturn(List.of(tecnico));
		when(tecnicoRepository.buscarPorCodigo("TEC-014")).thenReturn(Optional.of(tecnico));

		Ticket resultado = service.ejecutar(command());

		assertEquals(EstadoTicket.ASIGNADO, resultado.getEstado());
		assertEquals(tecnico, resultado.getTecnicoAsignado());
		assertEquals(1, resultado.getHistorial().size());
		verify(asignacionRepository).guardar(any());
		verify(metricas).ticketCreado(Categoria.RED, Prioridad.ALTA);
	}

	@Test
	void rejectsInactiveRequester() {
		when(usuarioRepository.buscarPorId(1L))
				.thenReturn(Optional.of(ApplicationTestFixtures.usuario(1L, EstadoUsuario.INACTIVO)));

		assertThrows(UsuarioInactivoException.class, () -> service.ejecutar(command()));
		verify(ticketRepository, never()).guardar(any());
	}

	@Test
	void dispatchFailureKeepsOpenTicketAndRecordsObservation() {
		when(dispatchPort.despachar(10L, Categoria.RED, Prioridad.ALTA))
				.thenThrow(new DispatchUnavailableException("Connection refused"));

		Ticket resultado = service.ejecutar(command());

		assertEquals(EstadoTicket.ABIERTO, resultado.getEstado());
		assertNull(resultado.getTecnicoAsignado());
		assertEquals(1, resultado.getHistorial().size());
		assertTrue(resultado.getHistorial().getFirst().observacion().toLowerCase().contains("dispatch"));
		verify(asignacionRepository, never()).guardar(any());
		verify(metricas).ticketCreado(Categoria.RED, Prioridad.ALTA);
	}

	@Test
	void selectsAlternativeWhenSuggestedTechnicianHasNoCapacity() {
		Tecnico sugerido = ApplicationTestFixtures.tecnico(14L, "TEC-014", 1, 1, EstadoUsuario.ACTIVO);
		Tecnico alternativo = ApplicationTestFixtures.tecnico(15L, "TEC-015", 5, 1, EstadoUsuario.ACTIVO);
		when(dispatchPort.despachar(10L, Categoria.RED, Prioridad.ALTA))
				.thenReturn(new DespachoSugerido("TEC-014", Disponibilidad.ALTA, 2));
		when(tecnicoRepository.buscarPorCodigo("TEC-014")).thenReturn(Optional.of(sugerido));
		when(tecnicoRepository.buscarCandidatos(Categoria.RED, "CENTRAL"))
				.thenReturn(List.of(sugerido, alternativo));

		Ticket resultado = service.ejecutar(command());

		assertEquals(alternativo, resultado.getTecnicoAsignado());
	}

	@Test
	void failsWhenNoTechnicianIsAvailable() {
		when(dispatchPort.despachar(10L, Categoria.RED, Prioridad.ALTA))
				.thenReturn(new DespachoSugerido("TEC-MISSING", Disponibilidad.ALTA, 2));
		when(tecnicoRepository.buscarPorCodigo("TEC-MISSING")).thenReturn(Optional.empty());
		when(tecnicoRepository.buscarCandidatos(Categoria.RED, "CENTRAL")).thenReturn(List.of());

		assertThrows(TecnicoNoDisponibleException.class, () -> service.ejecutar(command()));
	}

	private CrearTicketCommand command() {
		return new CrearTicketCommand(
				1L, "Sin conexión", "Descripción", Categoria.RED, Prioridad.ALTA,
				"CENTRAL", null, ApplicationTestFixtures.usuarioActor());
	}
}
