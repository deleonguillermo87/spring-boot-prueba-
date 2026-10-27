package com.helpflow.supportticket.infrastructure.adapter.in.rest.controller;

import java.net.URI;

import com.helpflow.supportticket.application.dto.Actor;
import com.helpflow.supportticket.application.dto.AsignarTicketCommand;
import com.helpflow.supportticket.application.dto.ConsultarTicketsCommand;
import com.helpflow.supportticket.application.dto.CrearTicketCommand;
import com.helpflow.supportticket.application.dto.FiltroTickets;
import com.helpflow.supportticket.application.dto.GestionarTicketCommand;
import com.helpflow.supportticket.application.dto.PaginaResultado;
import com.helpflow.supportticket.application.dto.PaginaSolicitud;
import com.helpflow.supportticket.application.dto.RegistrarObservacionCommand;
import com.helpflow.supportticket.application.dto.TicketResult;
import com.helpflow.supportticket.application.port.in.AsignarTicketUseCase;
import com.helpflow.supportticket.application.port.in.ConsultarTicketsUseCase;
import com.helpflow.supportticket.application.port.in.CrearTicketUseCase;
import com.helpflow.supportticket.application.port.in.GestionarTicketUseCase;
import com.helpflow.supportticket.application.port.in.RegistrarObservacionUseCase;
import com.helpflow.supportticket.domain.model.Categoria;
import com.helpflow.supportticket.domain.model.EstadoTicket;
import com.helpflow.supportticket.domain.model.Prioridad;
import com.helpflow.supportticket.domain.model.Ticket;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.dto.AgregarObservacionRequest;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.dto.AsignarTicketRequest;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.dto.CambiarEstadoRequest;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.dto.CrearTicketRequest;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.dto.PaginaResponse;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.dto.TicketResponse;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.mapper.TicketRestMapper;
import com.helpflow.supportticket.infrastructure.security.ActorResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/tickets")
@Tag(name = "Tickets", description = "Operaciones sobre tickets de soporte")
public class TicketController {

	private final CrearTicketUseCase crearTicketUseCase;
	private final ConsultarTicketsUseCase consultarTicketsUseCase;
	private final GestionarTicketUseCase gestionarTicketUseCase;
	private final RegistrarObservacionUseCase registrarObservacionUseCase;
	private final AsignarTicketUseCase asignarTicketUseCase;
	private final ActorResolver actorResolver;
	private final TicketRestMapper ticketRestMapper;

	public TicketController(
			CrearTicketUseCase crearTicketUseCase,
			ConsultarTicketsUseCase consultarTicketsUseCase,
			GestionarTicketUseCase gestionarTicketUseCase,
			RegistrarObservacionUseCase registrarObservacionUseCase,
			AsignarTicketUseCase asignarTicketUseCase,
			ActorResolver actorResolver,
			TicketRestMapper ticketRestMapper) {
		this.crearTicketUseCase = crearTicketUseCase;
		this.consultarTicketsUseCase = consultarTicketsUseCase;
		this.gestionarTicketUseCase = gestionarTicketUseCase;
		this.registrarObservacionUseCase = registrarObservacionUseCase;
		this.asignarTicketUseCase = asignarTicketUseCase;
		this.actorResolver = actorResolver;
		this.ticketRestMapper = ticketRestMapper;
	}

	@PostMapping
	@PreAuthorize("hasAuthority('ROLE_USUARIO')")
	@Operation(summary = "Crear ticket", description = "Registra un nuevo ticket de soporte y despacha a técnico si hay disponibilidad")
	public ResponseEntity<TicketResponse> crear(
			@Valid @RequestBody CrearTicketRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		Actor actor = actorResolver.resolver(jwt);
		CrearTicketCommand command = new CrearTicketCommand(
				actor.id(),
				request.titulo(),
				request.descripcion(),
				request.categoria(),
				request.prioridad(),
				request.sede(),
				request.activoAfectado(),
				actor);
		Ticket ticket = crearTicketUseCase.ejecutar(command);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(ticket.getId())
				.toUri();
		return ResponseEntity.created(location).body(ticketRestMapper.toResponse(ticket));
	}

