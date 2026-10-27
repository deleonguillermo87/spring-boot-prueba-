package com.helpflow.supportticket.unit.application.usecase;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;

import com.helpflow.supportticket.application.dto.Actor;
import com.helpflow.supportticket.application.dto.ConfigurarSlaCommand;
import com.helpflow.supportticket.application.port.out.SlaConfigPort;
import com.helpflow.supportticket.application.usecase.ConfigurarSlaService;
import com.helpflow.supportticket.domain.exception.AccesoNoPermitidoException;
import com.helpflow.supportticket.domain.model.Prioridad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigurarSlaServiceTest {

	@Mock
	private SlaConfigPort slaConfigPort;

	private ConfigurarSlaService service;

	@BeforeEach
	void setUp() {
		service = new ConfigurarSlaService(slaConfigPort, Clock.systemUTC());
	}

	@Test
	void adminPuedeConsultarSla() {
		when(slaConfigPort.obtener()).thenReturn(ApplicationTestFixtures.SLA);

		Map<Prioridad, Duration> resultado = service.obtener(ApplicationTestFixtures.adminActor());

		assertEquals(ApplicationTestFixtures.SLA, resultado);
		verify(slaConfigPort).obtener();
	}

	@Test
	void noAdminNoPuedeConsultarSla() {
		assertThrows(AccesoNoPermitidoException.class, () ->
				service.obtener(ApplicationTestFixtures.usuarioActor()));
		assertThrows(AccesoNoPermitidoException.class, () ->
				service.obtener(ApplicationTestFixtures.tecnicoActor()));
		assertThrows(AccesoNoPermitidoException.class, () ->
				service.obtener(null));
	}

	@Test
	void adminPuedeActualizarSla() {
		when(slaConfigPort.actualizar(ApplicationTestFixtures.SLA)).thenReturn(ApplicationTestFixtures.SLA);

		Map<Prioridad, Duration> resultado = service.ejecutar(
				new ConfigurarSlaCommand(ApplicationTestFixtures.SLA, ApplicationTestFixtures.adminActor()));

		assertEquals(ApplicationTestFixtures.SLA, resultado);
		verify(slaConfigPort).actualizar(ApplicationTestFixtures.SLA);
	}

	@Test
	void noAdminNoPuedeActualizarSla() {
		assertThrows(AccesoNoPermitidoException.class, () ->
				service.ejecutar(new ConfigurarSlaCommand(ApplicationTestFixtures.SLA, ApplicationTestFixtures.usuarioActor())));
	}
}

