package com.helpflow.supportticket.application.dto;

import com.helpflow.supportticket.domain.model.Categoria;
import com.helpflow.supportticket.domain.model.Prioridad;

public record CrearTicketCommand(
		Long solicitanteId,
		String titulo,
		String descripcion,
		Categoria categoria,
		Prioridad prioridad,
		String sede,
		String activoAfectado,
		Actor actor) {
}
