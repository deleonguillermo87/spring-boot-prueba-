package com.helpflow.supportticket.application.port.in;

import java.time.Duration;
import java.util.Map;

import com.helpflow.supportticket.application.dto.ConfigurarSlaCommand;
import com.helpflow.supportticket.domain.model.Prioridad;

public interface ConfigurarSlaUseCase {

	Map<Prioridad, Duration> ejecutar(ConfigurarSlaCommand command);
}
