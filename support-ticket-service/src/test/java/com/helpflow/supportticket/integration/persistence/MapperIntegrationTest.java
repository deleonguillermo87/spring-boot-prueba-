package com.helpflow.supportticket.integration.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import com.helpflow.supportticket.domain.model.Asignacion;
import com.helpflow.supportticket.domain.model.Categoria;
import com.helpflow.supportticket.domain.model.Disponibilidad;
import com.helpflow.supportticket.domain.model.EstadoTicket;
import com.helpflow.supportticket.domain.model.EstadoUsuario;
import com.helpflow.supportticket.domain.model.HistorialTicket;
import com.helpflow.supportticket.domain.model.Prioridad;
import com.helpflow.supportticket.domain.model.Rol;
import com.helpflow.supportticket.domain.model.Tecnico;
import com.helpflow.supportticket.domain.model.Ticket;
import com.helpflow.supportticket.domain.model.Usuario;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.entity.AsignacionEntity;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.entity.HistorialTicketEntity;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.entity.SlaConfigEntity;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.entity.TecnicoEntity;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.entity.TicketEntity;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.mapper.AsignacionMapper;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.mapper.HistorialTicketMapper;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.mapper.SlaConfigMapper;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.mapper.TecnicoMapper;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.mapper.TicketMapper;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.mapper.UsuarioMapper;
import com.helpflow.supportticket.integration.config.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MapperIntegrationTest extends PostgresIntegrationTest {

	private static final Instant NOW = Instant.parse("2026-03-01T10:00:00Z");

	@Autowired UsuarioMapper usuarioMapper;
	@Autowired TecnicoMapper tecnicoMapper;
	@Autowired HistorialTicketMapper historialMapper;
	@Autowired TicketMapper ticketMapper;
	@Autowired AsignacionMapper asignacionMapper;
	@Autowired SlaConfigMapper slaConfigMapper;

	@Test
	void mapsUsuarioBothWays() {
		Usuario usuario = usuario();
		UsuarioEntity entity = usuarioMapper.toEntity(usuario);
		assertEquals(usuario.getCorreoCorporativo(), usuarioMapper.toDomain(entity).getCorreoCorporativo());
	}

	@Test
	void mapsTecnicoAndAssociatedUserId() {
		UsuarioEntity account = usuarioMapper.toEntity(usuario());
		account.setId(701L);
		TecnicoEntity entity = new TecnicoEntity();
		entity.setId(14L);
		entity.setUsuario(account);
		entity.setCodigo("TEC-014");
		entity.setDocumento("DOC-014");
		entity.setNombre("Técnico");
		entity.setSede("CENTRAL");
		entity.setEspecialidades(Set.of(Categoria.RED));
		entity.setCargaMaxima(4);
		entity.setEstado(EstadoUsuario.ACTIVO);

		Tecnico tecnico = tecnicoMapper.toDomain(entity, 2);

		assertEquals(14L, tecnico.getId());
		assertEquals(701L, tecnico.getUsuarioId());
		assertEquals(2, tecnico.getTicketsActivos());
	}

	@Test
	void mapsTicketHistoryBothWays() {
		HistorialTicket historial = new HistorialTicket(
				31L, EstadoTicket.ABIERTO, EstadoTicket.ASIGNADO, "Asignado", "admin", NOW);
		HistorialTicketEntity entity = historialMapper.toEntity(historial);
		assertNull(entity.getTicket());
		assertEquals(historial, historialMapper.toDomain(entity));
	}

	@Test
	void mapsTicketAggregateWithoutNeedingMutableDomainSetters() {
		Ticket ticket = new Ticket(
				41L, usuario(), "Título", "Detalle", Categoria.RED, Prioridad.ALTA,
				"CENTRAL", null, NOW, NOW.plusSeconds(28_800), EstadoTicket.ABIERTO,
				null, List.of());
		TicketEntity entity = ticketMapper.toEntity(ticket);
		entity.setSolicitante(usuarioMapper.toEntity(usuario()));

		Ticket mapped = ticketMapper.toDomain(entity);

		assertEquals(ticket.getId(), mapped.getId());
		assertEquals(ticket.getTitulo(), mapped.getTitulo());
		assertEquals(ticket.getCategoria(), mapped.getCategoria());
	}

	@Test
	void mapsAsignacionIds() {
		TicketEntity ticket = new TicketEntity();
		ticket.setId(44L);
		TecnicoEntity tecnico = new TecnicoEntity();
		tecnico.setId(14L);
		AsignacionEntity entity = new AsignacionEntity();
		entity.setId(91L);
		entity.setTicket(ticket);
		entity.setTecnico(tecnico);
		entity.setFecha(NOW);
		entity.setTiempoEstimadoHoras(3);
		entity.setDisponibilidad(Disponibilidad.MEDIA);

		Asignacion mapped = asignacionMapper.toDomain(entity);

		assertEquals(44L, mapped.ticketId());
		assertEquals(14L, mapped.tecnicoId());
	}

	@Test
	void mapsSlaConfigInWholeHours() {
		SlaConfigEntity entity = slaConfigMapper.toEntity(Prioridad.CRITICA, Duration.ofHours(4));

		assertEquals(Prioridad.CRITICA, entity.getPrioridad());
		assertEquals(Duration.ofHours(4), slaConfigMapper.toDuration(entity));
	}

	private Usuario usuario() {
		return new Usuario(
				701L, "DOC-701", "Usuario", "user701@helpflow.test", "CENTRAL", "TI",
				EstadoUsuario.ACTIVO, Rol.ROLE_USUARIO, "hash");
	}
}
