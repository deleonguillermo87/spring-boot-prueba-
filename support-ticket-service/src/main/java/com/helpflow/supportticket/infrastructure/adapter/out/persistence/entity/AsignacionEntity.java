package com.helpflow.supportticket.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;

import com.helpflow.supportticket.domain.model.Disponibilidad;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "asignaciones")
public class AsignacionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ticket_id", nullable = false)
	private TicketEntity ticket;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tecnico_id", nullable = false)
	private TecnicoEntity tecnico;
	@Column(nullable = false)
	private Instant fecha;
	@Column(name = "tiempo_estimado_horas", nullable = false)
	private int tiempoEstimadoHoras;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Disponibilidad disponibilidad;

	public AsignacionEntity() {
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public TicketEntity getTicket() { return ticket; }
	public void setTicket(TicketEntity ticket) { this.ticket = ticket; }
	public TecnicoEntity getTecnico() { return tecnico; }
	public void setTecnico(TecnicoEntity tecnico) { this.tecnico = tecnico; }
	public Instant getFecha() { return fecha; }
	public void setFecha(Instant fecha) { this.fecha = fecha; }
	public int getTiempoEstimadoHoras() { return tiempoEstimadoHoras; }
	public void setTiempoEstimadoHoras(int tiempoEstimadoHoras) { this.tiempoEstimadoHoras = tiempoEstimadoHoras; }
	public Disponibilidad getDisponibilidad() { return disponibilidad; }
	public void setDisponibilidad(Disponibilidad disponibilidad) { this.disponibilidad = disponibilidad; }
}
