package com.helpflow.supportticket.application.port.out;

import java.time.Duration;
import java.util.Map;

import com.helpflow.supportticket.domain.model.Prioridad;

public interface SlaConfigPort {

	Map<Prioridad, Duration> obtener();

	Map<Prioridad, Duration> actualizar(Map<Prioridad, Duration> plazos);
}
