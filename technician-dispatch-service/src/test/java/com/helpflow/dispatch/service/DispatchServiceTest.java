package com.helpflow.dispatch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.helpflow.dispatch.dto.Categoria;
import com.helpflow.dispatch.dto.Disponibilidad;
import com.helpflow.dispatch.dto.DispatchRequest;
import com.helpflow.dispatch.dto.DispatchResponse;
import com.helpflow.dispatch.dto.Prioridad;
import org.junit.jupiter.api.Test;

class DispatchServiceTest {

	private final DispatchService dispatchService = new DispatchService();

	@Test
	void sameInputProducesSameResultEveryTime() {
		DispatchRequest request = new DispatchRequest(125L, Categoria.RED, Prioridad.ALTA);

		DispatchResponse first = dispatchService.dispatch(request);

		assertEquals(first, dispatchService.dispatch(request));
		assertEquals(first, dispatchService.dispatch(request));
	}

	@Test
	void differentInputsCanProduceDifferentResults() {
		List<DispatchResponse> responses = List.of(
				dispatchService.dispatch(new DispatchRequest(125L, Categoria.RED, Prioridad.ALTA)),
				dispatchService.dispatch(new DispatchRequest(126L, Categoria.RED, Prioridad.ALTA)),
				dispatchService.dispatch(new DispatchRequest(125L, Categoria.SOFTWARE, Prioridad.BAJA)));

		assertTrue(responses.stream().distinct().count() > 1);
	}

	@Test
	void availabilityMatchesHourBoundaries() {
		assertEquals(Disponibilidad.ALTA, DispatchService.disponibilidadPara(2));
		assertEquals(Disponibilidad.MEDIA, DispatchService.disponibilidadPara(3));
		assertEquals(Disponibilidad.MEDIA, DispatchService.disponibilidadPara(8));
		assertEquals(Disponibilidad.BAJA, DispatchService.disponibilidadPara(9));
	}
}
