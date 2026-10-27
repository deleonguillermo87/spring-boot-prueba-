package com.helpflow.supportticket.domain.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.helpflow.supportticket.domain.exception.TecnicoNoDisponibleException;
import com.helpflow.supportticket.domain.model.EstadoTicket;
import com.helpflow.supportticket.domain.model.Prioridad;
import com.helpflow.supportticket.domain.model.Tecnico;
import com.helpflow.supportticket.domain.model.Ticket;

public class AsignacionPolicy {

	public Tecnico seleccionarTecnico(
			Ticket ticket,
			List<Tecnico> candidatos,
			Tecnico tecnicoSugerido) {
		Objects.requireNonNull(ticket, "ticket es obligatorio");
		Objects.requireNonNull(candidatos, "candidatos son obligatorios");

		List<Tecnico> opciones = new ArrayList<>(candidatos);
		if (tecnicoSugerido != null) {
			opciones.add(tecnicoSugerido);
		}

		List<Tecnico> elegibles = opciones.stream()
				.filter(Objects::nonNull)
				.filter(Tecnico::estaActivo)
				.filter(tecnico -> tecnico.puedeAtender(ticket.getCategoria()))
				.filter(Tecnico::tieneCapacidad)
				.distinct()
				.toList();

		if (tecnicoSugerido != null) {
			Tecnico sugeridoElegible = elegibles.stream()
					.filter(tecnico -> tecnico.getCodigo().equals(tecnicoSugerido.getCodigo()))
					.findFirst()
					.orElse(null);
			if (sugeridoElegible != null) {
				return sugeridoElegible;
			}
		}

		return elegibles.stream()
				.sorted(ordenarCandidatos(ticket))
				.findFirst()
				.orElseThrow(() -> new TecnicoNoDisponibleException(
						"No hay técnicos activos, compatibles y con capacidad para el ticket " + ticket.getId() + "."));
	}

	public List<Ticket> priorizarTickets(List<Ticket> tickets, Instant ahora, Duration umbralSla) {
		Objects.requireNonNull(tickets, "tickets son obligatorios");
		Objects.requireNonNull(ahora, "ahora es obligatorio");
		Objects.requireNonNull(umbralSla, "umbralSla es obligatorio");

		return tickets.stream()
				.filter(Objects::nonNull)
				.sorted(Comparator
						.comparing((Ticket ticket) -> !esUrgente(ticket, ahora, umbralSla))
						.thenComparing((Ticket ticket) -> ticket.getPrioridad() != Prioridad.CRITICA)
						.thenComparing(Ticket::getFechaLimiteSla)
						.thenComparing(Ticket::getFechaCreacion))
				.toList();
	}

	private Comparator<Tecnico> ordenarCandidatos(Ticket ticket) {
		return Comparator
				.comparing((Tecnico tecnico) -> !tecnico.getSede().equalsIgnoreCase(ticket.getSede()))
				.thenComparingDouble(tecnico -> (double) tecnico.getTicketsActivos() / tecnico.getCargaMaxima())
				.thenComparingInt(Tecnico::getTicketsActivos)
				.thenComparing(Tecnico::getCodigo);
	}

	private boolean esUrgente(Ticket ticket, Instant ahora, Duration umbralSla) {
		return ticket.getPrioridad() == Prioridad.CRITICA
				|| ticket.estaVencido(ahora)
				|| ticket.estaProximoAVencer(ahora, umbralSla);
	}
}
