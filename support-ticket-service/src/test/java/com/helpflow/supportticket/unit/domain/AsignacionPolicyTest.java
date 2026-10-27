package com.helpflow.supportticket.unit.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import com.helpflow.supportticket.domain.exception.TecnicoNoDisponibleException;
import com.helpflow.supportticket.domain.model.Categoria;
import com.helpflow.supportticket.domain.model.EstadoTicket;
import com.helpflow.supportticket.domain.model.EstadoUsuario;
import com.helpflow.supportticket.domain.model.Prioridad;
import com.helpflow.supportticket.domain.model.Tecnico;
import com.helpflow.supportticket.domain.service.AsignacionPolicy;
import org.junit.jupiter.api.Test;

class AsignacionPolicyTest {

	private static final Duration UMBRAL_SLA = Duration.ofHours(2);
	private final AsignacionPolicy policy = new AsignacionPolicy();

	@Test
	void rejectsInactiveTechnicianAndChoosesEligibleAlternative() {
		Tecnico inactivo = DomainTestFixtures.tecnico(
				2L, "TEC-002", "CENTRAL", Set.of(Categoria.RED), 4, 0, EstadoUsuario.INACTIVO);
		Tecnico alternativa = DomainTestFixtures.tecnicoDisponible();

		assertEquals(alternativa, policy.seleccionarTecnico(
				ticket(), List.of(inactivo, alternativa), inactivo));
	}

	@Test
	void rejectsTechnicianWithoutMatchingSpecialty() {
		Tecnico sinEspecialidad = DomainTestFixtures.tecnico(
				3L, "TEC-003", "CENTRAL", Set.of(Categoria.HARDWARE), 4, 0, EstadoUsuario.ACTIVO);

		assertThrows(TecnicoNoDisponibleException.class, () -> policy.seleccionarTecnico(
				ticket(), List.of(sinEspecialidad), sinEspecialidad));
	}

	@Test
	void rejectsTechnicianWithoutCapacity() {
		Tecnico sinCapacidad = DomainTestFixtures.tecnico(
				4L, "TEC-004", "CENTRAL", Set.of(Categoria.RED), 2, 2, EstadoUsuario.ACTIVO);

		assertThrows(TecnicoNoDisponibleException.class, () -> policy.seleccionarTecnico(
				ticket(), List.of(sinCapacidad), sinCapacidad));
	}

	@Test
	void prefersTechnicianFromSameSiteWhenSuggestionIsNotEligible() {
		Tecnico otraSede = DomainTestFixtures.tecnico(
				5L, "TEC-005", "NORTE", Set.of(Categoria.RED), 5, 0, EstadoUsuario.ACTIVO);
		Tecnico mismaSede = DomainTestFixtures.tecnico(
				6L, "TEC-006", "CENTRAL", Set.of(Categoria.RED), 5, 3, EstadoUsuario.ACTIVO);

		assertEquals(mismaSede, policy.seleccionarTecnico(
				ticket(), List.of(otraSede, mismaSede), null));
	}

	@Test
	void returnsEligibleDispatchSuggestion() {
		Tecnico sugerido = DomainTestFixtures.tecnicoDisponible();
		Tecnico conMenorCarga = DomainTestFixtures.tecnico(
				7L, "TEC-007", "CENTRAL", Set.of(Categoria.RED), 5, 0, EstadoUsuario.ACTIVO);

		assertEquals(sugerido, policy.seleccionarTecnico(
				ticket(), List.of(conMenorCarga), sugerido));
	}

	@Test
	void throwsWhenNoTechnicianIsAvailable() {
		assertThrows(TecnicoNoDisponibleException.class, () -> policy.seleccionarTecnico(
				ticket(), List.of(), null));
	}

	@Test
	void prioritizesCriticalAndNearDeadlineTickets() {
		var normal = DomainTestFixtures.ticket(
				Prioridad.BAJA, EstadoTicket.ABIERTO, DomainTestFixtures.AHORA.plus(Duration.ofHours(8)));
		var nearDeadline = DomainTestFixtures.ticket(
				Prioridad.ALTA, EstadoTicket.ABIERTO, DomainTestFixtures.AHORA.plus(Duration.ofHours(1)));
		var critical = DomainTestFixtures.ticket(
				Prioridad.CRITICA, EstadoTicket.ABIERTO, DomainTestFixtures.AHORA.plus(Duration.ofHours(12)));

		assertEquals(List.of(critical, nearDeadline, normal),
				policy.priorizarTickets(
						List.of(normal, nearDeadline, critical),
						DomainTestFixtures.AHORA,
						UMBRAL_SLA));
	}

	private com.helpflow.supportticket.domain.model.Ticket ticket() {
		return DomainTestFixtures.ticket(
				Prioridad.MEDIA,
				EstadoTicket.ABIERTO,
				DomainTestFixtures.AHORA.plus(Duration.ofHours(8)));
	}
}
