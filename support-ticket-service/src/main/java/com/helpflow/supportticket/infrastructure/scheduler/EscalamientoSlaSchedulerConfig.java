package com.helpflow.supportticket.infrastructure.scheduler;

import com.helpflow.supportticket.application.port.in.EscalarTicketsUseCase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(EscalamientoSlaSchedulerConfig.EscalationProperties.class)
public class EscalamientoSlaSchedulerConfig {

	@Bean
	@ConditionalOnProperty(prefix = "helpflow.sla.escalation", name = "enabled", havingValue = "true", matchIfMissing = true)
	EscalamientoSlaScheduler escalamientoSlaScheduler(
			EscalarTicketsUseCase escalarTickets, EscalationProperties properties) {
		return new EscalamientoSlaScheduler(escalarTickets, properties.umbralMinutos());
	}

	@ConfigurationProperties("helpflow.sla.escalation")
	public record EscalationProperties(@DefaultValue("60") long umbralMinutos) { }
}
