package com.helpflow.supportticket.infrastructure.adapter.out.persistence.mapper;

import com.helpflow.supportticket.domain.model.HistorialTicket;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.entity.HistorialTicketEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HistorialTicketMapper {

	HistorialTicket toDomain(HistorialTicketEntity entity);

	@Mapping(target = "ticket", ignore = true)
	HistorialTicketEntity toEntity(HistorialTicket historialTicket);
}
