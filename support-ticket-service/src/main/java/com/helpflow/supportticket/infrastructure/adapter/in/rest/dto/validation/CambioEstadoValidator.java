package com.helpflow.supportticket.infrastructure.adapter.in.rest.dto.validation;

import com.helpflow.supportticket.domain.model.EstadoTicket;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.dto.CambiarEstadoRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CambioEstadoValidator implements ConstraintValidator<ValidCambioEstado, CambiarEstadoRequest> {

	@Override
	public boolean isValid(CambiarEstadoRequest value, ConstraintValidatorContext context) {
		if (value == null || value.estado() == null) {
			return true;
		}

		if (value.estado() == EstadoTicket.RESUELTO || value.estado() == EstadoTicket.CANCELADO) {
			if (value.observacion() == null || value.observacion().isBlank()) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("La observación es obligatoria cuando el estado destino es RESUELTO o CANCELADO.")
						.addPropertyNode("observacion")
						.addConstraintViolation();
				return false;
			}
		}

		return true;
	}
}

