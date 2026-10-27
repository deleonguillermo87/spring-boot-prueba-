package com.helpflow.supportticket.application.port.out;

import java.time.Duration;

import com.helpflow.supportticket.domain.model.Categoria;
import com.helpflow.supportticket.domain.model.Prioridad;

public interface MetricasPort {

	void ticketCreado(Categoria categoria, Prioridad prioridad);

	void ticketVencido();

	void tiempoPrimeraRespuesta(Duration duracion);

	void tiempoResolucion(Duration duracion);

	void falloAutenticacion();

	void accesoDenegado();
}
