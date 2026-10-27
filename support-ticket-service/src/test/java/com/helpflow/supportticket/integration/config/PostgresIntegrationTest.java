package com.helpflow.supportticket.integration.config;

import java.time.Clock;

import com.helpflow.supportticket.SupportTicketApplication;
import com.helpflow.supportticket.application.port.out.MetricasPort;
import com.helpflow.supportticket.application.port.out.PasswordHasherPort;
import com.helpflow.supportticket.application.port.out.TechnicianDispatchPort;
import com.helpflow.supportticket.application.port.out.TokenProviderPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.Mockito.mock;

@Testcontainers
@SpringBootTest(classes = SupportTicketApplication.class)
@Import(PostgresIntegrationTest.TestBeans.class)
public abstract class PostgresIntegrationTest {

	@Container
	@ServiceConnection
	static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

	@TestConfiguration
	static class TestBeans {

		@Bean
		Clock applicationClock() {
			return Clock.systemUTC();
		}

		@Bean
		TechnicianDispatchPort technicianDispatchPort() {
			return mock(TechnicianDispatchPort.class);
		}

		@Bean
		PasswordHasherPort passwordHasherPort() {
			return mock(PasswordHasherPort.class);
		}

		@Bean
		TokenProviderPort tokenProviderPort() {
			return mock(TokenProviderPort.class);
		}

		@Bean
		MetricasPort metricasPort() {
			return mock(MetricasPort.class);
		}
	}
}
