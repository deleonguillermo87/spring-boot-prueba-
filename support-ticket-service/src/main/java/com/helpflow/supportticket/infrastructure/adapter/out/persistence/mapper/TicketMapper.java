package com.helpflow.supportticket.infrastructure.adapter.out.persistence.mapper;

import java.util.List;
import java.util.Objects;

import com.helpflow.supportticket.domain.model.Ticket;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.entity.TicketEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {UsuarioMapper.class, TecnicoMapper.class, HistorialTicketMapper.class})
public abstract class TicketMapper {

	@Autowired
	protected UsuarioMapper usuarioMapper;
	@Autowired
	protected TecnicoMapper tecnicoMapper;
	@Autowired
	protected HistorialTicketMapper historialTicketMapper;

	public Ticket toDomain(TicketEntity entity) {
		Objects.requireNonNull(entity, "entity es obligatorio");
		return toDomain(entity, entity.getHistorial().stream().map(historialTicketMapper::toDomain).toList());
	}

	public Ticket toDomainWithoutHistory(TicketEntity entity) {
		Objects.requireNonNull(entity, "entity es obligatorio");
		return toDomain(entity, List.of());
	}

	private Ticket toDomain(TicketEntity entity, List<com.helpflow.supportticket.domain.model.HistorialTicket> historial) {
		return new Ticket(
				entity.getId(),
				usuarioMapper.toDomain(entity.getSolicitante()),
				entity.getTitulo(),
				entity.getDescripcion(),
				entity.getCategoria(),
				entity.getPrioridad(),
				entity.getSede(),
				entity.getActivoAfectado(),
				entity.getFechaCreacion(),
				entity.getFechaLimiteSla(),
				entity.getEstado(),
				entity.getTecnicoAsignado() == null
						? null
						: tecnicoMapper.toDomain(entity.getTecnicoAsignado(), 0),
				historial);
	}

	@Mapping(target = "solicitante", ignore = true)
	@Mapping(target = "tecnicoAsignado", ignore = true)
	@Mapping(target = "historial", ignore = true)
	@Mapping(target = "version", ignore = true)
	public abstract TicketEntity toEntity(Ticket ticket);

	public List<Ticket> toDomain(List<TicketEntity> entities) {
		return entities.stream().map(this::toDomainWithoutHistory).toList();
	}

	public List<Ticket> toDomainWithHistory(List<TicketEntity> entities) {
		return entities.stream().map(this::toDomain).toList();
	}
}
