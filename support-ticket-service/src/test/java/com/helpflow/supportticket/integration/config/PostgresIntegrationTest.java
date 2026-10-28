package com.helpflow.supportticket.integration.config;

import java.time.Clock;

import com.helpflow.supportticket.SupportTicketApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


@Testcontainers
@SpringBootTest(classes = SupportTicketApplication.class)
@TestPropertySource(properties = "helpflow.sla.escalation.enabled=false")
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

	}
}
