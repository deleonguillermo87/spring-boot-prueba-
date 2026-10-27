package com.helpflow.dispatch.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DispatchController.class)
@Import(com.helpflow.dispatch.service.DispatchService.class)
class DispatchControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void returnsDispatchForValidRequest() throws Exception {
		mockMvc.perform(post("/technician-dispatch")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"ticketId":125,"categoria":"RED","prioridad":"ALTA"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.ticketId").value(125))
				.andExpect(jsonPath("$.tecnicoId").value(
						org.hamcrest.Matchers.matchesRegex("TEC-\\d{3}")))
				.andExpect(jsonPath("$.disponibilidad").isNotEmpty())
				.andExpect(jsonPath("$.tiempoEstimadoHoras").isNumber());
	}

	@Test
	void returnsProblemDetailWhenTicketIdIsNull() throws Exception {
		mockMvc.perform(post("/technician-dispatch")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"ticketId":null,"categoria":"RED","prioridad":"ALTA"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.type").value("about:blank"))
				.andExpect(jsonPath("$.title").value("Bad Request"))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.detail").isNotEmpty())
				.andExpect(jsonPath("$.instance").value("/technician-dispatch"))
				.andExpect(jsonPath("$.timestamp").isNotEmpty());
	}

	@Test
	void returnsProblemDetailWhenCategoryIsUnknown() throws Exception {
		mockMvc.perform(post("/technician-dispatch")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"ticketId":125,"categoria":"INEXISTENTE","prioridad":"ALTA"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.type").value("about:blank"))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.instance").value("/technician-dispatch"))
				.andExpect(jsonPath("$.timestamp").isNotEmpty());
	}
}
