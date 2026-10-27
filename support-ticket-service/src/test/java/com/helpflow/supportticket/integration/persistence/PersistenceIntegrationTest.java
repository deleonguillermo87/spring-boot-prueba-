package com.helpflow.supportticket.integration.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.helpflow.supportticket.application.port.out.AsignacionRepositoryPort;
import com.helpflow.supportticket.application.dto.FiltroTickets;
import com.helpflow.supportticket.application.dto.PaginaSolicitud;
import com.helpflow.supportticket.application.port.out.TecnicoRepositoryPort;
import com.helpflow.supportticket.application.port.out.TicketRepositoryPort;
import com.helpflow.supportticket.application.port.out.UsuarioRepositoryPort;
import com.helpflow.supportticket.domain.model.Categoria;
import com.helpflow.supportticket.domain.model.Asignacion;
import com.helpflow.supportticket.domain.model.Disponibilidad;
import com.helpflow.supportticket.domain.model.EstadoTicket;
import com.helpflow.supportticket.domain.model.EstadoUsuario;
import com.helpflow.supportticket.domain.model.Prioridad;
import com.helpflow.supportticket.domain.model.Rol;
import com.helpflow.supportticket.domain.model.Tecnico;
import com.helpflow.supportticket.domain.model.Ticket;
import com.helpflow.supportticket.domain.model.Usuario;
import com.helpflow.supportticket.integration.config.PostgresIntegrationTest;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

class PersistenceIntegrationTest extends PostgresIntegrationTest {

	@Autowired JdbcTemplate jdbc;
	@Autowired UsuarioRepositoryPort usuarios;
	@Autowired TecnicoRepositoryPort tecnicos;
	@Autowired TicketRepositoryPort tickets;
	@Autowired AsignacionRepositoryPort asignaciones;
	@Autowired EntityManagerFactory entityManagerFactory;
	@Autowired PlatformTransactionManager transactionManager;

	@Test
	void appliesAllMigrationsAndValidatesSchema() {
		assertEquals(3, jdbc.queryForObject(
				"select count(*) from flyway_schema_history where success = true", Integer.class));
		assertEquals(50, jdbc.queryForObject("select count(*) from tecnicos", Integer.class));
		assertEquals(4, jdbc.queryForObject("select count(*) from sla_config", Integer.class));
	}

	@Test
	void savesAndLoadsTicketWithTechnicianAndCascadedHistory() {
		Usuario solicitante = usuarios.guardar(usuario("solicitante"));
		Usuario cuentaTecnico = usuarios.guardar(usuario("tecnico"));
		Tecnico base = tecnicos.buscarPorCodigo("TEC-014").orElseThrow();
		Tecnico tecnico = tecnicos.guardar(new Tecnico(
				base.getId(),
				cuentaTecnico.getId(),
				base.getCodigo(),
				base.getDocumento(),
				base.getNombre(),
				base.getSede(),
				base.getEspecialidades(),
				base.getCargaMaxima(),
				base.getTicketsActivos(),
				base.getEstado()));
		Ticket guardado = tickets.guardar(ticket(solicitante, tecnico, Categoria.ACCESOS));
		Ticket recuperado = tickets.buscarPorId(guardado.getId()).orElseThrow();

		assertEquals(tecnico.getId(), recuperado.getTecnicoAsignado().getId());
		assertEquals(cuentaTecnico.getId(), recuperado.getTecnicoAsignado().getUsuarioId());
		assertEquals(1, recuperado.getHistorial().size());
		assertEquals(1, jdbc.queryForObject(
				"select count(*) from historial_ticket where ticket_id = ?",
				Integer.class,
				guardado.getId()));
	}

	@Test
	void appliesPaginationSortAndOptionalStateFilter() {
		Usuario solicitante = usuarios.guardar(usuario("paginado"));
		for (int index = 0; index < 23; index++) {
			Ticket ticket = ticket(
					solicitante,
					null,
					Categoria.RED,
					Instant.parse("2026-03-01T10:00:00Z").plusSeconds(index));
			if (index % 2 == 0) {
				ticket.cambiarEstado(EstadoTicket.CANCELADO, "Cancelado para prueba", "integration");
			}
			tickets.guardar(ticket);
		}

		var firstPage = tickets.buscarPorSolicitante(
				solicitante.getId(),
				new PaginaSolicitud(0, 10, "fechaCreacion", true),
				FiltroTickets.todos());
		var filtered = tickets.buscarPorSolicitante(
				solicitante.getId(),
				new PaginaSolicitud(0, 100, "estado", false),
				new FiltroTickets(EstadoTicket.CANCELADO, null, null));

		assertEquals(10, firstPage.contenido().size());
		assertEquals(23, firstPage.totalElementos());
		assertEquals(3, firstPage.totalPaginas());
		assertTrue(firstPage.primera());
		assertEquals(Instant.parse("2026-03-01T10:00:22Z"), firstPage.contenido().getFirst().getFechaCreacion());
		assertEquals(12, filtered.totalElementos());
		assertTrue(filtered.contenido().stream().allMatch(t -> t.getEstado() == EstadoTicket.CANCELADO));
		assertFalse(filtered.contenido().isEmpty());
	}

