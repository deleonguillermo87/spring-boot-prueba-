package com.helpflow.supportticket.unit.scheduler;

import java.time.Duration;

import com.helpflow.supportticket.application.dto.Actor;
import com.helpflow.supportticket.application.dto.EscalarTicketsCommand;
import com.helpflow.supportticket.application.port.in.EscalarTicketsUseCase;
import com.helpflow.supportticket.infrastructure.scheduler.EscalamientoSlaScheduler;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

class EscalamientoSlaSchedulerTest {

	@Test
	void invocaElCasoDeUsoConActorSistemaYUmbralConfigurado() {
		EscalarTicketsUseCase useCase = mock(EscalarTicketsUseCase.class);
		EscalamientoSlaScheduler scheduler = new EscalamientoSlaScheduler(useCase, 45);

		scheduler.ejecutar();

		var captor = org.mockito.ArgumentCaptor.forClass(EscalarTicketsCommand.class);
		verify(useCase).ejecutar(captor.capture());
		assertThat(captor.getValue().umbralSla()).isEqualTo(Duration.ofMinutes(45));
		assertThat(captor.getValue().actor()).isEqualTo(Actor.sistema());
	}

	@Test
	void capturaLasExcepcionesDelJob() {
		EscalarTicketsUseCase useCase = mock(EscalarTicketsUseCase.class);
		when(useCase.ejecutar(any())).thenThrow(new IllegalStateException("fallo simulado"));

		org.assertj.core.api.Assertions.assertThatCode(
				() -> new EscalamientoSlaScheduler(useCase, 60).ejecutar()).doesNotThrowAnyException();
	}
}
