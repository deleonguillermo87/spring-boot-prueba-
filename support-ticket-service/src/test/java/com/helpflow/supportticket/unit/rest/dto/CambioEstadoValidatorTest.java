package com.helpflow.supportticket.unit.rest.dto;

import java.util.Set;

import com.helpflow.supportticket.domain.model.EstadoTicket;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.dto.CambiarEstadoRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CambioEstadoValidatorTest {

	private Validator validator;

	@BeforeEach
	void setUp() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	@Test
	void observacionOpcionalParaEnProgreso() {
		CambiarEstadoRequest request = new CambiarEstadoRequest(EstadoTicket.EN_PROGRESO, null);
		Set<ConstraintViolation<CambiarEstadoRequest>> violations = validator.validate(request);
		assertTrue(violations.isEmpty());
	}

	@Test
	void observacionObligatoriaParaResuelto() {
		CambiarEstadoRequest requestSinObs = new CambiarEstadoRequest(EstadoTicket.RESUELTO, null);
		Set<ConstraintViolation<CambiarEstadoRequest>> violations1 = validator.validate(requestSinObs);
		assertFalse(violations1.isEmpty());

		CambiarEstadoRequest requestVacia = new CambiarEstadoRequest(EstadoTicket.RESUELTO, "   ");
		Set<ConstraintViolation<CambiarEstadoRequest>> violations2 = validator.validate(requestVacia);
		assertFalse(violations2.isEmpty());

		CambiarEstadoRequest requestConObs = new CambiarEstadoRequest(EstadoTicket.RESUELTO, "Problema resuelto exitosamente");
		Set<ConstraintViolation<CambiarEstadoRequest>> violations3 = validator.validate(requestConObs);
		assertTrue(violations3.isEmpty());
	}

	@Test
	void observacionObligatoriaParaCancelado() {
		CambiarEstadoRequest requestSinObs = new CambiarEstadoRequest(EstadoTicket.CANCELADO, null);
		Set<ConstraintViolation<CambiarEstadoRequest>> violations1 = validator.validate(requestSinObs);
		assertFalse(violations1.isEmpty());

		CambiarEstadoRequest requestVacia = new CambiarEstadoRequest(EstadoTicket.CANCELADO, "   ");
		Set<ConstraintViolation<CambiarEstadoRequest>> violations2 = validator.validate(requestVacia);
		assertFalse(violations2.isEmpty());

		CambiarEstadoRequest requestConObs = new CambiarEstadoRequest(EstadoTicket.CANCELADO, "Cancelado por el usuario");
		Set<ConstraintViolation<CambiarEstadoRequest>> violations3 = validator.validate(requestConObs);
		assertTrue(violations3.isEmpty());
	}
}

