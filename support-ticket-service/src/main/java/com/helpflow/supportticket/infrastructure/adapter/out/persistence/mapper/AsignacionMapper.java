package com.helpflow.supportticket.infrastructure.adapter.out.persistence.mapper;

import com.helpflow.supportticket.domain.model.Asignacion;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.entity.AsignacionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AsignacionMapper {

	@Mapping(target = "ticketId", source = "ticket.id")
	@Mapping(target = "tecnicoId", source = "tecnico.id")
	Asignacion toDomain(AsignacionEntity entity);

	@Mapping(target = "ticket", ignore = true)
	@Mapping(target = "tecnico", ignore = true)
	AsignacionEntity toEntity(Asignacion asignacion);
}
