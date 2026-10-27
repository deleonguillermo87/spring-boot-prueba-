package com.helpflow.supportticket.infrastructure.adapter.out.persistence.mapper;

import java.time.Duration;

import com.helpflow.supportticket.domain.model.Prioridad;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.entity.SlaConfigEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SlaConfigMapper {

	@Mapping(target = "prioridad", source = "prioridad")
	@Mapping(target = "horas", expression = "java(Math.toIntExact(duracion.toHours()))")
	SlaConfigEntity toEntity(Prioridad prioridad, Duration duracion);

	default Duration toDuration(SlaConfigEntity entity) {
		return Duration.ofHours(entity.getHoras());
	}
}
