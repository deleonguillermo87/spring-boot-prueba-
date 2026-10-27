package com.helpflow.supportticket.application.dto;

import com.helpflow.supportticket.domain.model.EstadoTicket;

public record GestionarTicketCommand(
		Long ticketId,
		EstadoTicket nuevoEstado,
		String observacion,
		Actor actor) {
}
