package com.helpflow.supportticket.application.port.in;

import com.helpflow.supportticket.application.dto.EscalarTicketsCommand;

public interface EscalarTicketsUseCase {

	int ejecutar(EscalarTicketsCommand command);
}
