package com.helpflow.supportticket.infrastructure.adapter.out.persistence.repository;

import java.time.Instant;
import java.util.List;

import com.helpflow.supportticket.domain.model.Categoria;
import com.helpflow.supportticket.domain.model.EstadoTicket;
import com.helpflow.supportticket.domain.model.Prioridad;
import com.helpflow.supportticket.infrastructure.adapter.out.persistence.entity.TicketEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketJpaRepository extends JpaRepository<TicketEntity, Long> {

	@EntityGraph(attributePaths = {"solicitante", "tecnicoAsignado", "tecnicoAsignado.usuario", "historial"})
	@Query("select t from TicketEntity t where t.id = :id")
	java.util.Optional<TicketEntity> findDetailedById(@Param("id") Long id);

	@EntityGraph(attributePaths = {"solicitante", "tecnicoAsignado", "tecnicoAsignado.usuario"})
	@Query("""
			select t from TicketEntity t
			where t.solicitante.id = :actorId
				and (:estado is null or t.estado = :estado)
				and (:categoria is null or t.categoria = :categoria)
				and (:prioridad is null or t.prioridad = :prioridad)
			""")
	Page<TicketEntity> pageBySolicitante(
			@Param("actorId") Long actorId,
			@Param("estado") EstadoTicket estado,
			@Param("categoria") Categoria categoria,
			@Param("prioridad") Prioridad prioridad,
			Pageable pageable);

	@EntityGraph(attributePaths = {"solicitante", "tecnicoAsignado", "tecnicoAsignado.usuario"})
	@Query("""
			select t from TicketEntity t
			where t.tecnicoAsignado.usuario.id = :actorId
				and (:estado is null or t.estado = :estado)
				and (:categoria is null or t.categoria = :categoria)
				and (:prioridad is null or t.prioridad = :prioridad)
			""")
	Page<TicketEntity> pageByTecnico(
			@Param("actorId") Long actorId,
			@Param("estado") EstadoTicket estado,
			@Param("categoria") Categoria categoria,
			@Param("prioridad") Prioridad prioridad,
			Pageable pageable);

	@EntityGraph(attributePaths = {"solicitante", "tecnicoAsignado", "tecnicoAsignado.usuario"})
	@Query("""
			select t from TicketEntity t
			where (:estado is null or t.estado = :estado)
				and (:categoria is null or t.categoria = :categoria)
				and (:prioridad is null or t.prioridad = :prioridad)
			""")
	Page<TicketEntity> pageAll(
			@Param("estado") EstadoTicket estado,
			@Param("categoria") Categoria categoria,
			@Param("prioridad") Prioridad prioridad,
			Pageable pageable);

	@EntityGraph(attributePaths = {"solicitante", "tecnicoAsignado", "tecnicoAsignado.usuario", "historial"})
	@Query("""
			select distinct t from TicketEntity t
			where t.estado in (
				com.helpflow.supportticket.domain.model.EstadoTicket.ABIERTO,
				com.helpflow.supportticket.domain.model.EstadoTicket.ASIGNADO,
				com.helpflow.supportticket.domain.model.EstadoTicket.EN_PROGRESO
			)
			and t.fechaLimiteSla <= :limite
			""")
	List<TicketEntity> findActivosProximosAVencer(@Param("limite") Instant limite);
}
