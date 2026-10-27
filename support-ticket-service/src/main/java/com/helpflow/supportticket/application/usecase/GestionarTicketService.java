package com.helpflow.supportticket.application.usecase;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;

import com.helpflow.supportticket.application.dto.Actor;
import com.helpflow.supportticket.application.dto.GestionarTicketCommand;
import com.helpflow.supportticket.application.port.in.GestionarTicketUseCase;
import com.helpflow.supportticket.application.port.out.MetricasPort;
import com.helpflow.supportticket.application.port.out.TicketRepositoryPort;
import com.helpflow.supportticket.domain.exception.AccesoNoPermitidoException;
import com.helpflow.supportticket.domain.exception.TicketNoEncontradoException;
import com.helpflow.supportticket.domain.model.EstadoTicket;
import com.helpflow.supportticket.domain.model.Rol;
import com.helpflow.supportticket.domain.model.Ticket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GestionarTicketService implements GestionarTicketUseCase {

	private final TicketRepositoryPort ticketRepository;
	private final MetricasPort metricas;
	private final Clock clock = Clock.systemUTC();

	public GestionarTicketService(TicketRepositoryPort ticketRepository, MetricasPort metricas) {
		this.ticketRepository = ticketRepository;
		this.metricas = metricas;
	}

	@Override
	@Transactional
	public Ticket ejecutar(GestionarTicketCommand command) {
		Objects.requireNonNull(command, "command es obligatorio");
		Actor actor = Objects.requireNonNull(command.actor(), "actor es obligatorio");
		Ticket ticket = ticketRepository.buscarPorId(command.ticketId())
				.orElseThrow(() -> new TicketNoEncontradoException("No existe el ticket " + command.ticketId()));
		autorizar(ticket, command, actor);

		EstadoTicket anterior = ticket.getEstado();
		boolean primeraRespuesta = command.nuevoEstado() == EstadoTicket.EN_PROGRESO
				&& ticket.getHistorial().stream()
						.noneMatch(historial -> historial.estadoNuevo() == EstadoTicket.EN_PROGRESO);
		ticket.cambiarEstado(command.nuevoEstado(), command.observacion(), String.valueOf(actor.id()));
		if (primeraRespuesta) {
			metricas.tiempoPrimeraRespuesta(Duration.between(ticket.getFechaCreacion(), clock.instant()));
		}
		if (anterior != EstadoTicket.RESUELTO && command.nuevoEstado() == EstadoTicket.RESUELTO) {
			metricas.tiempoResolucion(Duration.between(ticket.getFechaCreacion(), clock.instant()));
		}
		return ticketRepository.guardar(ticket);
	}

	private void autorizar(Ticket ticket, GestionarTicketCommand command, Actor actor) {
		if (actor.rol() == Rol.ROLE_ADMIN) {
			return;
		}
		if (actor.rol() == Rol.ROLE_TECNICO
				&& ticket.getTecnicoAsignado() != null
				&& Objects.equals(ticket.getTecnicoAsignado().getUsuarioId(), actor.id())) {
			return;
		}
		if (actor.rol() == Rol.ROLE_USUARIO
				&& Objects.equals(ticket.getSolicitante().getId(), actor.id())
				&& ticket.getEstado() == EstadoTicket.ABIERTO
				&& command.nuevoEstado() == EstadoTicket.CANCELADO) {
			return;
		}
		throw new AccesoNoPermitidoException("El actor no puede gestionar este ticket.");
	}
}
