package com.helpflow.supportticket.application.dto;

public record AsignarTicketCommand(
		Long ticketId,
		String tecnicoCodigo,
		String observacion,
		Actor actor) {
}
