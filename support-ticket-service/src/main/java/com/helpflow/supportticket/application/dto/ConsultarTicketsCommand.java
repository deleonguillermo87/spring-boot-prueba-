package com.helpflow.supportticket.application.dto;

public record ConsultarTicketsCommand(
		Long ticketId,
		Actor actor,
		PaginaSolicitud pagina,
		FiltroTickets filtros) {

	public ConsultarTicketsCommand {
		pagina = pagina == null ? new PaginaSolicitud() : pagina;
		filtros = filtros == null ? FiltroTickets.todos() : filtros;
	}

	public ConsultarTicketsCommand(Long ticketId, Actor actor) {
		this(ticketId, actor, new PaginaSolicitud(), FiltroTickets.todos());
	}

	public ConsultarTicketsCommand(Actor actor, PaginaSolicitud pagina, FiltroTickets filtros) {
		this(null, actor, pagina, filtros);
	}
}
