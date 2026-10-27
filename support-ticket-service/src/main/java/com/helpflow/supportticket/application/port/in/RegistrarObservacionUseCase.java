package com.helpflow.supportticket.application.port.in;

import com.helpflow.supportticket.application.dto.RegistrarObservacionCommand;
import com.helpflow.supportticket.domain.model.Ticket;

public interface RegistrarObservacionUseCase {

	Ticket ejecutar(RegistrarObservacionCommand command);
}