	@GetMapping
	@PreAuthorize("hasAnyAuthority('ROLE_USUARIO', 'ROLE_TECNICO', 'ROLE_ADMIN')")
	@Operation(summary = "Listar tickets", description = "Consulta paginada de tickets con filtros opcionales de estado, categoría y prioridad")
	public PaginaResponse<TicketResponse> listar(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "fechaCreacion") String sort,
			@RequestParam(defaultValue = "desc") String dir,
			@RequestParam(required = false) EstadoTicket estado,
			@RequestParam(required = false) Categoria categoria,
			@RequestParam(required = false) Prioridad prioridad,
			@AuthenticationPrincipal Jwt jwt) {
		Actor actor = actorResolver.resolver(jwt);
		PaginaSolicitud pagina = new PaginaSolicitud(page, size, sort, !"asc".equalsIgnoreCase(dir));
		FiltroTickets filtros = new FiltroTickets(estado, categoria, prioridad);
		ConsultarTicketsCommand command = new ConsultarTicketsCommand(actor, pagina, filtros);
		PaginaResultado<TicketResult> resultado = consultarTicketsUseCase.ejecutar(command);
		return PaginaResponse.desde(resultado, ticketRestMapper::toResponse);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('ROLE_USUARIO', 'ROLE_TECNICO', 'ROLE_ADMIN')")
	@Operation(summary = "Consultar ticket por id", description = "Obtiene los detalles del ticket indicado si el actor tiene acceso")
	public TicketResponse obtenerPorId(
			@PathVariable Long id,
			@AuthenticationPrincipal Jwt jwt) {
		Actor actor = actorResolver.resolver(jwt);
		ConsultarTicketsCommand command = new ConsultarTicketsCommand(id, actor);
		PaginaResultado<TicketResult> resultado = consultarTicketsUseCase.ejecutar(command);
		return ticketRestMapper.toResponse(resultado.contenido().getFirst());
	}

	@PatchMapping("/{id}/estado")
	@PreAuthorize("hasAnyAuthority('ROLE_USUARIO', 'ROLE_TECNICO', 'ROLE_ADMIN')")
	@Operation(summary = "Cambiar estado del ticket", description = "Transiciona el estado del ticket con observación opcional u obligatoria")
	public TicketResponse cambiarEstado(
			@PathVariable Long id,
			@Valid @RequestBody CambiarEstadoRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		Actor actor = actorResolver.resolver(jwt);
		GestionarTicketCommand command = new GestionarTicketCommand(id, request.estado(), request.observacion(), actor);
		Ticket ticket = gestionarTicketUseCase.ejecutar(command);
		return ticketRestMapper.toResponse(ticket);
	}

	@PostMapping("/{id}/observaciones")
	@PreAuthorize("hasAnyAuthority('ROLE_USUARIO', 'ROLE_TECNICO', 'ROLE_ADMIN')")
	@Operation(summary = "Registrar observación", description = "Agrega una nueva observación al historial del ticket")
	public TicketResponse agregarObservacion(
			@PathVariable Long id,
			@Valid @RequestBody AgregarObservacionRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		Actor actor = actorResolver.resolver(jwt);
		RegistrarObservacionCommand command = new RegistrarObservacionCommand(id, request.observacion(), actor);
		Ticket ticket = registrarObservacionUseCase.ejecutar(command);
		return ticketRestMapper.toResponse(ticket);
	}

	@PutMapping("/{id}/asignacion")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	@Operation(summary = "Asignar o reasignar ticket", description = "Asigna o reasigna manualmente un ticket a un técnico")
	public TicketResponse asignar(
			@PathVariable Long id,
			@Valid @RequestBody AsignarTicketRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		Actor actor = actorResolver.resolver(jwt);
		AsignarTicketCommand command = new AsignarTicketCommand(id, request.codigoTecnico(), request.observacion(), actor);
		Ticket ticket = asignarTicketUseCase.ejecutar(command);
		return ticketRestMapper.toResponse(ticket);
	}
}

