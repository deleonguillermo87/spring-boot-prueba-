package com.helpflow.supportticket.unit.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import com.helpflow.supportticket.domain.model.Prioridad;
import com.helpflow.supportticket.domain.service.SlaPolicy;
import org.junit.jupiter.api.Test;

class SlaPolicyTest {

	@Test
	void calculatesDefaultDeadlineForEachPriorityUsingFixedClock() {
		SlaPolicy policy = new SlaPolicy(DomainTestFixtures.RELOJ_FIJO);
		Map<Prioridad, Duration> plazosEsperados = Map.of(
				Prioridad.CRITICA, Duration.ofHours(4),
				Prioridad.ALTA, Duration.ofHours(8),
				Prioridad.MEDIA, Duration.ofHours(24),
				Prioridad.BAJA, Duration.ofHours(72));

		plazosEsperados.forEach((prioridad, plazo) ->
				assertEquals(DomainTestFixtures.AHORA.plus(plazo), policy.calcularFechaLimiteSla(prioridad)));
	}

	@Test
	void calculatesDeadlineFromExplicitInstantAndCustomConfiguration() {
		Instant inicio = Instant.parse("2026-02-01T00:00:00Z");
		SlaPolicy policy = new SlaPolicy(
				DomainTestFixtures.RELOJ_FIJO,
				Map.of(
						Prioridad.CRITICA, Duration.ofMinutes(30),
						Prioridad.ALTA, Duration.ofHours(2),
						Prioridad.MEDIA, Duration.ofHours(6),
						Prioridad.BAJA, Duration.ofDays(2)));

		assertEquals(inicio.plus(Duration.ofMinutes(30)),
				policy.calcularFechaLimiteSla(Prioridad.CRITICA, inicio));
	}
}
