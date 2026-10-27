package com.helpflow.supportticket.infrastructure.adapter.in.rest.dto;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.helpflow.supportticket.domain.model.Prioridad;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ConfigurarSlaRequest(
		@NotNull @NotEmpty Map<Prioridad, Long> plazos) {

	@JsonCreator
	public static ConfigurarSlaRequest create(Map<String, Object> raw) {
		if (raw == null) {
			return new ConfigurarSlaRequest(null);
		}
		if (raw.containsKey("plazos") && raw.get("plazos") instanceof Map<?, ?> inner) {
			Map<Prioridad, Long> mapped = new HashMap<>();
			inner.forEach((k, v) -> mapped.put(Prioridad.valueOf(k.toString()), Long.valueOf(v.toString())));
			return new ConfigurarSlaRequest(mapped);
		}
		Map<Prioridad, Long> mapped = new HashMap<>();
		raw.forEach((k, v) -> mapped.put(Prioridad.valueOf(k.toString()), Long.valueOf(v.toString())));
		return new ConfigurarSlaRequest(mapped);
	}
}

