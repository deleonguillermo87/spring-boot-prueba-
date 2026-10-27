package com.helpflow.supportticket.unit.rest;

import java.time.Instant;
import java.util.List;

import com.helpflow.supportticket.application.dto.Actor;
import com.helpflow.supportticket.application.dto.AsignarTicketCommand;
import com.helpflow.supportticket.application.dto.ConsultarTicketsCommand;
import com.helpflow.supportticket.application.dto.CrearTicketCommand;
import com.helpflow.supportticket.application.dto.GestionarTicketCommand;
import com.helpflow.supportticket.application.dto.PaginaResultado;
import com.helpflow.supportticket.application.dto.RegistrarObservacionCommand;
import com.helpflow.supportticket.application.dto.TicketResult;
import com.helpflow.supportticket.application.port.in.AsignarTicketUseCase;
import com.helpflow.supportticket.application.port.in.ConsultarTicketsUseCase;
import com.helpflow.supportticket.application.port.in.CrearTicketUseCase;
import com.helpflow.supportticket.application.port.in.GestionarTicketUseCase;
import com.helpflow.supportticket.application.port.in.RegistrarObservacionUseCase;
import com.helpflow.supportticket.domain.model.Categoria;
import com.helpflow.supportticket.domain.model.EstadoTicket;
import com.helpflow.supportticket.domain.model.EstadoUsuario;
import com.helpflow.supportticket.domain.model.Prioridad;
import com.helpflow.supportticket.domain.model.Rol;
import com.helpflow.supportticket.domain.model.Ticket;
import com.helpflow.supportticket.domain.model.Usuario;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.controller.TicketController;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.dto.AgregarObservacionRequest;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.dto.AsignarTicketRequest;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.dto.CambiarEstadoRequest;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.dto.CrearTicketRequest;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.dto.PaginaResponse;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.dto.TicketResponse;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.mapper.TicketRestMapper;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.mapper.TicketRestMapperImpl;
import com.helpflow.supportticket.infrastructure.security.ActorResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketControllerTest {

	@Mock
	private CrearTicketUseCase crearTicketUseCase;
	@Mock
	private ConsultarTicketsUseCase consultarTicketsUseCase;
	@Mock
	private GestionarTicketUseCase gestionarTicketUseCase;
	@Mock
	private RegistrarObservacionUseCase registrarObservacionUseCase;
	@Mock
	private AsignarTicketUseCase asignarTicketUseCase;
	@Mock
	private ActorResolver actorResolver;

	private final TicketRestMapper ticketRestMapper = new TicketRestMapperImpl();
	private TicketController controller;
	private Jwt jwt;
	private Actor usuarioActor;
	private Actor adminActor;

	@BeforeEach
	void setUp() {
		controller = new TicketController(
				crearTicketUseCase,
				consultarTicketsUseCase,
				gestionarTicketUseCase,
				registrarObservacionUseCase,
				asignarTicketUseCase,
				actorResolver,
				ticketRestMapper);

		jwt = Jwt.withTokenValue("mock-token")
				.header("alg", "HS256")
				.subject("1")
				.claim("rol", "ROLE_USUARIO")
				.build();

		usuarioActor = new Actor(1L, Rol.ROLE_USUARIO);
		adminActor = new Actor(99L, Rol.ROLE_ADMIN);

		MockHttpServletRequest request = new MockHttpServletRequest();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
	}

	@Test
	void crearTicketRetorna201ConLocation() {
		when(actorResolver.resolver(jwt)).thenReturn(usuarioActor);
		Ticket ticket = ticketSample(10L, EstadoTicket.ABIERTO);
		when(crearTicketUseCase.ejecutar(any(CrearTicketCommand.class))).thenReturn(ticket);

		CrearTicketRequest request = new CrearTicketRequest(
				"Sin internet", "Caída del switch", Categoria.RED, Prioridad.ALTA, "CENTRAL", "SW-01");

		ResponseEntity<TicketResponse> response = controller.crear(request, jwt);

		assertEquals(201, response.getStatusCode().value());
		assertNotNull(response.getHeaders().getLocation());
		assertNotNull(response.getBody());
		assertEquals(10L, response.getBody().id());
		verify(crearTicketUseCase).ejecutar(any(CrearTicketCommand.class));
	}

	@Test
	void listarTicketsRetornaPaginaResponse() {
		when(actorResolver.resolver(jwt)).thenReturn(usuarioActor);
		TicketResult result = new TicketResult(
				10L, 1L, "Sin internet", "Caída", Categoria.RED, Prioridad.ALTA,
				"CENTRAL", "SW-01", Instant.now(), Instant.now().plusSeconds(3600),
				EstadoTicket.ABIERTO, null, null);
		PaginaResultado<TicketResult> pagina = new PaginaResultado<>(List.of(result), 0, 10, 1, 1, true, true);
		when(consultarTicketsUseCase.ejecutar(any(ConsultarTicketsCommand.class))).thenReturn(pagina);

		PaginaResponse<TicketResponse> response = controller.listar(0, 10, "fechaCreacion", "desc", null, null, null, jwt);

		assertNotNull(response);
		assertEquals(1, response.totalElementos());
		assertEquals(1, response.contenido().size());
		assertEquals(10L, response.contenido().getFirst().id());
	}

	@Test
	void obtenerPorIdRetornaTicket() {
		when(actorResolver.resolver(jwt)).thenReturn(usuarioActor);
		TicketResult result = new TicketResult(
				10L, 1L, "Sin internet", "Caída", Categoria.RED, Prioridad.ALTA,
				"CENTRAL", "SW-01", Instant.now(), Instant.now().plusSeconds(3600),
				EstadoTicket.ABIERTO, null, null);
		PaginaResultado<TicketResult> pagina = new PaginaResultado<>(List.of(result), 0, 10, 1, 1, true, true);
		when(consultarTicketsUseCase.ejecutar(any(ConsultarTicketsCommand.class))).thenReturn(pagina);

		TicketResponse response = controller.obtenerPorId(10L, jwt);

		assertNotNull(response);
		assertEquals(10L, response.id());
	}

	@Test
	void cambiarEstadoEjecutaCasoDeUso() {
		when(actorResolver.resolver(jwt)).thenReturn(usuarioActor);
		Ticket ticket = ticketSample(10L, EstadoTicket.CANCELADO);
		when(gestionarTicketUseCase.ejecutar(any(GestionarTicketCommand.class))).thenReturn(ticket);

		CambiarEstadoRequest request = new CambiarEstadoRequest(EstadoTicket.CANCELADO, "Ya no es necesario");
		TicketResponse response = controller.cambiarEstado(10L, request, jwt);

		assertNotNull(response);
		assertEquals("CANCELADO", response.estado());
		verify(gestionarTicketUseCase).ejecutar(any(GestionarTicketCommand.class));
	}

	@Test
	void agregarObservacionEjecutaCasoDeUso() {
		when(actorResolver.resolver(jwt)).thenReturn(usuarioActor);
		Ticket ticket = ticketSample(10L, EstadoTicket.ABIERTO);
		when(registrarObservacionUseCase.ejecutar(any(RegistrarObservacionCommand.class))).thenReturn(ticket);

		AgregarObservacionRequest request = new AgregarObservacionRequest("Aclaración de la falla");
		TicketResponse response = controller.agregarObservacion(10L, request, jwt);

		assertNotNull(response);
		verify(registrarObservacionUseCase).ejecutar(any(RegistrarObservacionCommand.class));
	}

	@Test
	void asignarTicketEjecutaCasoDeUso() {
		when(actorResolver.resolver(jwt)).thenReturn(adminActor);
		Ticket ticket = ticketSample(10L, EstadoTicket.ASIGNADO);
		when(asignarTicketUseCase.ejecutar(any(AsignarTicketCommand.class))).thenReturn(ticket);

		AsignarTicketRequest request = new AsignarTicketRequest("TEC-001", "Asignación manual por admin");
		TicketResponse response = controller.asignar(10L, request, jwt);

		assertNotNull(response);
		verify(asignarTicketUseCase).ejecutar(any(AsignarTicketCommand.class));
	}

	private Ticket ticketSample(Long id, EstadoTicket estado) {
		Usuario usuario = new Usuario(1L, "DOC-1", "Usuario", "user@helpflow.com", "CENTRAL", "TI", EstadoUsuario.ACTIVO, Rol.ROLE_USUARIO, "hash");
		return new Ticket(
				id, usuario, "Sin internet", "Caída", Categoria.RED, Prioridad.ALTA, "CENTRAL", "SW-01",
				Instant.now(), Instant.now().plusSeconds(3600), estado, null, List.of(), java.time.Clock.systemUTC());
	}
}

