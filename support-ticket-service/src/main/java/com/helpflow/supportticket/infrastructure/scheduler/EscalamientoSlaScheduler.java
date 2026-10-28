package com.helpflow.supportticket.infrastructure.scheduler;

import java.time.Duration;
import java.util.UUID;

import com.helpflow.supportticket.application.dto.Actor;
import com.helpflow.supportticket.application.dto.EscalarTicketsCommand;
import com.helpflow.supportticket.application.port.in.EscalarTicketsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;

public class EscalamientoSlaScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(EscalamientoSlaScheduler.class);
	private final EscalarTicketsUseCase escalarTickets;
	private final long umbralMinutos;

	public EscalamientoSlaScheduler(EscalarTicketsUseCase escalarTickets, long umbralMinutos) {
		this.escalarTickets = escalarTickets;
		this.umbralMinutos = umbralMinutos;
	}

	@Scheduled(fixedDelayString = "${helpflow.sla.escalation.fixed-delay:60000}")
	public void ejecutar() {
		try {
			int escalados = escalarTickets.ejecutar(
					new EscalarTicketsCommand(Duration.ofMinutes(umbralMinutos), Actor.sistema()));
			LOGGER.debug("Escalamiento SLA completado; ticketsEscalados={}", escalados);
		} catch (Exception exception) {
			String traceId = MDC.get("traceId");
			if (traceId == null || traceId.isBlank()) {
				traceId = UUID.randomUUID().toString();
			}
			LOGGER.error("Falló el job de escalamiento SLA; traceId={}", traceId, exception);
		}
	}
}
