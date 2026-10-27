package com.helpflow.supportticket.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.helpflow.supportticket.domain.model.Categoria;
import com.helpflow.supportticket.domain.model.EstadoTicket;
import com.helpflow.supportticket.domain.model.Prioridad;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "tickets")
public class TicketEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "solicitante_id", nullable = false)
	private UsuarioEntity solicitante;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tecnico_id")
	private TecnicoEntity tecnicoAsignado;
	@Column(nullable = false, length = 200)
	private String titulo;
	@Column(nullable = false, columnDefinition = "text")
	private String descripcion;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private Categoria categoria;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Prioridad prioridad;
	@Column(nullable = false, length = 120)
	private String sede;
	@Column(name = "activo_afectado", length = 200)
	private String activoAfectado;
	@Column(name = "fecha_creacion", nullable = false)
	private Instant fechaCreacion;
	@Column(name = "fecha_limite_sla", nullable = false)
	private Instant fechaLimiteSla;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private EstadoTicket estado;
	@Version
	@Column(nullable = false)
	private long version;
	@OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@OrderBy("id ASC")
	@BatchSize(size = 50)
	private List<HistorialTicketEntity> historial = new ArrayList<>();

	public TicketEntity() {
	}

	public void addHistorial(HistorialTicketEntity item) {
		historial.add(item);
		item.setTicket(this);
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public UsuarioEntity getSolicitante() { return solicitante; }
	public void setSolicitante(UsuarioEntity solicitante) { this.solicitante = solicitante; }
	public TecnicoEntity getTecnicoAsignado() { return tecnicoAsignado; }
	public void setTecnicoAsignado(TecnicoEntity tecnicoAsignado) { this.tecnicoAsignado = tecnicoAsignado; }
	public String getTitulo() { return titulo; }
	public void setTitulo(String titulo) { this.titulo = titulo; }
	public String getDescripcion() { return descripcion; }
	public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
	public Categoria getCategoria() { return categoria; }
	public void setCategoria(Categoria categoria) { this.categoria = categoria; }
	public Prioridad getPrioridad() { return prioridad; }
	public void setPrioridad(Prioridad prioridad) { this.prioridad = prioridad; }
	public String getSede() { return sede; }
	public void setSede(String sede) { this.sede = sede; }
	public String getActivoAfectado() { return activoAfectado; }
	public void setActivoAfectado(String activoAfectado) { this.activoAfectado = activoAfectado; }
	public Instant getFechaCreacion() { return fechaCreacion; }
	public void setFechaCreacion(Instant fechaCreacion) { this.fechaCreacion = fechaCreacion; }
	public Instant getFechaLimiteSla() { return fechaLimiteSla; }
	public void setFechaLimiteSla(Instant fechaLimiteSla) { this.fechaLimiteSla = fechaLimiteSla; }
	public EstadoTicket getEstado() { return estado; }
	public void setEstado(EstadoTicket estado) { this.estado = estado; }
	public long getVersion() { return version; }
	public void setVersion(long version) { this.version = version; }
	public List<HistorialTicketEntity> getHistorial() { return historial; }
	public void setHistorial(List<HistorialTicketEntity> historial) { this.historial = historial; }
}
