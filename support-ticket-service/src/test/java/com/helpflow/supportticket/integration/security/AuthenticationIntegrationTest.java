package com.helpflow.supportticket.integration.security;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helpflow.supportticket.application.port.out.TechnicianDispatchPort;
import com.helpflow.supportticket.integration.config.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AuthenticationIntegrationTest extends PostgresIntegrationTest {

	@Autowired MockMvc mvc;
	private final ObjectMapper objectMapper = new ObjectMapper();
	@Autowired JwtEncoder jwtEncoder;
	@MockitoBean TechnicianDispatchPort dispatch;

	@Test
	void registroLoginYValidacionJwtReales() throws Exception {
		mvc.perform(get("/tickets"))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith("application/problem+json"));

		String email = "auth-" + UUID.randomUUID() + "@helpflow.test";
		mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("documento", "DOC-" + UUID.randomUUID(), "nombre", "Usuario Auth",
						"correoCorporativo", email, "sede", "NORTE", "area", "SOPORTE", "password", "clave-segura"))))
				.andExpect(status().isCreated());

		JsonNode login = objectMapper.readTree(mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("correoCorporativo", email, "password", "clave-segura"))))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
		String token = login.get("token").asText();
		mvc.perform(get("/tickets").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());

		mvc.perform(get("/tickets").header("Authorization", "Bearer token-invalido"))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith("application/problem+json"));

		Instant now = Instant.now();
		String expired = jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), JwtClaimsSet.builder()
				.issuer("helpflow-test").subject(login.get("usuarioId").asText())
				.issuedAt(now.minusSeconds(120)).expiresAt(now.minusSeconds(60))
				.claim("rol", "ROLE_USUARIO").build())).getTokenValue();
		mvc.perform(get("/tickets").header("Authorization", "Bearer " + expired))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith("application/problem+json"));
	}

	private String json(Object value) throws Exception { return objectMapper.writeValueAsString(value); }
}
