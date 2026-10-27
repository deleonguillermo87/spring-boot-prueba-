package com.helpflow.supportticket.application.port.in;

import com.helpflow.supportticket.application.dto.GestionarTicketCommand;
import com.helpflow.supportticket.domain.model.Ticket;

public interface GestionarTicketUseCase {

	Ticket ejecutar(GestionarTicketCommand command);
}
