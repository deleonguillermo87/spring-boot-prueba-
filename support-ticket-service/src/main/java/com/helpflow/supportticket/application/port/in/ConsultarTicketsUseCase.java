package com.helpflow.supportticket.application.port.in;

import com.helpflow.supportticket.application.dto.ConsultarTicketsCommand;
import com.helpflow.supportticket.application.dto.PaginaResultado;
import com.helpflow.supportticket.application.dto.TicketResult;

public interface ConsultarTicketsUseCase {

	PaginaResultado<TicketResult> ejecutar(ConsultarTicketsCommand command);
}
