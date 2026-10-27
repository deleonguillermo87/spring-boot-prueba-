package com.helpflow.supportticket.application.port.in;

import com.helpflow.supportticket.application.dto.AsignarTicketCommand;
import com.helpflow.supportticket.domain.model.Ticket;

public interface AsignarTicketUseCase {

	Ticket ejecutar(AsignarTicketCommand command);
}
