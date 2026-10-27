package com.helpflow.supportticket.unit.application.usecase;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.helpflow.supportticket.application.dto.Actor;
import com.helpflow.supportticket.domain.model.Categoria;
import com.helpflow.supportticket.domain.model.EstadoTicket;
import com.helpflow.supportticket.domain.model.EstadoUsuario;
import com.helpflow.supportticket.domain.model.Prioridad;
import com.helpflow.supportticket.domain.model.Rol;
import com.helpflow.supportticket.domain.model.Tecnico;
import com.helpflow.supportticket.domain.model.Ticket;
import com.helpflow.supportticket.domain.model.Usuario;

final class ApplicationTestFixtures {

	static final Instant AHORA = Instant.parse("2026-01-01T10:00:00Z");
	static final Clock CLOCK = Clock.fixed(AHORA, ZoneOffset.UTC);
	static final Map<Prioridad, Duration> SLA = Map.of(
			Prioridad.CRITICA, Duration.ofHours(4),
			Prioridad.ALTA, Duration.ofHours(8),
			Prioridad.MEDIA, Duration.ofHours(24),
			Prioridad.BAJA, Duration.ofHours(72));

	private ApplicationTestFixtures() {
	}

	static Actor usuarioActor() {
		return new Actor(1L, Rol.ROLE_USUARIO);
	}

	static Actor tecnicoActor() {
		return new Actor(14L, Rol.ROLE_TECNICO);
	}

	static Actor adminActor() {
		return new Actor(99L, Rol.ROLE_ADMIN);
	}

	static Usuario usuario() {
		return usuario(1L, EstadoUsuario.ACTIVO);
	}

	static Usuario usuario(long id, EstadoUsuario estado) {
		return new Usuario(
				id, "DOC-" + id, "Usuario " + id, "user" + id + "@helpflow.com",
				"CENTRAL", "TI", estado, Rol.ROLE_USUARIO, "hashed-password");
	}

	static Tecnico tecnico(long id, String codigo, int cargaMaxima, int activos, EstadoUsuario estado) {
		return new Tecnico(
				id, codigo, "TEC-DOC-" + id, "Técnico " + codigo, "CENTRAL",
				Set.of(Categoria.RED), cargaMaxima, activos, estado);
	}

	static Tecnico tecnicoDisponible() {
		return tecnico(14L, "TEC-014", 5, 1, EstadoUsuario.ACTIVO);
	}

	static Ticket ticket(EstadoTicket estado, Instant fechaLimite) {
		return new Ticket(
				10L, usuario(), "Sin conexión", "Descripción", Categoria.RED,
				Prioridad.ALTA, "CENTRAL", null, AHORA, fechaLimite,
				estado, estado == EstadoTicket.ABIERTO ? null : tecnicoDisponible(),
				List.of(), CLOCK);
	}
}
