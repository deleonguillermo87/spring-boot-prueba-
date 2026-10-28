package com.helpflow.supportticket.integration.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class GlobalExceptionHandlerIntegrationTest extends PostgresIntegrationTest {

	@Autowired private MockMvc mvc;
	@Autowired private ApplicationContext context;

	@Test
	void contextoCompletoTieneBeansConectados() {
		assertThat(context.getBeanDefinitionCount()).isPositive();
		assertThat(context.getBean("globalExceptionHandler")).isNotNull();
	}

	@Test
	void healthEsPublicoYRespondeUp() throws Exception {
		mvc.perform(get("/actuator/health")).andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"));
	}

	@Test
	void ticketsSinTokenDevuelveProblemConTraceId() throws Exception {
		mvc.perform(get("/tickets").header("X-Trace-Id", "integration-trace"))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith("application/problem+json"))
				.andExpect(jsonPath("$.traceId").value("integration-trace"));
	}
}
