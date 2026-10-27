package com.helpflow.supportticket.infrastructure.adapter.in.rest.dto.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = CambioEstadoValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCambioEstado {

	String message() default "La observación es obligatoria cuando el estado destino es RESUELTO o CANCELADO.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}

