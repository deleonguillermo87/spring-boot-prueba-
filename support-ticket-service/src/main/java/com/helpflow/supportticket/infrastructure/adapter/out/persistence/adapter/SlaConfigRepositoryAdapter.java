package com.helpflow.supportticket.infrastructure.adapter.out.persistence.adapter;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.helpflow.supportticket.application.port.out.SlaConfigPort;
import com.helpflow.supportticket.domain.model.Prioridad;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.mapper.SlaConfigMapper;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.repository.SlaConfigJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class SlaConfigRepositoryAdapter implements SlaConfigPort {

	private final SlaConfigJpaRepository repository;
	private final SlaConfigMapper mapper;

	public SlaConfigRepositoryAdapter(SlaConfigJpaRepository repository, SlaConfigMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	@Transactional(readOnly = true)
	public Map<Prioridad, Duration> obtener() {
		return repository.findAll().stream().collect(Collectors.toMap(
				entity -> entity.getPrioridad(),
				mapper::toDuration,
				(first, second) -> first,
				() -> new EnumMap<>(Prioridad.class)));
	}

	@Override
	public Map<Prioridad, Duration> actualizar(Map<Prioridad, Duration> plazos) {
		plazos.forEach((prioridad, duracion) -> {
			long horas = duracion.toHours();
			if (duracion.isNegative() || duracion.isZero()
					|| duracion.getSeconds() != Duration.ofHours(horas).getSeconds()) {
				throw new IllegalArgumentException("El SLA debe expresarse en horas enteras positivas.");
			}
			repository.save(mapper.toEntity(prioridad, duracion));
		});
		return obtener();
	}
}
