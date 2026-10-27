package com.helpflow.supportticket.application.port.in;

import com.helpflow.supportticket.application.dto.CrearTicketCommand;
import com.helpflow.supportticket.domain.model.Ticket;

public interface CrearTicketUseCase {

	Ticket ejecutar(CrearTicketCommand command);
}
