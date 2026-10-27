package com.helpflow.supportticket.application.dto;

import java.util.Objects;

public record RegistrarObservacionCommand(
		Long ticketId,
		String observacion,
		Actor actor) {

	public RegistrarObservacionCommand {
		Objects.requireNonNull(ticketId, "ticketId es obligatorio");
		Objects.requireNonNull(observacion, "observacion es obligatoria");
		Objects.requireNonNull(actor, "actor es obligatorio");
	}
}

