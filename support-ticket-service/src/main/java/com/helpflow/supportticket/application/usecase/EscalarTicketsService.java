package com.helpflow.supportticket.application.usecase;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Objects;

import com.helpflow.supportticket.application.dto.EscalarTicketsCommand;
import com.helpflow.supportticket.application.port.in.EscalarTicketsUseCase;
import com.helpflow.supportticket.application.port.out.AsignacionRepositoryPort;
import com.helpflow.supportticket.application.port.out.DispatchUnavailableException;
import com.helpflow.supportticket.application.port.out.MetricasPort;
import com.helpflow.supportticket.application.port.out.TechnicianDispatchPort;
import com.helpflow.supportticket.application.port.out.TecnicoRepositoryPort;
import com.helpflow.supportticket.application.port.out.TicketRepositoryPort;
import com.helpflow.supportticket.domain.exception.AccesoNoPermitidoException;
import com.helpflow.supportticket.domain.exception.TecnicoNoDisponibleException;
import com.helpflow.supportticket.domain.model.Asignacion;
import com.helpflow.supportticket.domain.model.Disponibilidad;
import com.helpflow.supportticket.domain.model.Rol;
import com.helpflow.supportticket.domain.model.Tecnico;
import com.helpflow.supportticket.domain.model.Ticket;
import com.helpflow.supportticket.domain.service.AsignacionPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EscalarTicketsService implements EscalarTicketsUseCase {

	private final TicketRepositoryPort ticketRepository;
	private final TecnicoRepositoryPort tecnicoRepository;
	private final AsignacionRepositoryPort asignacionRepository;
	private final TechnicianDispatchPort dispatchPort;
	private final MetricasPort metricas;
	private final AsignacionPolicy asignacionPolicy = new AsignacionPolicy();
	private final Clock clock = Clock.systemUTC();

	public EscalarTicketsService(
			TicketRepositoryPort ticketRepository,
			TecnicoRepositoryPort tecnicoRepository,
			AsignacionRepositoryPort asignacionRepository,
			TechnicianDispatchPort dispatchPort,
			MetricasPort metricas) {
		this.ticketRepository = ticketRepository;
		this.tecnicoRepository = tecnicoRepository;
		this.asignacionRepository = asignacionRepository;
		this.dispatchPort = dispatchPort;
		this.metricas = metricas;
	}

	@Override
	@Transactional
	public int ejecutar(EscalarTicketsCommand command) {
		Objects.requireNonNull(command, "command es obligatorio");
		if (command.actor() == null || command.actor().rol() != Rol.ROLE_ADMIN) {
			throw new AccesoNoPermitidoException("Solo un administrador puede ejecutar el escalamiento.");
		}
		Duration umbral = Objects.requireNonNull(command.umbralSla(), "umbralSla es obligatorio");
		if (umbral.isNegative()) {
			throw new IllegalArgumentException("umbralSla no puede ser negativo");
		}
		var ahora = clock.instant();
		var tickets = asignacionPolicy.priorizarTickets(
				ticketRepository.buscarActivosProximosAVencer(ahora.plus(umbral)), ahora, umbral);
		int escalados = 0;
		for (Ticket ticket : tickets) {
			if (ticket.estaVencido(ahora)) {
				metricas.ticketVencido();
				continue;
			}
			if (!ticket.estaProximoAVencer(ahora, umbral)) {
				continue;
			}
			ticket.escalar("Escalamiento automático por proximidad al SLA.");
			try {
				var sugerido = dispatchPort.despachar(ticket.getId(), ticket.getCategoria(), ticket.getPrioridad());
				var candidatos = new ArrayList<>(
						tecnicoRepository.buscarCandidatos(ticket.getCategoria(), ticket.getSede()));
				Tecnico recomendado = tecnicoRepository.buscarPorCodigo(sugerido.tecnicoCodigo()).orElse(null);
				Tecnico seleccionado = asignacionPolicy.seleccionarTecnico(ticket, candidatos, recomendado);
				if (ticket.getTecnicoAsignado() == null
						|| !ticket.getTecnicoAsignado().getCodigo().equals(seleccionado.getCodigo())) {
					if (ticket.getTecnicoAsignado() == null) {
						ticket.asignar(seleccionado, "Asignación durante escalamiento SLA.");
					} else {
						ticket.reasignar(seleccionado, "Reasignación durante escalamiento SLA.", "SISTEMA");
					}
					asignacionRepository.guardar(new Asignacion(
							null, ticket.getId(), seleccionado.getId(), clock.instant(),
							sugerido.tiempoEstimadoHoras(), sugerido.disponibilidad()));
				}
			} catch (DispatchUnavailableException | TecnicoNoDisponibleException exception) {
				ticket.registrarObservacion(
						"No fue posible reasignar durante el escalamiento: " + exception.getMessage(), "SISTEMA");
			}
			ticketRepository.guardar(ticket);
			escalados++;
		}
		return escalados;
	}
}
