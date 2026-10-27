package com.helpflow.supportticket.application.port.out;

import com.helpflow.supportticket.domain.model.Asignacion;

public interface AsignacionRepositoryPort {

	Asignacion guardar(Asignacion asignacion);
}
