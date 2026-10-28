package com.helpflow.supportticket.unit.rest;

import com.helpflow.supportticket.application.port.out.MetricasPort;
import com.helpflow.supportticket.domain.exception.*;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.exception.GlobalExceptionHandler;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.helpflow.supportticket.infrastructure.observability.TraceIdFilter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

	private final MetricasPort metricas = mock(MetricasPort.class);
	private MockMvc mvc;

	@BeforeEach
	void setUp() {
		mvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
				.addFilters(new TraceIdFilter())
				.setControllerAdvice(new GlobalExceptionHandler(metricas)).build();
	}

	@Test void ticketNoEncontrado() throws Exception { assertProblem("ticket", 404, "ticket-no-encontrado"); }
	@Test void transicionInvalida() throws Exception { assertProblem("transicion", 409, "transicion-invalida"); }
	@Test void tecnicoNoDisponible() throws Exception { assertProblem("tecnico", 409, "tecnico-no-disponible"); }
	@Test void usuarioInactivo() throws Exception { assertProblem("usuario", 403, "usuario-inactivo"); }
	@Test void accesoNoPermitido() throws Exception { assertProblem("acceso", 403, "acceso-no-permitido"); verify(metricas).accesoDenegado(); }
	@Test void slaInvalido() throws Exception { assertProblem("sla", 422, "sla-invalido"); }
	@Test void constraintViolation() throws Exception { assertProblem("constraint", 400, "validacion-fallida"); }
	@Test void dataIntegrity() throws Exception { assertProblem("integridad", 409, "conflicto-integridad"); }
	@Test void optimisticLock() throws Exception { assertProblem("optimistic", 409, "conflicto-concurrencia"); }
	@Test void accessDenied() throws Exception { assertProblem("denied", 403, "acceso-denegado"); verify(metricas).accesoDenegado(); }
	@Test void excepcionDesconocida() throws Exception { assertProblem("unexpected", 500, "error-interno"); }
	@Test void jsonMalformado() throws Exception {
		mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/malformed")
					.contentType("application/json").content("{"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType("application/problem+json"))
				.andExpect(jsonPath("$.type").value("urn:helpflow:problem:solicitud-malformada"))
				.andExpect(jsonPath("$.traceId").exists());
	}
	@Test void argumentoNoValidoIncluyeErrores() throws Exception {
		mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/invalid")
					.contentType("application/json").content("{\"name\":\"\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errores[0].campo").value("name"))
				.andExpect(jsonPath("$.errores[0].mensaje").exists());
	}

	private void assertProblem(String path, int status, String code) throws Exception {
		mvc.perform(get("/" + path).header("X-Trace-Id", "trace-test"))
				.andExpect(status().is(status))
				.andExpect(content().contentType("application/problem+json"))
				.andExpect(jsonPath("$.type").value("urn:helpflow:problem:" + code))
				.andExpect(jsonPath("$.title").exists())
				.andExpect(jsonPath("$.status").value(status))
				.andExpect(jsonPath("$.detail").exists())
				.andExpect(jsonPath("$.instance").exists())
				.andExpect(jsonPath("$.timestamp").exists())
				.andExpect(jsonPath("$.traceId").value("trace-test"));
	}

	@RestController
	static class ThrowingController {
		@GetMapping("/ticket") void ticket() { throw new TicketNoEncontradoException("missing"); }
		@GetMapping("/transicion") void transition() { throw new TransicionInvalidaException("invalid"); }
		@GetMapping("/tecnico") void technician() { throw new TecnicoNoDisponibleException("busy"); }
		@GetMapping("/usuario") void user() { throw new UsuarioInactivoException("inactive"); }
		@GetMapping("/acceso") void access() { throw new AccesoNoPermitidoException("no"); }
		@GetMapping("/sla") void sla() { throw new SlaInvalidoException("bad"); }
		@GetMapping("/constraint") void constraint() { throw new ConstraintViolationException(java.util.Set.of()); }
		@GetMapping("/integridad") void integrity() { throw new DataIntegrityViolationException("secret SQL"); }
		@GetMapping("/optimistic") void optimistic() { throw new ObjectOptimisticLockingFailureException(Object.class, 1); }
		@GetMapping("/denied") void denied() { throw new AccessDeniedException("no"); }
		@GetMapping("/unexpected") void unexpected() { throw new IllegalStateException("secret"); }
		@PostMapping("/malformed") void malformed(@RequestBody Payload body) { }
		@PostMapping("/invalid") void invalid(@Valid @RequestBody Payload body) { }
	}
	static class Payload { @NotBlank public String name; }
}
