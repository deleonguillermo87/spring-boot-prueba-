package com.helpflow.supportticket.infrastructure.adapter.out.persistence.adapter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.helpflow.supportticket.application.port.out.TecnicoRepositoryPort;
import com.helpflow.supportticket.domain.model.Categoria;
import com.helpflow.supportticket.domain.model.Tecnico;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.entity.TecnicoEntity;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.mapper.TecnicoMapper;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.repository.TecnicoJpaRepository;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.repository.UsuarioJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class TecnicoRepositoryAdapter implements TecnicoRepositoryPort {

	private final TecnicoJpaRepository repository;
	private final UsuarioJpaRepository usuarioRepository;
	private final TecnicoMapper mapper;

	public TecnicoRepositoryAdapter(
			TecnicoJpaRepository repository,
			UsuarioJpaRepository usuarioRepository,
			TecnicoMapper mapper) {
		this.repository = repository;
		this.usuarioRepository = usuarioRepository;
		this.mapper = mapper;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Tecnico> buscarPorCodigo(String codigo) {
		Map<Long, Long> conteos = conteosActivos();
		return repository.findByCodigo(codigo)
				.map(entity -> mapper.toDomain(entity, conteos.getOrDefault(entity.getId(), 0L).intValue()));
	}

	@Override
	@Transactional(readOnly = true)
	public List<Tecnico> buscarCandidatos(Categoria categoria, String sede) {
		List<TecnicoEntity> candidates = repository.findCandidatos(categoria);
		if (candidates.isEmpty()) {
			return List.of();
		}
		Map<Long, Long> conteos = conteosActivos();
		return candidates.stream()
				.map(entity -> mapper.toDomain(entity, conteos.getOrDefault(entity.getId(), 0L).intValue()))
				.toList();
	}

	@Override
	public Tecnico guardar(Tecnico tecnico) {
		var entity = mapper.toEntity(tecnico);
		entity.setId(tecnico.getId());
		if (tecnico.getUsuarioId() != null) {
			entity.setUsuario(usuarioRepository.getReferenceById(tecnico.getUsuarioId()));
		}
		var saved = repository.save(entity);
		return mapper.toDomain(saved, tecnico.getTicketsActivos());
	}

	private Map<Long, Long> conteosActivos() {
		return repository.contarTicketsActivos().stream().collect(Collectors.toMap(
				TecnicoJpaRepository.TecnicoTicketCount::getTecnicoId,
				TecnicoJpaRepository.TecnicoTicketCount::getCantidad,
				(first, second) -> first));
	}
}
