package com.helpflow.supportticket.application.dto;

import java.util.Objects;

import com.helpflow.supportticket.domain.model.Rol;

public record Actor(Long id, Rol rol) {

	public Actor {
		Objects.requireNonNull(rol, "rol es obligatorio");
	}

	public static Actor sistema() {
		return new Actor(0L, Rol.ROLE_ADMIN);
	}

	public boolean esAdministrador() {
		return rol == Rol.ROLE_ADMIN;
	}
}
