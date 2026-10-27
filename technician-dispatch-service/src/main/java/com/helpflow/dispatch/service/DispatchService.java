package com.helpflow.dispatch.service;

import com.helpflow.dispatch.dto.Disponibilidad;
import com.helpflow.dispatch.dto.DispatchRequest;
import com.helpflow.dispatch.dto.DispatchResponse;
import org.springframework.stereotype.Service;

@Service
public class DispatchService {

	public DispatchResponse dispatch(DispatchRequest request) {
		String seedSource = request.ticketId()
				+ "|" + request.categoria().name()
				+ "|" + request.prioridad().name();
		int seed = seedSource.hashCode();

		int technicianNumber = Math.floorMod(seed, 50) + 1;
		int estimatedHours = Math.floorMod(seed, 13);
		String technicianId = "TEC-%03d".formatted(technicianNumber);

		return new DispatchResponse(
				request.ticketId(),
				technicianId,
				disponibilidadPara(estimatedHours),
				estimatedHours);
	}

	static Disponibilidad disponibilidadPara(int tiempoEstimadoHoras) {
		if (tiempoEstimadoHoras <= 2) {
			return Disponibilidad.ALTA;
		}
		if (tiempoEstimadoHoras <= 8) {
			return Disponibilidad.MEDIA;
		}
		return Disponibilidad.BAJA;
	}
}
