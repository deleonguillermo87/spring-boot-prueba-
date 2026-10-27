package com.helpflow.supportticket.application.usecase;

import java.util.List;
import java.util.Objects;

import com.helpflow.supportticket.application.dto.Actor;
import com.helpflow.supportticket.application.dto.ConsultarTicketsCommand;
import com.helpflow.supportticket.application.dto.PaginaResultado;
import com.helpflow.supportticket.application.dto.TicketResult;
import com.helpflow.supportticket.application.port.in.ConsultarTicketsUseCase;
import com.helpflow.supportticket.application.port.out.MetricasPort;
import com.helpflow.supportticket.application.port.out.TicketRepositoryPort;
import com.helpflow.supportticket.domain.exception.AccesoNoPermitidoException;
import com.helpflow.supportticket.domain.exception.TicketNoEncontradoException;
import com.helpflow.supportticket.domain.model.Ticket;
import org.springframework.stereotype.Service;

@Service
public class ConsultarTicketsService implements ConsultarTicketsUseCase {

	private final TicketRepositoryPort ticketRepository;
	private final MetricasPort metricas;

	public ConsultarTicketsService(TicketRepositoryPort ticketRepository, MetricasPort metricas) {
		this.ticketRepository = ticketRepository;
		this.metricas = metricas;
	}

	@Override
	public PaginaResultado<TicketResult> ejecutar(ConsultarTicketsCommand command) {
		Objects.requireNonNull(command, "command es obligatorio");
		Actor actor = Objects.requireNonNull(command.actor(), "actor es obligatorio");
		if (command.ticketId() != null) {
			Ticket ticket = ticketRepository.buscarPorId(command.ticketId())
					.orElseThrow(() -> new TicketNoEncontradoException("No existe el ticket " + command.ticketId()));
			if (!puedeVer(ticket, actor)) {
				metricas.accesoDenegado();
				throw new AccesoNoPermitidoException("No tiene acceso al ticket solicitado.");
			}
			boolean coincide = (command.filtros().estado() == null
					|| command.filtros().estado() == ticket.getEstado())
					&& (command.filtros().categoria() == null
					|| command.filtros().categoria() == ticket.getCategoria())
					&& (command.filtros().prioridad() == null
					|| command.filtros().prioridad() == ticket.getPrioridad());
			boolean primera = command.pagina().pagina() == 0;
			boolean contieneTicket = primera && coincide;
			return new PaginaResultado<>(
					contieneTicket ? List.of(TicketResult.desde(ticket)) : List.of(),
					command.pagina().pagina(),
					command.pagina().tamano(),
					coincide ? 1 : 0,
					coincide ? 1 : 0,
					primera,
					true);
		}
		PaginaResultado<Ticket> tickets = switch (actor.rol()) {
			case ROLE_USUARIO -> ticketRepository.buscarPorSolicitante(
					actor.id(), command.pagina(), command.filtros());
			case ROLE_TECNICO -> ticketRepository.buscarPorTecnico(
					actor.id(), command.pagina(), command.filtros());
			case ROLE_ADMIN -> ticketRepository.buscarTodos(command.pagina(), command.filtros());
		};
		return PaginaResultado.map(tickets, TicketResult::desde);
	}

	private boolean puedeVer(Ticket ticket, Actor actor) {
		return switch (actor.rol()) {
			case ROLE_USUARIO -> Objects.equals(ticket.getSolicitante().getId(), actor.id());
			case ROLE_TECNICO -> ticket.getTecnicoAsignado() != null
					&& Objects.equals(ticket.getTecnicoAsignado().getUsuarioId(), actor.id());
			case ROLE_ADMIN -> true;
		};
	}
}
