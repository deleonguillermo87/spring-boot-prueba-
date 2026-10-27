package com.helpflow.supportticket.application.usecase;

import java.util.ArrayList;
import java.util.Objects;

import com.helpflow.supportticket.application.dto.AsignarTicketCommand;
import com.helpflow.supportticket.application.port.in.AsignarTicketUseCase;
import com.helpflow.supportticket.application.port.out.TecnicoRepositoryPort;
import com.helpflow.supportticket.application.port.out.TicketRepositoryPort;
import com.helpflow.supportticket.domain.exception.AccesoNoPermitidoException;
import com.helpflow.supportticket.domain.exception.TicketNoEncontradoException;
import com.helpflow.supportticket.domain.exception.TransicionInvalidaException;
import com.helpflow.supportticket.domain.model.EstadoTicket;
import com.helpflow.supportticket.domain.model.Rol;
import com.helpflow.supportticket.domain.model.Tecnico;
import com.helpflow.supportticket.domain.model.Ticket;
import com.helpflow.supportticket.domain.service.AsignacionPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AsignarTicketService implements AsignarTicketUseCase {

	private final TicketRepositoryPort ticketRepository;
	private final TecnicoRepositoryPort tecnicoRepository;
	private final AsignacionPolicy asignacionPolicy = new AsignacionPolicy();

	public AsignarTicketService(
			TicketRepositoryPort ticketRepository,
			TecnicoRepositoryPort tecnicoRepository) {
		this.ticketRepository = ticketRepository;
		this.tecnicoRepository = tecnicoRepository;
	}

	@Override
	@Transactional
	public Ticket ejecutar(AsignarTicketCommand command) {
		Objects.requireNonNull(command, "command es obligatorio");
		if (command.actor() == null || command.actor().rol() != Rol.ROLE_ADMIN) {
			throw new AccesoNoPermitidoException("Solo un administrador puede asignar o reasignar tickets.");
		}
		Ticket ticket = ticketRepository.buscarPorId(command.ticketId())
				.orElseThrow(() -> new TicketNoEncontradoException("No existe el ticket " + command.ticketId()));
		Tecnico sugerido = tecnicoRepository.buscarPorCodigo(command.tecnicoCodigo()).orElse(null);
		var candidatos = new ArrayList<>(tecnicoRepository.buscarCandidatos(ticket.getCategoria(), ticket.getSede()));
		Tecnico seleccionado = asignacionPolicy.seleccionarTecnico(ticket, candidatos, sugerido);

		if (ticket.getEstado() == EstadoTicket.ABIERTO) {
			ticket.asignar(seleccionado, command.observacion());
		} else {
			if (ticket.getEstado() != EstadoTicket.ASIGNADO && ticket.getEstado() != EstadoTicket.EN_PROGRESO) {
				throw new TransicionInvalidaException("El ticket no está en un estado reasignable.");
			}
			ticket.reasignar(seleccionado, command.observacion(), String.valueOf(command.actor().id()));
		}
		return ticketRepository.guardar(ticket);
	}
}
