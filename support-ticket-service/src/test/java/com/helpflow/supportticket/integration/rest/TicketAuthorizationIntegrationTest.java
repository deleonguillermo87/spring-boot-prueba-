package com.helpflow.supportticket.integration.rest;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helpflow.supportticket.application.port.out.DespachoSugerido;
import com.helpflow.supportticket.application.port.out.DispatchUnavailableException;
import com.helpflow.supportticket.application.port.out.TechnicianDispatchPort;
import com.helpflow.supportticket.domain.model.Disponibilidad;
import com.helpflow.supportticket.integration.config.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class TicketAuthorizationIntegrationTest extends PostgresIntegrationTest {

	@Autowired MockMvc mvc;
	private final ObjectMapper objectMapper = new ObjectMapper();
	@Autowired JdbcTemplate jdbc;
	@Autowired PasswordEncoder passwordEncoder;
	@MockitoBean TechnicianDispatchPort dispatch;

	@Test
	void controlaTicketsPorRolYPersisteEstadoEHistorial() throws Exception {
		when(dispatch.despachar(anyLong(), any(), any())).thenReturn(
				new DespachoSugerido("TEC-001", Disponibilidad.ALTA, 1));
		String user1Email = email("uno");
		String user2Email = email("dos");
		String tech1Email = email("tecuno");
		String tech2Email = email("tecdos");
		String adminEmail = email("admin");
		register(user1Email, "DOC-U1", null, null);
		register(user2Email, "DOC-U2", null, null);
		register(tech1Email, "TEC-DOC-001", "TECNICO", "TEC-001");
		register(tech2Email, "TEC-DOC-005", "TECNICO", "TEC-005");
		jdbc.update("insert into usuarios(documento,nombre,correo_corporativo,sede,area,estado,rol,password_hash) values(?,?,?,?,?,?,?,?)",
				"DOC-ADMIN-" + UUID.randomUUID(), "Admin Integración", adminEmail, "CENTRAL", "ADMINISTRACION",
				"ACTIVO", "ROLE_ADMIN", passwordEncoder.encode("clave-admin"));

		String user1 = login(user1Email, "clave-test");
		String user2 = login(user2Email, "clave-test");
		String tech1 = login(tech1Email, "clave-test");
		String tech2 = login(tech2Email, "clave-test");
		String admin = login(adminEmail, "clave-admin");

		long firstTicket = createTicket(user1, "Ticket propietario uno");
		mvc.perform(get("/tickets/{id}", firstTicket).header("Authorization", bearer(user1)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(firstTicket));
		assertThat(mvc.perform(get("/tickets").header("Authorization", bearer(user1)))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString())
				.contains("Ticket propietario uno");
		mvc.perform(get("/tickets").header("Authorization", bearer(user2)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.contenido").isEmpty());
		mvc.perform(get("/tickets/{id}", firstTicket).header("Authorization", bearer(user2)))
				.andExpect(status().isForbidden());

		when(dispatch.despachar(anyLong(), any(), any())).thenReturn(
				new DespachoSugerido("TEC-005", Disponibilidad.ALTA, 1));
		long secondTicket = createTicket(user2, "Ticket propietario dos");
		mvc.perform(get("/tickets").header("Authorization", bearer(tech1)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.contenido.length()").value(1))
				.andExpect(jsonPath("$.contenido[0].id").value(firstTicket));
		mvc.perform(get("/tickets").header("Authorization", bearer(tech2)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.contenido.length()").value(1))
				.andExpect(jsonPath("$.contenido[0].id").value(secondTicket));
		mvc.perform(patch("/tickets/{id}/estado", firstTicket).header("Authorization", bearer(tech2))
				.contentType(MediaType.APPLICATION_JSON).content(json(Map.of("estado", "EN_PROGRESO", "observacion", "No autorizado"))))
				.andExpect(status().isForbidden());
		mvc.perform(patch("/tickets/{id}/estado", firstTicket).header("Authorization", bearer(tech1))
				.contentType(MediaType.APPLICATION_JSON).content(json(Map.of("estado", "EN_PROGRESO", "observacion", "Inicio atención"))))
				.andExpect(status().isOk());

		mvc.perform(get("/tickets").header("Authorization", bearer(admin)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.contenido.length()").value(2));
		mvc.perform(put("/tickets/{id}/asignacion", firstTicket).header("Authorization", bearer(admin))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("codigoTecnico", "TEC-005", "observacion", "Reasignación de prueba"))))
				.andExpect(status().isOk()).andExpect(jsonPath("$.tecnicoAsignadoCodigo").value("TEC-005"));
		mvc.perform(get("/admin/sla").header("Authorization", bearer(user1)))
				.andExpect(status().isForbidden());
		mvc.perform(post("/admin/tickets/escalar").header("Authorization", bearer(user1)))
				.andExpect(status().isForbidden());

		mvc.perform(patch("/tickets/{id}/estado", firstTicket).header("Authorization", bearer(admin))
				.contentType(MediaType.APPLICATION_JSON).content(json(Map.of("estado", "ASIGNADO", "observacion", "Transición inválida"))))
				.andExpect(status().isConflict())
				.andExpect(content().contentTypeCompatibleWith("application/problem+json"));
		mvc.perform(post("/tickets").header("Authorization", bearer(user1)).contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("titulo", "", "descripcion", "prueba", "categoria", "HARDWARE",
						"prioridad", "BAJA", "sede", "NORTE"))))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.errores").isArray());

		when(dispatch.despachar(anyLong(), any(), any()))
				.thenThrow(new DispatchUnavailableException("dispatch caído"));
		long unassignedTicket = createTicket(user1, "Ticket sin dispatch");
		assertThat(jdbc.queryForObject("select estado from tickets where id=?", String.class, unassignedTicket))
				.isEqualTo("ABIERTO");
		assertThat(jdbc.queryForObject("select tecnico_id from tickets where id=?", Long.class, unassignedTicket))
				.isNull();
		assertThat(jdbc.queryForObject("select count(*) from historial_ticket where ticket_id=?", Integer.class, unassignedTicket))
				.isGreaterThan(0);
		assertThat(jdbc.queryForObject("select estado from tickets where id=?", String.class, firstTicket))
				.isEqualTo("EN_PROGRESO");
		assertThat(jdbc.queryForObject("select count(*) from historial_ticket where ticket_id=?", Integer.class, firstTicket))
				.isGreaterThanOrEqualTo(3);
	}

	private long createTicket(String token, String title) throws Exception {
		MvcResult result = mvc.perform(post("/tickets").header("Authorization", bearer(token))
				.contentType(MediaType.APPLICATION_JSON).content(json(Map.of("titulo", title,
						"descripcion", "Descripción para integración", "categoria", "HARDWARE",
						"prioridad", "MEDIA", "sede", "NORTE"))))
				.andExpect(status().isCreated()).andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
	}

	private void register(String email, String document, String role, String technicianCode) throws Exception {
		Map<String, Object> body = new java.util.HashMap<>();
		body.put("documento", document); body.put("nombre", "Integración " + document);
		body.put("correoCorporativo", email); body.put("sede", "NORTE"); body.put("area", "SOPORTE");
		body.put("password", "clave-test");
		if (role != null) { body.put("rol", role); body.put("codigoTecnico", technicianCode); }
		mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(json(body)))
				.andExpect(status().isCreated());
	}

	private String login(String email, String password) throws Exception {
		String body = json(Map.of("correoCorporativo", email, "password", password));
		JsonNode response = objectMapper.readTree(mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(body)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
		return response.get("token").asText();
	}

	private String email(String label) { return label + "-" + UUID.randomUUID() + "@helpflow.test"; }
	private String bearer(String token) { return "Bearer " + token; }
	private String json(Object value) throws Exception { return objectMapper.writeValueAsString(value); }
}
