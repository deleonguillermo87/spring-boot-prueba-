package com.helpflow.supportticket.application.usecase;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import java.util.Map;

import com.helpflow.supportticket.application.dto.Actor;
import com.helpflow.supportticket.application.dto.ConfigurarSlaCommand;
import com.helpflow.supportticket.application.port.in.ConfigurarSlaUseCase;
import com.helpflow.supportticket.application.port.out.SlaConfigPort;
import com.helpflow.supportticket.domain.exception.AccesoNoPermitidoException;
import com.helpflow.supportticket.domain.model.Prioridad;
import com.helpflow.supportticket.domain.model.Rol;
import com.helpflow.supportticket.domain.service.SlaPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfigurarSlaService implements ConfigurarSlaUseCase {

	private final SlaConfigPort slaConfigPort;
	private final Clock clock;

	public ConfigurarSlaService(SlaConfigPort slaConfigPort, Clock clock) {
		this.slaConfigPort = slaConfigPort;
		this.clock = clock;
	}

	@Override
	@Transactional
	public Map<Prioridad, Duration> ejecutar(ConfigurarSlaCommand command) {
		Objects.requireNonNull(command, "command es obligatorio");
		if (command.actor() == null || command.actor().rol() != Rol.ROLE_ADMIN) {
			throw new AccesoNoPermitidoException("Solo un administrador puede configurar los SLA.");
		}
		Map<Prioridad, Duration> plazos = Map.copyOf(command.plazos());
		new SlaPolicy(clock, plazos);
		return slaConfigPort.actualizar(plazos);
	}

	@Override
	@Transactional(readOnly = true)
	public Map<Prioridad, Duration> obtener(Actor actor) {
		if (actor == null || actor.rol() != Rol.ROLE_ADMIN) {
			throw new AccesoNoPermitidoException("Solo un administrador puede consultar los SLA.");
		}
		return slaConfigPort.obtener();
	}
}
