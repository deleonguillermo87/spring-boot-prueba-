package com.helpflow.supportticket.infrastructure.adapter.in.rest.mapper;

import java.util.List;

import com.helpflow.supportticket.application.dto.TicketResult;
import com.helpflow.supportticket.domain.model.HistorialTicket;
import com.helpflow.supportticket.domain.model.Ticket;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.dto.HistorialResponse;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.dto.TicketResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketRestMapper {

	@Mapping(target = "solicitanteId", source = "solicitante.id")
	@Mapping(target = "tecnicoAsignadoId", source = "tecnicoAsignado.id")
	@Mapping(target = "tecnicoAsignadoCodigo", source = "tecnicoAsignado.codigo")
	@Mapping(target = "categoria", expression = "java(ticket.getCategoria() != null ? ticket.getCategoria().name() : null)")
	@Mapping(target = "prioridad", expression = "java(ticket.getPrioridad() != null ? ticket.getPrioridad().name() : null)")
	@Mapping(target = "estado", expression = "java(ticket.getEstado() != null ? ticket.getEstado().name() : null)")
	@Mapping(target = "historial", source = "historial")
	TicketResponse toResponse(Ticket ticket);

	@Mapping(target = "categoria", expression = "java(ticketResult.categoria() != null ? ticketResult.categoria().name() : null)")
	@Mapping(target = "prioridad", expression = "java(ticketResult.prioridad() != null ? ticketResult.prioridad().name() : null)")
	@Mapping(target = "estado", expression = "java(ticketResult.estado() != null ? ticketResult.estado().name() : null)")
	@Mapping(target = "historial", ignore = true)
	TicketResponse toResponse(TicketResult ticketResult);

	@Mapping(target = "estadoAnterior", expression = "java(historial.estadoAnterior() != null ? historial.estadoAnterior().name() : null)")
	@Mapping(target = "estadoNuevo", expression = "java(historial.estadoNuevo() != null ? historial.estadoNuevo().name() : null)")
	HistorialResponse toResponse(HistorialTicket historial);

	List<HistorialResponse> toHistorialList(List<HistorialTicket> historial);
}

