package com.helpflow.supportticket.infrastructure.adapter.out.persistence.adapter;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.helpflow.supportticket.application.dto.FiltroTickets;
import com.helpflow.supportticket.application.dto.PaginaResultado;
import com.helpflow.supportticket.application.dto.PaginaSolicitud;
import com.helpflow.supportticket.application.port.out.TicketRepositoryPort;
import com.helpflow.supportticket.domain.model.HistorialTicket;
import com.helpflow.supportticket.domain.model.Ticket;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.entity.HistorialTicketEntity;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.entity.TicketEntity;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.mapper.HistorialTicketMapper;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.mapper.TicketMapper;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.repository.TecnicoJpaRepository;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.repository.TicketJpaRepository;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.repository.UsuarioJpaRepository;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class TicketRepositoryAdapter implements TicketRepositoryPort {

	private static final Map<String, String> SORT_FIELDS = Map.of(
			"fechaCreacion", "fechaCreacion",
			"fechaLimiteSla", "fechaLimiteSla",
			"prioridad", "prioridad",
			"estado", "estado");

	private final TicketJpaRepository repository;
	private final UsuarioJpaRepository usuarioRepository;
	private final TecnicoJpaRepository tecnicoRepository;
	private final EntityManager entityManager;
	private final TicketMapper ticketMapper;
	private final HistorialTicketMapper historialMapper;

	public TicketRepositoryAdapter(
			TicketJpaRepository repository,
			UsuarioJpaRepository usuarioRepository,
			TecnicoJpaRepository tecnicoRepository,
			EntityManager entityManager,
			TicketMapper ticketMapper,
			HistorialTicketMapper historialMapper) {
		this.repository = repository;
		this.usuarioRepository = usuarioRepository;
		this.tecnicoRepository = tecnicoRepository;
		this.entityManager = entityManager;
		this.ticketMapper = ticketMapper;
		this.historialMapper = historialMapper;
	}

	@Override
	public Ticket guardar(Ticket ticket) {
		TicketEntity entity = ticket.getId() == null
				? ticketMapper.toEntity(ticket)
				: repository.findDetailedById(ticket.getId())
						.orElseThrow(() -> new IllegalStateException("No existe el ticket " + ticket.getId()));
		copyScalars(ticket, entity);
		entity.setSolicitante(usuarioRepository.getReferenceById(ticket.getSolicitante().getId()));
		entity.setTecnicoAsignado(ticket.getTecnicoAsignado() == null
				? null
				: tecnicoRepository.getReferenceById(ticket.getTecnicoAsignado().getId()));
		reconcileHistory(ticket, entity);
		return ticketMapper.toDomain(repository.saveAndFlush(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Ticket> buscarPorId(Long id) {
		return repository.findDetailedById(id).map(ticketMapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public PaginaResultado<Ticket> buscarPorSolicitante(
			Long solicitanteId,
			PaginaSolicitud pagina,
			FiltroTickets filtros) {
		return page(repository.pageBySolicitante(
				solicitanteId,
				filtros.estado(),
				filtros.categoria(),
				filtros.prioridad(),
				pageRequest(pagina)));
	}

	@Override
	@Transactional(readOnly = true)
	public PaginaResultado<Ticket> buscarPorTecnico(
			Long tecnicoId,
			PaginaSolicitud pagina,
			FiltroTickets filtros) {
		return page(repository.pageByTecnico(
				tecnicoId,
				filtros.estado(),
				filtros.categoria(),
				filtros.prioridad(),
				pageRequest(pagina)));
	}

	@Override
	@Transactional(readOnly = true)
	public PaginaResultado<Ticket> buscarTodos(PaginaSolicitud pagina, FiltroTickets filtros) {
		return page(repository.pageAll(
				filtros.estado(),
				filtros.categoria(),
				filtros.prioridad(),
				pageRequest(pagina)));
	}

	@Override
	@Transactional(readOnly = true)
	public List<Ticket> buscarActivosProximosAVencer(Instant limite) {
		return ticketMapper.toDomainWithHistory(repository.findActivosProximosAVencer(limite));
	}

	private PageRequest pageRequest(PaginaSolicitud solicitud) {
		String field = SORT_FIELDS.getOrDefault(solicitud.ordenarPor(), "fechaCreacion");
		Sort.Direction direction = solicitud.descendente() ? Sort.Direction.DESC : Sort.Direction.ASC;
		return PageRequest.of(solicitud.pagina(), solicitud.tamano(), Sort.by(direction, field));
	}

	private PaginaResultado<Ticket> page(Page<TicketEntity> page) {
		return new PaginaResultado<>(
				ticketMapper.toDomain(page.getContent()),
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages(),
				page.isFirst(),
				page.isLast());
	}

	private void copyScalars(Ticket ticket, TicketEntity entity) {
		entity.setTitulo(ticket.getTitulo());
		entity.setDescripcion(ticket.getDescripcion());
		entity.setCategoria(ticket.getCategoria());
		entity.setPrioridad(ticket.getPrioridad());
		entity.setSede(ticket.getSede());
		entity.setActivoAfectado(ticket.getActivoAfectado());
		entity.setFechaCreacion(ticket.getFechaCreacion());
		entity.setFechaLimiteSla(ticket.getFechaLimiteSla());
		entity.setEstado(ticket.getEstado());
	}

	private void reconcileHistory(Ticket ticket, TicketEntity entity) {
		Map<Long, HistorialTicketEntity> existingById = new HashMap<>();
		for (HistorialTicketEntity existing : entity.getHistorial()) {
			if (existing.getId() != null) {
				existingById.put(existing.getId(), existing);
			}
		}
		Set<Long> retainedIds = new HashSet<>();
		for (HistorialTicket historial : ticket.getHistorial()) {
			HistorialTicketEntity record = historial.id() == null
					? historialMapper.toEntity(historial)
					: existingById.get(historial.id());
			if (record == null) {
				throw new IllegalStateException("El historial " + historial.id() + " no pertenece al ticket.");
			}
			record.setEstadoAnterior(historial.estadoAnterior());
			record.setEstadoNuevo(historial.estadoNuevo());
			record.setObservacion(historial.observacion());
			record.setAutor(historial.autor());
			record.setFecha(historial.fecha());
			if (historial.id() == null) {
				entity.addHistorial(record);
			} else {
				retainedIds.add(historial.id());
			}
		}
		entity.getHistorial().removeIf(record -> record.getId() != null && !retainedIds.contains(record.getId()));
	}
}
