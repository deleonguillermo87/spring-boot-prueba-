package com.helpflow.supportticket.application.dto;

import java.time.Instant;

import com.helpflow.supportticket.domain.model.Categoria;
import com.helpflow.supportticket.domain.model.EstadoTicket;
import com.helpflow.supportticket.domain.model.Prioridad;
import com.helpflow.supportticket.domain.model.Ticket;

public record TicketResult(
		Long id,
		Long solicitanteId,
		String titulo,
		String descripcion,
		Categoria categoria,
		Prioridad prioridad,
		String sede,
		String activoAfectado,
		Instant fechaCreacion,
		Instant fechaLimiteSla,
		EstadoTicket estado,
		Long tecnicoAsignadoId,
		String tecnicoAsignadoCodigo) {

	public static TicketResult desde(Ticket ticket) {
		return new TicketResult(
				ticket.getId(),
				ticket.getSolicitante().getId(),
				ticket.getTitulo(),
				ticket.getDescripcion(),
				ticket.getCategoria(),
				ticket.getPrioridad(),
				ticket.getSede(),
				ticket.getActivoAfectado(),
				ticket.getFechaCreacion(),
				ticket.getFechaLimiteSla(),
				ticket.getEstado(),
				ticket.getTecnicoAsignado() == null ? null : ticket.getTecnicoAsignado().getId(),
				ticket.getTecnicoAsignado() == null ? null : ticket.getTecnicoAsignado().getCodigo());
	}
}
