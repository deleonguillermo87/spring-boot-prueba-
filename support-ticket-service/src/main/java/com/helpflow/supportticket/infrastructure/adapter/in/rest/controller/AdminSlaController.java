package com.helpflow.supportticket.infrastructure.adapter.in.rest.controller;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

import com.helpflow.supportticket.application.dto.Actor;
import com.helpflow.supportticket.application.dto.ConfigurarSlaCommand;
import com.helpflow.supportticket.application.port.in.ConfigurarSlaUseCase;
import com.helpflow.supportticket.domain.model.Prioridad;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.dto.ConfigurarSlaRequest;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.dto.SlaResponse;
import com.helpflow.supportticket.infrastructure.security.ActorResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/sla")
@Tag(name = "Administración de SLA", description = "Configuración y consulta de plazos SLA por prioridad")
public class AdminSlaController {

	private final ConfigurarSlaUseCase configurarSlaUseCase;
	private final ActorResolver actorResolver;

	public AdminSlaController(ConfigurarSlaUseCase configurarSlaUseCase, ActorResolver actorResolver) {
		this.configurarSlaUseCase = configurarSlaUseCase;
		this.actorResolver = actorResolver;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	@Operation(summary = "Consultar SLA", description = "Obtiene los plazos SLA en horas para cada prioridad")
	public SlaResponse obtener(@AuthenticationPrincipal Jwt jwt) {
		Actor actor = actorResolver.resolver(jwt);
		Map<Prioridad, Duration> plazos = configurarSlaUseCase.obtener(actor);
		Map<Prioridad, Long> enHoras = new EnumMap<>(Prioridad.class);
		plazos.forEach((p, d) -> enHoras.put(p, d.toHours()));
		return new SlaResponse(enHoras);
	}

	@PutMapping
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	@Operation(summary = "Configurar SLA", description = "Actualiza los plazos SLA en horas para las distintas prioridades")
	public SlaResponse actualizar(
			@Valid @RequestBody ConfigurarSlaRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		Actor actor = actorResolver.resolver(jwt);
		Map<Prioridad, Duration> duraciones = new EnumMap<>(Prioridad.class);
		request.plazos().forEach((prioridad, horas) -> duraciones.put(prioridad, Duration.ofHours(horas)));
		Map<Prioridad, Duration> actualizados = configurarSlaUseCase.ejecutar(new ConfigurarSlaCommand(duraciones, actor));
		Map<Prioridad, Long> enHoras = new EnumMap<>(Prioridad.class);
		actualizados.forEach((p, d) -> enHoras.put(p, d.toHours()));
		return new SlaResponse(enHoras);
	}
}

