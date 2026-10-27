package com.helpflow.supportticket.unit.rest;

import java.time.Duration;
import java.util.Map;

import com.helpflow.supportticket.application.dto.Actor;
import com.helpflow.supportticket.application.dto.ConfigurarSlaCommand;
import com.helpflow.supportticket.application.port.in.ConfigurarSlaUseCase;
import com.helpflow.supportticket.domain.model.Prioridad;
import com.helpflow.supportticket.domain.model.Rol;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.controller.AdminSlaController;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.dto.ConfigurarSlaRequest;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.dto.SlaResponse;
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
class AdminSlaControllerTest {

	@Mock
	private ConfigurarSlaUseCase configurarSlaUseCase;
	@Mock
	private ActorResolver actorResolver;

	private AdminSlaController controller;
	private Jwt jwt;
	private Actor adminActor;

	@BeforeEach
	void setUp() {
		controller = new AdminSlaController(configurarSlaUseCase, actorResolver);
		jwt = Jwt.withTokenValue("mock-jwt")
				.header("alg", "HS256")
				.subject("99")
				.claim("rol", "ROLE_ADMIN")
				.build();
		adminActor = new Actor(99L, Rol.ROLE_ADMIN);
	}

	@Test
	void obtenerSlaRetornaPlazosEnHoras() {
		when(actorResolver.resolver(jwt)).thenReturn(adminActor);
		when(configurarSlaUseCase.obtener(adminActor)).thenReturn(Map.of(
				Prioridad.CRITICA, Duration.ofHours(4),
				Prioridad.ALTA, Duration.ofHours(8)));

		SlaResponse response = controller.obtener(jwt);

		assertNotNull(response);
		assertEquals(4L, response.plazos().get(Prioridad.CRITICA));
		assertEquals(8L, response.plazos().get(Prioridad.ALTA));
		verify(configurarSlaUseCase).obtener(adminActor);
	}

	@Test
	void actualizarSlaRetornaPlazosActualizados() {
		when(actorResolver.resolver(jwt)).thenReturn(adminActor);
		when(configurarSlaUseCase.ejecutar(any(ConfigurarSlaCommand.class))).thenReturn(Map.of(
				Prioridad.CRITICA, Duration.ofHours(2),
				Prioridad.ALTA, Duration.ofHours(6)));

		ConfigurarSlaRequest request = new ConfigurarSlaRequest(Map.of(
				Prioridad.CRITICA, 2L,
				Prioridad.ALTA, 6L));

		SlaResponse response = controller.actualizar(request, jwt);

		assertNotNull(response);
		assertEquals(2L, response.plazos().get(Prioridad.CRITICA));
		assertEquals(6L, response.plazos().get(Prioridad.ALTA));
		verify(configurarSlaUseCase).ejecutar(any(ConfigurarSlaCommand.class));
	}
}

