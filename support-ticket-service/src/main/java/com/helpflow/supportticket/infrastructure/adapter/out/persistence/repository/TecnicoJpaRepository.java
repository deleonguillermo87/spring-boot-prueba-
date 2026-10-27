package com.helpflow.supportticket.infrastructure.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;

import com.helpflow.supportticket.domain.model.Categoria;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.entity.TecnicoEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TecnicoJpaRepository extends JpaRepository<TecnicoEntity, Long> {

	@EntityGraph(attributePaths = {"usuario", "especialidades"})
	Optional<TecnicoEntity> findByCodigo(String codigo);

	@EntityGraph(attributePaths = {"usuario", "especialidades"})
	@Query("""
			select distinct t from TecnicoEntity t
			join t.especialidades especialidad
			where especialidad = :categoria
			""")
	List<TecnicoEntity> findCandidatos(@Param("categoria") Categoria categoria);

	@Query("""
			select t.id as tecnicoId, count(ticket.id) as cantidad
			from TecnicoEntity t
			left join TicketEntity ticket on ticket.tecnicoAsignado = t
				and ticket.estado in (
					com.helpflow.supportticket.domain.model.EstadoTicket.ASIGNADO,
					com.helpflow.supportticket.domain.model.EstadoTicket.EN_PROGRESO
				)
			group by t.id
			""")
	List<TecnicoTicketCount> contarTicketsActivos();

	interface TecnicoTicketCount {
		Long getTecnicoId();
		Long getCantidad();
	}
}
