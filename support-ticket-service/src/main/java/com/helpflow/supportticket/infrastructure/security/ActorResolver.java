package com.helpflow.supportticket.infrastructure.security;

import java.util.Objects;

import com.helpflow.supportticket.application.dto.Actor;
import com.helpflow.supportticket.domain.model.Rol;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class ActorResolver {

	public Actor resolver(Jwt jwt) {
		Objects.requireNonNull(jwt, "jwt es obligatorio");
		Long id;
		try {
			id = Long.valueOf(jwt.getSubject());
		} catch (NumberFormatException exception) {
			throw new IllegalArgumentException("El subject del token debe ser un id numérico.", exception);
		}
		String rol = jwt.getClaimAsString("rol");
		try {
			return new Actor(id, Rol.valueOf(rol));
		} catch (IllegalArgumentException | NullPointerException exception) {
			throw new IllegalArgumentException("El token contiene un rol inválido.", exception);
		}
	}
}
