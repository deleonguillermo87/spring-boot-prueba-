package com.helpflow.supportticket.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;

import com.helpflow.supportticket.domain.model.EstadoTicket;
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
@Table(name = "historial_ticket")
public class HistorialTicketEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ticket_id", nullable = false)
	private TicketEntity ticket;
	@Enumerated(EnumType.STRING)
	@Column(name = "estado_anterior", length = 30)
	private EstadoTicket estadoAnterior;
	@Enumerated(EnumType.STRING)
	@Column(name = "estado_nuevo", nullable = false, length = 30)
	private EstadoTicket estadoNuevo;
	@Column(nullable = false, columnDefinition = "text")
	private String observacion;
	@Column(nullable = false, length = 160)
	private String autor;
	@Column(nullable = false)
	private Instant fecha;

	public HistorialTicketEntity() {
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public TicketEntity getTicket() { return ticket; }
	public void setTicket(TicketEntity ticket) { this.ticket = ticket; }
	public EstadoTicket getEstadoAnterior() { return estadoAnterior; }
	public void setEstadoAnterior(EstadoTicket estadoAnterior) { this.estadoAnterior = estadoAnterior; }
	public EstadoTicket getEstadoNuevo() { return estadoNuevo; }
	public void setEstadoNuevo(EstadoTicket estadoNuevo) { this.estadoNuevo = estadoNuevo; }
	public String getObservacion() { return observacion; }
	public void setObservacion(String observacion) { this.observacion = observacion; }
	public String getAutor() { return autor; }
	public void setAutor(String autor) { this.autor = autor; }
	public Instant getFecha() { return fecha; }
	public void setFecha(Instant fecha) { this.fecha = fecha; }
}
