package com.helpflow.supportticket.infrastructure.adapter.out.client;

import java.util.Objects;

import com.helpflow.supportticket.application.port.out.DespachoSugerido;
import com.helpflow.supportticket.application.port.out.DispatchUnavailableException;
import com.helpflow.supportticket.application.port.out.TechnicianDispatchPort;
import com.helpflow.supportticket.domain.model.Categoria;
import com.helpflow.supportticket.domain.model.Disponibilidad;
import com.helpflow.supportticket.domain.model.Prioridad;
import com.helpflow.supportticket.infrastructure.adapter.out.client.dto.DispatchClientRequest;
import com.helpflow.supportticket.infrastructure.adapter.out.client.dto.DispatchClientResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
@ConditionalOnMissingBean(TechnicianDispatchPort.class)
public class TechnicianDispatchRestAdapter implements TechnicianDispatchPort {

	private final RestClient restClient;

	public TechnicianDispatchRestAdapter(RestClient dispatchRestClient) {
		this.restClient = dispatchRestClient;
	}

	@Override
	public DespachoSugerido despachar(Long ticketId, Categoria categoria, Prioridad prioridad)
			throws DispatchUnavailableException {
		Objects.requireNonNull(ticketId, "ticketId es obligatorio");
		Objects.requireNonNull(categoria, "categoria es obligatoria");
		Objects.requireNonNull(prioridad, "prioridad es obligatoria");

		DispatchClientRequest request = new DispatchClientRequest(
				ticketId,
				categoria.name(),
				prioridad.name());

		try {
			DispatchClientResponse response = restClient.post()
					.uri("/technician-dispatch")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
					.body(request)
					.retrieve()
					.body(DispatchClientResponse.class);

			if (response == null || response.tecnicoId() == null || response.disponibilidad() == null) {
				throw new DispatchUnavailableException("Respuesta vacía o incompleta de technician-dispatch");
			}

			Disponibilidad disponibilidad = Disponibilidad.valueOf(response.disponibilidad().trim().toUpperCase());
			return new DespachoSugerido(response.tecnicoId(), disponibilidad, response.tiempoEstimadoHoras());
		} catch (ResourceAccessException exception) {
			throw new DispatchUnavailableException("Fallo de red o timeout al comunicar con technician-dispatch: " + exception.getMessage(), exception);
		} catch (HttpServerErrorException exception) {
			throw new DispatchUnavailableException("Error 5xx en technician-dispatch: " + exception.getStatusCode(), exception);
		} catch (RestClientResponseException exception) {
			if (exception.getStatusCode().is5xxServerError()) {
				throw new DispatchUnavailableException("Error 5xx en technician-dispatch: " + exception.getStatusCode(), exception);
			}
			throw exception;
		} catch (IllegalArgumentException exception) {
			throw new DispatchUnavailableException("Formato inválido en la respuesta de technician-dispatch: " + exception.getMessage(), exception);
		}
	}
}

