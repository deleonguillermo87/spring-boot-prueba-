package com.helpflow.supportticket.application.usecase;

import java.util.Objects;

import com.helpflow.supportticket.application.dto.Actor;
import com.helpflow.supportticket.application.dto.RegistrarObservacionCommand;
import com.helpflow.supportticket.application.port.in.RegistrarObservacionUseCase;
import com.helpflow.supportticket.application.port.out.TicketRepositoryPort;
import com.helpflow.supportticket.domain.exception.AccesoNoPermitidoException;
import com.helpflow.supportticket.domain.exception.TicketNoEncontradoException;
import com.helpflow.supportticket.domain.exception.TransicionInvalidaException;
import com.helpflow.supportticket.domain.model.EstadoTicket;
import com.helpflow.supportticket.domain.model.Rol;
import com.helpflow.supportticket.domain.model.Ticket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrarObservacionService implements RegistrarObservacionUseCase {

	private final TicketRepositoryPort ticketRepository;

	public RegistrarObservacionService(TicketRepositoryPort ticketRepository) {
		this.ticketRepository = ticketRepository;
	}

	@Override
	@Transactional
	public Ticket ejecutar(RegistrarObservacionCommand command) {
		Objects.requireNonNull(command, "command es obligatorio");
		Actor actor = Objects.requireNonNull(command.actor(), "actor es obligatorio");
		Ticket ticket = ticketRepository.buscarPorId(command.ticketId())
				.orElseThrow(() -> new TicketNoEncontradoException("No existe el ticket " + command.ticketId()));

		if (ticket.getEstado() == EstadoTicket.CERRADO || ticket.getEstado() == EstadoTicket.CANCELADO) {
			throw new TransicionInvalidaException("No se pueden agregar observaciones a un ticket cerrado o cancelado.");
		}

		autorizar(ticket, actor);

		ticket.registrarObservacion(command.observacion(), String.valueOf(actor.id()));
		return ticketRepository.guardar(ticket);
	}

	private void autorizar(Ticket ticket, Actor actor) {
		if (actor.rol() == Rol.ROLE_ADMIN) {
			return;
		}
		if (actor.rol() == Rol.ROLE_TECNICO
				&& ticket.getTecnicoAsignado() != null
				&& Objects.equals(ticket.getTecnicoAsignado().getUsuarioId(), actor.id())) {
			return;
		}
		if (actor.rol() == Rol.ROLE_USUARIO
				&& Objects.equals(ticket.getSolicitante().getId(), actor.id())) {
			return;
		}
		throw new AccesoNoPermitidoException("El actor no tiene permiso para agregar observaciones a este ticket.");
	}
}

