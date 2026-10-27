package com.helpflow.supportticket.unit.client;

import java.net.SocketTimeoutException;

import com.helpflow.supportticket.application.port.out.DespachoSugerido;
import com.helpflow.supportticket.application.port.out.DispatchUnavailableException;
import com.helpflow.supportticket.domain.model.Categoria;
import com.helpflow.supportticket.domain.model.Disponibilidad;
import com.helpflow.supportticket.domain.model.Prioridad;
import com.helpflow.supportticket.infrastructure.adapter.out.client.TechnicianDispatchRestAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TechnicianDispatchRestAdapterTest {

	private MockRestServiceServer mockServer;
	private TechnicianDispatchRestAdapter adapter;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:8081");
		mockServer = MockRestServiceServer.bindTo(builder).build();
		RestClient restClient = builder.build();
		adapter = new TechnicianDispatchRestAdapter(restClient);
	}

	@Test
	void despacharRespuestaCorrectaRetornaDespachoSugerido() {
		String responseJson = """
				{
					"ticketId": 10,
					"tecnicoId": "TEC-014",
					"disponibilidad": "MEDIA",
					"tiempoEstimadoHoras": 4
				}
				""";

		mockServer.expect(requestTo("http://localhost:8081/technician-dispatch"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

		DespachoSugerido resultado = adapter.despachar(10L, Categoria.RED, Prioridad.ALTA);

		assertNotNull(resultado);
		assertEquals("TEC-014", resultado.tecnicoCodigo());
		assertEquals(Disponibilidad.MEDIA, resultado.disponibilidad());
		assertEquals(4, resultado.tiempoEstimadoHoras());
		mockServer.verify();
	}

	@Test
	void despacharError500LanzaDispatchUnavailableException() {
		mockServer.expect(requestTo("http://localhost:8081/technician-dispatch"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withServerError());

		assertThrows(DispatchUnavailableException.class, () ->
				adapter.despachar(10L, Categoria.RED, Prioridad.ALTA));
		mockServer.verify();
	}

	@Test
	void despacharTimeoutLanzaDispatchUnavailableException() {
		mockServer.expect(requestTo("http://localhost:8081/technician-dispatch"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withException(new SocketTimeoutException("Read timed out")));

		assertThrows(DispatchUnavailableException.class, () ->
				adapter.despachar(10L, Categoria.RED, Prioridad.ALTA));
		mockServer.verify();
	}
}