	@Test
	void loadsTwentyTicketsWithoutAnNPlusOneQueryPattern() {
		Usuario solicitante = usuarios.guardar(usuario("n-plus-one"));
		List<Tecnico> candidates = tecnicos.buscarCandidatos(Categoria.SOFTWARE, null);
		assertTrue(candidates.size() >= 20);
		for (int index = 0; index < 20; index++) {
			tickets.guardar(ticket(solicitante, candidates.get(index), Categoria.SOFTWARE));
		}
		SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
		sessionFactory.getStatistics().setStatisticsEnabled(true);
		sessionFactory.getStatistics().clear();

		var page = tickets.buscarPorSolicitante(
				solicitante.getId(), new PaginaSolicitud(0, 20), FiltroTickets.todos());

		assertEquals(20, page.contenido().size());
		assertTrue(sessionFactory.getStatistics().getPrepareStatementCount() <= 6,
				"Expected a bounded number of SQL statements, got "
						+ sessionFactory.getStatistics().getPrepareStatementCount());
		sessionFactory.getStatistics().setStatisticsEnabled(false);
	}

	@Test
	void schedulerBatchLoadsAndPreservesHistoryOnUpdate() {
		Usuario solicitante = usuarios.guardar(usuario("scheduler"));
		Ticket original = tickets.guardar(ticket(solicitante, null, Categoria.RED));

		Ticket nearDeadline = tickets.buscarActivosProximosAVencer(Instant.now().plusSeconds(86_400)).stream()
				.filter(ticket -> ticket.getId().equals(original.getId()))
				.findFirst()
				.orElseThrow();
		assertEquals(1, nearDeadline.getHistorial().size());
		nearDeadline.escalar("SLA próximo a vencer");
		tickets.guardar(nearDeadline);

		assertEquals(2, tickets.buscarPorId(original.getId()).orElseThrow().getHistorial().size());
	}

	@Test
	void transactionRollsBackTicketAssignmentAndHistoryTogether() {
		Usuario solicitante = usuarios.guardar(usuario("rollback"));
		Tecnico tecnico = tecnicos.buscarPorCodigo("TEC-014").orElseThrow();
		long beforeTickets = jdbc.queryForObject("select count(*) from tickets", Long.class);
		long beforeAssignments = jdbc.queryForObject("select count(*) from asignaciones", Long.class);
		long beforeHistory = jdbc.queryForObject("select count(*) from historial_ticket", Long.class);
		TransactionTemplate transaction = new TransactionTemplate(transactionManager);

		assertThrows(IllegalStateException.class, () -> transaction.executeWithoutResult(status -> {
			Ticket ticket = ticket(solicitante, null, tecnico.getEspecialidades().iterator().next());
			ticket.asignar(tecnico, "Asignación para rollback");
			Ticket saved = tickets.guardar(ticket);
			asignaciones.guardar(new Asignacion(
					null, saved.getId(), tecnico.getId(), Instant.now(), 3, Disponibilidad.MEDIA));
			throw new IllegalStateException("Forzar rollback");
		}));

		assertEquals(beforeTickets, jdbc.queryForObject("select count(*) from tickets", Long.class));
		assertEquals(beforeAssignments, jdbc.queryForObject("select count(*) from asignaciones", Long.class));
		assertEquals(beforeHistory, jdbc.queryForObject("select count(*) from historial_ticket", Long.class));
	}

	private Usuario usuario(String alias) {
		String suffix = alias + "-" + UUID.randomUUID();
		return usuarios.guardar(new Usuario(
				null,
				"DOC-" + suffix,
				"Usuario " + suffix,
				suffix + "@helpflow.test",
				"CENTRAL",
				"TI",
				EstadoUsuario.ACTIVO,
				Rol.ROLE_USUARIO,
				"test-hash"));
	}

	private Ticket ticket(Usuario solicitante, Tecnico tecnico, Categoria categoria) {
		return ticket(solicitante, tecnico, categoria, Instant.now());
	}

	private Ticket ticket(Usuario solicitante, Tecnico tecnico, Categoria categoria, Instant creada) {
		return new Ticket(
				null,
				solicitante,
				"Ticket integración",
				"Descripción de integración",
				categoria,
				Prioridad.MEDIA,
				"CENTRAL",
				null,
				creada,
				creada.plusSeconds(86_400),
				tecnico == null ? EstadoTicket.ABIERTO : EstadoTicket.ASIGNADO,
				tecnico,
				List.of(new com.helpflow.supportticket.domain.model.HistorialTicket(
						null, null, tecnico == null ? EstadoTicket.ABIERTO : EstadoTicket.ASIGNADO,
						"Creado en prueba", "integration", creada)));
	}

}
