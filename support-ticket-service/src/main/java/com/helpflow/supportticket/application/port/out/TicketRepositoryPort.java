package com.helpflow.supportticket.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.helpflow.supportticket.application.dto.FiltroTickets;
import com.helpflow.supportticket.application.dto.PaginaResultado;
import com.helpflow.supportticket.application.dto.PaginaSolicitud;
import com.helpflow.supportticket.domain.model.Ticket;

public interface TicketRepositoryPort {

	Ticket guardar(Ticket ticket);

	Optional<Ticket> buscarPorId(Long id);

	PaginaResultado<Ticket> buscarPorSolicitante(
			Long solicitanteId,
			PaginaSolicitud pagina,
			FiltroTickets filtros);

	PaginaResultado<Ticket> buscarPorTecnico(
			Long tecnicoId,
			PaginaSolicitud pagina,
			FiltroTickets filtros);

	PaginaResultado<Ticket> buscarTodos(PaginaSolicitud pagina, FiltroTickets filtros);

	List<Ticket> buscarActivosProximosAVencer(Instant limite);
}
