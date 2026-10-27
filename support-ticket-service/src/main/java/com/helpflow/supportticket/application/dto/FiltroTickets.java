package com.helpflow.supportticket.application.dto;

import com.helpflow.supportticket.domain.model.Categoria;
import com.helpflow.supportticket.domain.model.EstadoTicket;
import com.helpflow.supportticket.domain.model.Prioridad;

public record FiltroTickets(EstadoTicket estado, Categoria categoria, Prioridad prioridad) {

	public static FiltroTickets todos() {
		return new FiltroTickets(null, null, null);
	}
}
