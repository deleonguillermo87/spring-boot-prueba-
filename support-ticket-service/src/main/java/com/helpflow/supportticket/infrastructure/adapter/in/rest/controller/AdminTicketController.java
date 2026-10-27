package com.helpflow.supportticket.infrastructure.adapter.in.rest.controller;

import java.time.Duration;

import com.helpflow.supportticket.application.dto.Actor;
import com.helpflow.supportticket.application.dto.EscalarTicketsCommand;
import com.helpflow.supportticket.application.port.in.EscalarTicketsUseCase;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.dto.EscalarTicketsResponse;
import com.helpflow.supportticket.infrastructure.security.ActorResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/tickets")
@Tag(name = "Administración de Tickets", description = "Operaciones administrativas de tickets")
public class AdminTicketController {

	private final EscalarTicketsUseCase escalarTicketsUseCase;
	private final ActorResolver actorResolver;

	public AdminTicketController(EscalarTicketsUseCase escalarTicketsUseCase, ActorResolver actorResolver) {
		this.escalarTicketsUseCase = escalarTicketsUseCase;
		this.actorResolver = actorResolver;
	}

	@PostMapping("/escalar")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	@Operation(summary = "Escalar tickets próximos a vencer", description = "Escala la prioridad de tickets activos que se encuentran cerca de su límite SLA")
	public EscalarTicketsResponse escalar(
			@RequestParam(defaultValue = "2") long umbralHoras,
			@AuthenticationPrincipal Jwt jwt) {
		Actor actor = actorResolver.resolver(jwt);
		int escalados = escalarTicketsUseCase.ejecutar(
				new EscalarTicketsCommand(Duration.ofHours(umbralHoras), actor));
		return new EscalarTicketsResponse(escalados);
	}
}

