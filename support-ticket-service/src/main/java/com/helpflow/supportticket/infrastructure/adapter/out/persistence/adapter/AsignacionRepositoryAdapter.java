package com.helpflow.supportticket.infrastructure.adapter.out.persistence.adapter;

import com.helpflow.supportticket.application.port.out.AsignacionRepositoryPort;
import com.helpflow.supportticket.domain.model.Asignacion;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.mapper.AsignacionMapper;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.repository.AsignacionJpaRepository;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.repository.TecnicoJpaRepository;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.repository.TicketJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class AsignacionRepositoryAdapter implements AsignacionRepositoryPort {

	private final AsignacionJpaRepository repository;
	private final TicketJpaRepository ticketRepository;
	private final TecnicoJpaRepository tecnicoRepository;
	private final AsignacionMapper mapper;

	public AsignacionRepositoryAdapter(
			AsignacionJpaRepository repository,
			TicketJpaRepository ticketRepository,
			TecnicoJpaRepository tecnicoRepository,
			AsignacionMapper mapper) {
		this.repository = repository;
		this.ticketRepository = ticketRepository;
		this.tecnicoRepository = tecnicoRepository;
		this.mapper = mapper;
	}

	@Override
	public Asignacion guardar(Asignacion asignacion) {
		var entity = mapper.toEntity(asignacion);
		entity.setTicket(ticketRepository.getReferenceById(asignacion.ticketId()));
		entity.setTecnico(tecnicoRepository.getReferenceById(asignacion.tecnicoId()));
		return mapper.toDomain(repository.save(entity));
	}
}
