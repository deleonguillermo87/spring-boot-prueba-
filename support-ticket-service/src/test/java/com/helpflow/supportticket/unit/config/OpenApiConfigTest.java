package com.helpflow.supportticket.unit.config;

import com.helpflow.supportticket.infrastructure.config.OpenApiConfig;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OpenApiConfigTest {

	@Test
	void openApiConfigurationMatchesSpecifications() {
		OpenApiConfig config = new OpenApiConfig();
		OpenAPI openAPI = config.helpFlowOpenAPI();

		assertNotNull(openAPI);
		assertNotNull(openAPI.getInfo());
		assertEquals("HelpFlow", openAPI.getInfo().getTitle());

		assertNotNull(openAPI.getComponents());
		assertNotNull(openAPI.getComponents().getSecuritySchemes());
		SecurityScheme scheme = openAPI.getComponents().getSecuritySchemes().get("bearerAuth");
		assertNotNull(scheme);
		assertEquals(SecurityScheme.Type.HTTP, scheme.getType());
		assertEquals("bearer", scheme.getScheme());
		assertEquals("JWT", scheme.getBearerFormat());

		assertNotNull(openAPI.getSecurity());
		assertFalse(openAPI.getSecurity().isEmpty());
		assertNotNull(openAPI.getSecurity().getFirst().get("bearerAuth"));
	}
}

