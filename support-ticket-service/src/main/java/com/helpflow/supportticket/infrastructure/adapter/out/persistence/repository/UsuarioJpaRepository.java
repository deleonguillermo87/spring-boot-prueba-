package com.helpflow.supportticket.infrastructure.adapter.out.persistence.repository;

import java.util.Optional;

import com.helpflow.supportticket.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long> {

	Optional<UsuarioEntity> findByCorreoCorporativo(String correoCorporativo);

	boolean existsByDocumento(String documento);

	boolean existsByCorreoCorporativo(String correoCorporativo);
}
