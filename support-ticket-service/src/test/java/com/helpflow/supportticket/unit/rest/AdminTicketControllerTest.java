package com.helpflow.supportticket.unit.rest;

import com.helpflow.supportticket.application.dto.Actor;
import com.helpflow.supportticket.application.dto.EscalarTicketsCommand;
import com.helpflow.supportticket.application.port.in.EscalarTicketsUseCase;
import com.helpflow.supportticket.domain.model.Rol;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.controller.AdminTicketController;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.dto.EscalarTicketsResponse;
import com.helpflow.supportticket.infrastructure.security.ActorResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminTicketControllerTest {

	@Mock
	private EscalarTicketsUseCase escalarTicketsUseCase;
	@Mock
	private ActorResolver actorResolver;

	private AdminTicketController controller;
	private Jwt jwt;
	private Actor adminActor;

	@BeforeEach
	void setUp() {
		controller = new AdminTicketController(escalarTicketsUseCase, actorResolver);
		jwt = Jwt.withTokenValue("mock-jwt")
				.header("alg", "HS256")
				.subject("99")
				.claim("rol", "ROLE_ADMIN")
				.build();
		adminActor = new Actor(99L, Rol.ROLE_ADMIN);
	}

	@Test
	void escalarTicketsRetornaCantidad() {
		when(actorResolver.resolver(jwt)).thenReturn(adminActor);
		when(escalarTicketsUseCase.ejecutar(any(EscalarTicketsCommand.class))).thenReturn(3);

		EscalarTicketsResponse response = controller.escalar(4, jwt);

		assertNotNull(response);
		assertEquals(3, response.ticketsEscalados());
		verify(escalarTicketsUseCase).ejecutar(any(EscalarTicketsCommand.class));
	}
}

