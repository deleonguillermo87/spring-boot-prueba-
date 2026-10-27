package com.helpflow.supportticket.domain.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import com.helpflow.supportticket.domain.exception.SlaInvalidoException;
import com.helpflow.supportticket.domain.model.Prioridad;

public class SlaPolicy {

	private static final Map<Prioridad, Duration> PLAZOS_PREDETERMINADOS = Map.of(
			Prioridad.CRITICA, Duration.ofHours(4),
			Prioridad.ALTA, Duration.ofHours(8),
			Prioridad.MEDIA, Duration.ofHours(24),
			Prioridad.BAJA, Duration.ofHours(72));

	private final Clock clock;
	private final Map<Prioridad, Duration> plazos;

	public SlaPolicy(Clock clock) {
		this(clock, PLAZOS_PREDETERMINADOS);
	}

	public SlaPolicy(Clock clock, Map<Prioridad, Duration> plazos) {
		this.clock = Objects.requireNonNull(clock, "clock es obligatorio");
		Objects.requireNonNull(plazos, "plazos son obligatorios");
		EnumMap<Prioridad, Duration> plazosValidados = new EnumMap<>(Prioridad.class);
		for (Prioridad prioridad : Prioridad.values()) {
			Duration plazo = plazos.get(prioridad);
			if (plazo == null || plazo.isNegative() || plazo.isZero()) {
				throw new SlaInvalidoException("El plazo para " + prioridad + " debe ser positivo.");
			}
			plazosValidados.put(prioridad, plazo);
		}
		this.plazos = Map.copyOf(plazosValidados);
	}

	public Instant calcularFechaLimiteSla(Prioridad prioridad) {
		return calcularFechaLimiteSla(prioridad, clock.instant());
	}

	public Instant calcularFechaLimiteSla(Prioridad prioridad, Instant desde) {
		Objects.requireNonNull(desde, "desde es obligatorio");
		Duration plazo = plazos.get(Objects.requireNonNull(prioridad, "prioridad es obligatoria"));
		if (plazo == null) {
			throw new SlaInvalidoException("No hay un plazo SLA configurado para " + prioridad + ".");
		}
		return desde.plus(plazo);
	}
}
