package com.helpflow.supportticket.application.port.out;

import com.helpflow.supportticket.domain.model.Categoria;
import com.helpflow.supportticket.domain.model.Prioridad;

public interface TechnicianDispatchPort {

	DespachoSugerido despachar(Long ticketId, Categoria categoria, Prioridad prioridad)
			throws DispatchUnavailableException;
}
