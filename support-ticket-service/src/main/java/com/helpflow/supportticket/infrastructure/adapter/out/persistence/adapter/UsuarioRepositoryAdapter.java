package com.helpflow.supportticket.infrastructure.adapter.out.persistence.adapter;

import java.util.Optional;

import com.helpflow.supportticket.application.port.out.UsuarioRepositoryPort;
import com.helpflow.supportticket.domain.model.Usuario;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.mapper.UsuarioMapper;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.repository.UsuarioJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

	private final UsuarioJpaRepository repository;
	private final UsuarioMapper mapper;

	public UsuarioRepositoryAdapter(UsuarioJpaRepository repository, UsuarioMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public Usuario guardar(Usuario usuario) {
		return mapper.toDomain(repository.save(mapper.toEntity(usuario)));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Usuario> buscarPorId(Long id) {
		return repository.findById(id).map(mapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Usuario> buscarPorCorreo(String correoCorporativo) {
		return repository.findByCorreoCorporativo(correoCorporativo).map(mapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existePorDocumento(String documento) {
		return repository.existsByDocumento(documento);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existePorCorreo(String correoCorporativo) {
		return repository.existsByCorreoCorporativo(correoCorporativo);
	}
}
