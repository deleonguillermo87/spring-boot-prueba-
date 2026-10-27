package com.helpflow.supportticket.application.usecase;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.helpflow.supportticket.application.dto.Actor;
import com.helpflow.supportticket.application.dto.CrearTicketCommand;
import com.helpflow.supportticket.application.port.in.CrearTicketUseCase;
import com.helpflow.supportticket.application.port.out.AsignacionRepositoryPort;
import com.helpflow.supportticket.application.port.out.DespachoSugerido;
import com.helpflow.supportticket.application.port.out.DispatchUnavailableException;
import com.helpflow.supportticket.application.port.out.MetricasPort;
import com.helpflow.supportticket.application.port.out.SlaConfigPort;
import com.helpflow.supportticket.application.port.out.TechnicianDispatchPort;
import com.helpflow.supportticket.application.port.out.TecnicoRepositoryPort;
import com.helpflow.supportticket.application.port.out.TicketRepositoryPort;
import com.helpflow.supportticket.application.port.out.UsuarioRepositoryPort;
import com.helpflow.supportticket.domain.exception.AccesoNoPermitidoException;
import com.helpflow.supportticket.domain.exception.TecnicoNoDisponibleException;
import com.helpflow.supportticket.domain.exception.UsuarioInactivoException;
import com.helpflow.supportticket.domain.model.Asignacion;
import com.helpflow.supportticket.domain.model.Disponibilidad;
import com.helpflow.supportticket.domain.model.EstadoTicket;
import com.helpflow.supportticket.domain.model.EstadoUsuario;
import com.helpflow.supportticket.domain.model.Tecnico;
import com.helpflow.supportticket.domain.model.Ticket;
import com.helpflow.supportticket.domain.model.Usuario;
import com.helpflow.supportticket.domain.service.AsignacionPolicy;
import com.helpflow.supportticket.domain.service.SlaPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CrearTicketService implements CrearTicketUseCase {

	private final TicketRepositoryPort ticketRepository;
	private final UsuarioRepositoryPort usuarioRepository;
	private final TecnicoRepositoryPort tecnicoRepository;
	private final AsignacionRepositoryPort asignacionRepository;
	private final TechnicianDispatchPort dispatchPort;
	private final MetricasPort metricas;
	private final SlaConfigPort slaConfigPort;
	private final AsignacionPolicy asignacionPolicy = new AsignacionPolicy();
	private final Clock clock = Clock.systemUTC();

	public CrearTicketService(
			TicketRepositoryPort ticketRepository,
			UsuarioRepositoryPort usuarioRepository,
			TecnicoRepositoryPort tecnicoRepository,
			AsignacionRepositoryPort asignacionRepository,
			TechnicianDispatchPort dispatchPort,
			MetricasPort metricas,
			SlaConfigPort slaConfigPort) {
		this.ticketRepository = ticketRepository;
		this.usuarioRepository = usuarioRepository;
		this.tecnicoRepository = tecnicoRepository;
		this.asignacionRepository = asignacionRepository;
		this.dispatchPort = dispatchPort;
		this.metricas = metricas;
		this.slaConfigPort = slaConfigPort;
	}

	@Override
	@Transactional
	public Ticket ejecutar(CrearTicketCommand command) {
		Objects.requireNonNull(command, "command es obligatorio");
		Actor actor = Objects.requireNonNull(command.actor(), "actor es obligatorio");
		Usuario solicitante = usuarioRepository.buscarPorId(command.solicitanteId())
				.orElseThrow(() -> new AccesoNoPermitidoException("El solicitante no existe."));
		if (!solicitante.estaActivo()) {
			throw new UsuarioInactivoException("El solicitante está inactivo.");
		}
		if (!actor.esAdministrador() && !Objects.equals(actor.id(), solicitante.getId())) {
			throw new AccesoNoPermitidoException("No puede crear tickets para otro usuario.");
		}

		Instant ahora = clock.instant();
		Instant fechaLimiteSla = new SlaPolicy(clock, slaConfigPort.obtener())
				.calcularFechaLimiteSla(command.prioridad());
		Ticket ticket = new Ticket(
				null,
				solicitante,
				command.titulo(),
				command.descripcion(),
				command.categoria(),
				command.prioridad(),
				command.sede(),
				command.activoAfectado(),
				ahora,
				fechaLimiteSla,
				EstadoTicket.ABIERTO,
				null,
				List.of(),
				clock);

		ticket = ticketRepository.guardar(ticket);
		try {
			DespachoSugerido sugerido = dispatchPort.despachar(
					ticket.getId(), ticket.getCategoria(), ticket.getPrioridad());
			List<Tecnico> candidatos = new ArrayList<>(
					tecnicoRepository.buscarCandidatos(ticket.getCategoria(), ticket.getSede()));
			Tecnico recomendado = tecnicoRepository.buscarPorCodigo(sugerido.tecnicoCodigo()).orElse(null);
			Tecnico seleccionado = asignacionPolicy.seleccionarTecnico(ticket, candidatos, recomendado);
			ticket.asignar(seleccionado, "Asignación automática sugerida por dispatch.");
			ticket = ticketRepository.guardar(ticket);
			asignacionRepository.guardar(new Asignacion(
					null,
					ticket.getId(),
					seleccionado.getId(),
					clock.instant(),
					sugerido.tiempoEstimadoHoras(),
					sugerido.disponibilidad()));
		} catch (DispatchUnavailableException exception) {
			ticket.registrarObservacion("Dispatch no disponible; el ticket queda pendiente de asignación.", "SISTEMA");
			ticket = ticketRepository.guardar(ticket);
		}

		metricas.ticketCreado(ticket.getCategoria(), ticket.getPrioridad());
		return ticket;
	}
}
