package com.helpflow.supportticket.infrastructure.adapter.out.persistence.mapper;

import com.helpflow.supportticket.domain.model.Usuario;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

	Usuario toDomain(UsuarioEntity entity);

	UsuarioEntity toEntity(Usuario usuario);
}
