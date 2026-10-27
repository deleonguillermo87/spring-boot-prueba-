package com.helpflow.supportticket.unit.domain;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import com.helpflow.supportticket.domain.model.Categoria;
import com.helpflow.supportticket.domain.model.EstadoTicket;
import com.helpflow.supportticket.domain.model.EstadoUsuario;
import com.helpflow.supportticket.domain.model.Prioridad;
import com.helpflow.supportticket.domain.model.Rol;
import com.helpflow.supportticket.domain.model.Tecnico;
import com.helpflow.supportticket.domain.model.Ticket;
import com.helpflow.supportticket.domain.model.Usuario;

final class DomainTestFixtures {

	static final Instant AHORA = Instant.parse("2026-01-01T10:00:00Z");
	static final Clock RELOJ_FIJO = Clock.fixed(AHORA, ZoneOffset.UTC);

	private DomainTestFixtures() {
	}

	static Usuario usuario() {
		return new Usuario(
				1L, "DOC-1", "Usuario Test", "usuario@helpflow.com", "CENTRAL", "TI",
				EstadoUsuario.ACTIVO, Rol.ROLE_USUARIO, "hash");
	}

	static Tecnico tecnico(
			long id,
			String codigo,
			String sede,
			Set<Categoria> especialidades,
			int cargaMaxima,
			int ticketsActivos,
			EstadoUsuario estado) {
		return new Tecnico(
				id, codigo, "TEC-DOC-" + id, "Técnico " + codigo, sede,
				especialidades, cargaMaxima, ticketsActivos, estado);
	}

	static Ticket ticket(Prioridad prioridad, EstadoTicket estado, Instant fechaLimiteSla) {
		return new Ticket(
				1L, usuario(), "No hay conexión", "Descripción", Categoria.RED, prioridad,
				"CENTRAL", "Router", AHORA, fechaLimiteSla, estado, null, List.of(), RELOJ_FIJO);
	}

	static Tecnico tecnicoDisponible() {
		return tecnico(
				14L, "TEC-014", "CENTRAL", Set.of(Categoria.RED), 5, 1, EstadoUsuario.ACTIVO);
	}
}
