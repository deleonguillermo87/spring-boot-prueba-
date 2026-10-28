package com.helpflow.supportticket.infrastructure.adapter.in.rest.exception;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.helpflow.supportticket.application.port.out.MetricasPort;
import com.helpflow.supportticket.domain.exception.AccesoNoPermitidoException;
import com.helpflow.supportticket.domain.exception.SlaInvalidoException;
import com.helpflow.supportticket.domain.exception.TecnicoNoDisponibleException;
import com.helpflow.supportticket.domain.exception.TicketNoEncontradoException;
import com.helpflow.supportticket.domain.exception.TransicionInvalidaException;
import com.helpflow.supportticket.domain.exception.UsuarioInactivoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.http.ResponseEntity;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	private final MetricasPort metricas;

	public GlobalExceptionHandler(MetricasPort metricas) {
		this.metricas = metricas;
	}

	@ExceptionHandler(TicketNoEncontradoException.class)
	ResponseEntity<Object> ticketNoEncontrado(TicketNoEncontradoException ex, HttpServletRequest request) {
		return response(HttpStatus.NOT_FOUND, "ticket-no-encontrado", "Ticket no encontrado", ex.getMessage(), request);
	}

	@ExceptionHandler(TransicionInvalidaException.class)
	ResponseEntity<Object> transicionInvalida(TransicionInvalidaException ex, HttpServletRequest request) {
		return response(HttpStatus.CONFLICT, "transicion-invalida", "Transición inválida", ex.getMessage(), request);
	}

	@ExceptionHandler(TecnicoNoDisponibleException.class)
	ResponseEntity<Object> tecnicoNoDisponible(TecnicoNoDisponibleException ex, HttpServletRequest request) {
		return response(HttpStatus.CONFLICT, "tecnico-no-disponible", "Técnico no disponible", ex.getMessage(), request);
	}

	@ExceptionHandler(UsuarioInactivoException.class)
	ResponseEntity<Object> usuarioInactivo(UsuarioInactivoException ex, HttpServletRequest request) {
		return response(HttpStatus.FORBIDDEN, "usuario-inactivo", "Usuario inactivo", ex.getMessage(), request);
	}

	@ExceptionHandler(AccesoNoPermitidoException.class)
	ResponseEntity<Object> accesoNoPermitido(AccesoNoPermitidoException ex, HttpServletRequest request) {
		metricas.accesoDenegado();
		return response(HttpStatus.FORBIDDEN, "acceso-no-permitido", "Acceso no permitido", ex.getMessage(), request);
	}

	@ExceptionHandler(SlaInvalidoException.class)
	ResponseEntity<Object> slaInvalido(SlaInvalidoException ex, HttpServletRequest request) {
		return response(HttpStatus.UNPROCESSABLE_ENTITY, "sla-invalido", "SLA inválido", ex.getMessage(), request);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	ResponseEntity<Object> constraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
		List<ErrorCampo> errors = ex.getConstraintViolations().stream()
				.map(v -> new ErrorCampo(v.getPropertyPath().toString(), v.getMessage())).toList();
		return validationResponse(errors, request);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	ResponseEntity<Object> integridad(DataIntegrityViolationException ex, HttpServletRequest request) {
		return response(HttpStatus.CONFLICT, "conflicto-integridad", "Conflicto de datos",
				"La operación entra en conflicto con los datos existentes.", request);
	}

	@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
	ResponseEntity<Object> optimisticLock(ObjectOptimisticLockingFailureException ex, HttpServletRequest request) {
		return response(HttpStatus.CONFLICT, "conflicto-concurrencia", "Conflicto de concurrencia",
				"El recurso fue modificado por otra operación.", request);
	}

	@ExceptionHandler(AccessDeniedException.class)
	ResponseEntity<Object> denied(AccessDeniedException ex, HttpServletRequest request) {
		metricas.accesoDenegado();
		return response(HttpStatus.FORBIDDEN, "acceso-denegado", "Acceso denegado",
				"No tiene permiso para acceder al recurso.", request);
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<Object> unexpected(Exception ex, HttpServletRequest request) {
		String traceId = traceId();
		LOGGER.error("Error inesperado; traceId={}", traceId, ex);
		return response(HttpStatus.INTERNAL_SERVER_ERROR, "error-interno", "Error interno",
				"Ocurrió un error interno.", request, traceId);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		List<ErrorCampo> errors = ex.getBindingResult().getFieldErrors().stream()
				.map(e -> new ErrorCampo(e.getField(), e.getDefaultMessage())).toList();
		return validationResponse(errors, servletRequest(request));
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		return response(HttpStatus.BAD_REQUEST, "solicitud-malformada", "Solicitud malformada",
				"El cuerpo de la solicitud no es válido.", servletRequest(request));
	}

	private ResponseEntity<Object> validationResponse(List<ErrorCampo> errors, HttpServletRequest request) {
		ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "validacion-fallida", "Error de validación",
				"Uno o más campos no son válidos.", request, traceId());
		problem.setProperty("errores", errors);
		return entity(problem);
	}

	private ResponseEntity<Object> response(HttpStatus status, String code, String title, String detail,
			HttpServletRequest request) {
		return response(status, code, title, detail, request, traceId());
	}

	private ResponseEntity<Object> response(HttpStatus status, String code, String title, String detail,
			HttpServletRequest request, String traceId) {
		return entity(problem(status, code, title, detail, request, traceId));
	}

	private ProblemDetail problem(HttpStatus status, String code, String title, String detail,
			HttpServletRequest request, String traceId) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setType(java.net.URI.create("urn:helpflow:problem:" + code));
		problem.setTitle(title);
		problem.setInstance(java.net.URI.create(request.getRequestURI()));
		problem.setProperty("timestamp", Instant.now());
		problem.setProperty("traceId", traceId);
		return problem;
	}

	private ResponseEntity<Object> entity(ProblemDetail problem) {
		return ResponseEntity.status(problem.getStatus()).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(problem);
	}

	private HttpServletRequest servletRequest(WebRequest request) {
		return ((ServletWebRequest) request).getRequest();
	}

	private String traceId() {
		String traceId = MDC.get("traceId");
		return traceId == null || traceId.isBlank() ? UUID.randomUUID().toString() : traceId;
	}

	private record ErrorCampo(String campo, String mensaje) { }
}
