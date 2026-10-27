package com.helpflow.supportticket.infrastructure.adapter.out.persistence.mapper;

import com.helpflow.supportticket.domain.model.Tecnico;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.entity.TecnicoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class TecnicoMapper {

	public Tecnico toDomain(TecnicoEntity entity, int ticketsActivos) {
		return new Tecnico(
				entity.getId(),
				entity.getUsuario() == null ? null : entity.getUsuario().getId(),
				entity.getCodigo(),
				entity.getDocumento(),
				entity.getNombre(),
				entity.getSede(),
				entity.getEspecialidades(),
				entity.getCargaMaxima(),
				ticketsActivos,
				entity.getEstado());
	}

	@Mapping(target = "usuario", ignore = true)
	public abstract TecnicoEntity toEntity(Tecnico tecnico);
}
