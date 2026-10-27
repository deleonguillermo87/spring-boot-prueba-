package com.helpflow.supportticket.infrastructure.observability;

import java.time.Duration;
import java.util.Objects;

import com.helpflow.supportticket.application.port.out.MetricasPort;
import com.helpflow.supportticket.domain.model.Categoria;
import com.helpflow.supportticket.domain.model.Prioridad;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MicrometerMetricasAdapter implements MetricasPort {

	private final MeterRegistry registry;

	public MicrometerMetricasAdapter(MeterRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void ticketCreado(Categoria categoria, Prioridad prioridad) {
		registry.counter(
				"tickets_creados_total",
				"categoria", Objects.requireNonNull(categoria).name(),
				"prioridad", Objects.requireNonNull(prioridad).name()).increment();
	}

	@Override
	public void ticketVencido() {
		registry.counter("tickets_vencidos_sla_total").increment();
	}

	@Override
	public void tiempoPrimeraRespuesta(Duration duracion) {
		registry.timer("tiempo_primera_respuesta").record(Objects.requireNonNull(duracion));
	}

	@Override
	public void tiempoResolucion(Duration duracion) {
		registry.timer("tiempo_resolucion").record(Objects.requireNonNull(duracion));
	}

	@Override
	public void falloAutenticacion() {
		registry.counter("fallas_autenticacion_total").increment();
	}

	@Override
	public void accesoDenegado() {
		registry.counter("accesos_denegados_total").increment();
	}
}
