package com.helpflow.supportticket.domain.model;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.helpflow.supportticket.domain.exception.TecnicoNoDisponibleException;
import com.helpflow.supportticket.domain.exception.TransicionInvalidaException;
import com.helpflow.supportticket.domain.service.TransicionEstadoPolicy;

public class Ticket {

	private final Long id;
	private final Usuario solicitante;
	private final String titulo;
	private final String descripcion;
	private final Categoria categoria;
	private Prioridad prioridad;
	private final String sede;
	private final String activoAfectado;
	private final Instant fechaCreacion;
	private final Instant fechaLimiteSla;
	private final Clock clock;
	private final TransicionEstadoPolicy transicionEstadoPolicy;
	private EstadoTicket estado;
	private Tecnico tecnicoAsignado;
	private final List<HistorialTicket> historial;

	public Ticket(
			Long id,
			Usuario solicitante,
			String titulo,
			String descripcion,
			Categoria categoria,
			Prioridad prioridad,
			String sede,
			String activoAfectado,
			Instant fechaCreacion,
			Instant fechaLimiteSla,
			EstadoTicket estado,
			Tecnico tecnicoAsignado,
			List<HistorialTicket> historial) {
		this(
				id,
				solicitante,
				titulo,
				descripcion,
				categoria,
				prioridad,
				sede,
				activoAfectado,
				fechaCreacion,
				fechaLimiteSla,
				estado,
				tecnicoAsignado,
				historial,
				Clock.systemUTC());
	}

	public Ticket(
			Long id,
			Usuario solicitante,
			String titulo,
			String descripcion,
			Categoria categoria,
			Prioridad prioridad,
			String sede,
			String activoAfectado,
			Instant fechaCreacion,
			Instant fechaLimiteSla,
			EstadoTicket estado,
			Tecnico tecnicoAsignado,
			List<HistorialTicket> historial,
			Clock clock) {
		this.id = id;
		this.solicitante = Objects.requireNonNull(solicitante, "solicitante es obligatorio");
		this.titulo = Objects.requireNonNull(titulo, "titulo es obligatorio");
		this.descripcion = Objects.requireNonNull(descripcion, "descripcion es obligatoria");
		this.categoria = Objects.requireNonNull(categoria, "categoria es obligatoria");
		this.prioridad = Objects.requireNonNull(prioridad, "prioridad es obligatoria");
		this.sede = Objects.requireNonNull(sede, "sede es obligatoria");
		this.activoAfectado = activoAfectado;
		this.fechaCreacion = Objects.requireNonNull(fechaCreacion, "fechaCreacion es obligatoria");
		this.fechaLimiteSla = Objects.requireNonNull(fechaLimiteSla, "fechaLimiteSla es obligatoria");
		this.estado = Objects.requireNonNull(estado, "estado es obligatorio");
		this.tecnicoAsignado = tecnicoAsignado;
		this.historial = new ArrayList<>(Objects.requireNonNull(historial, "historial es obligatorio"));
		this.clock = Objects.requireNonNull(clock, "clock es obligatorio");
		this.transicionEstadoPolicy = new TransicionEstadoPolicy();
	}

	public Long getId() {
		return id;
	}

	public Usuario getSolicitante() {
		return solicitante;
	}

	public String getTitulo() {
		return titulo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public Categoria getCategoria() {
		return categoria;
	}

	public Prioridad getPrioridad() {
		return prioridad;
	}

	public String getSede() {
		return sede;
	}

	public String getActivoAfectado() {
		return activoAfectado;
	}

	public Instant getFechaCreacion() {
		return fechaCreacion;
	}

	public Instant getFechaLimiteSla() {
		return fechaLimiteSla;
	}

	public EstadoTicket getEstado() {
		return estado;
	}

	public Tecnico getTecnicoAsignado() {
		return tecnicoAsignado;
	}

	public List<HistorialTicket> getHistorial() {
		return List.copyOf(historial);
	}

	public void asignar(Tecnico tecnico, String observacion) {
		Objects.requireNonNull(tecnico, "tecnico es obligatorio");
		if (!tecnico.estaActivo() || !tecnico.puedeAtender(categoria) || !tecnico.tieneCapacidad()) {
			throw new TecnicoNoDisponibleException(
					"El técnico " + tecnico.getCodigo() + " no puede atender este ticket.");
		}
		transicionEstadoPolicy.validar(estado, EstadoTicket.ASIGNADO);
		tecnicoAsignado = tecnico;
		cambiarEstado(EstadoTicket.ASIGNADO, observacion, "sistema");
	}

	public void cambiarEstado(EstadoTicket nuevo, String observacion, String autor) {
		Objects.requireNonNull(observacion, "observacion es obligatoria");
		Objects.requireNonNull(autor, "autor es obligatorio");
		transicionEstadoPolicy.validar(estado, nuevo);
		EstadoTicket anterior = estado;
		estado = nuevo;
		historial.add(new HistorialTicket(null, anterior, nuevo, observacion, autor, Instant.now(clock)));
	}

	public void escalar(String observacion) {
		Objects.requireNonNull(observacion, "observacion es obligatoria");
		if (estado == EstadoTicket.RESUELTO || estado == EstadoTicket.CERRADO || estado == EstadoTicket.CANCELADO) {
			throw new TransicionInvalidaException(
					"No se puede escalar un ticket en estado " + estado + ".");
		}

		prioridad = prioridad.siguienteNivel();
		historial.add(new HistorialTicket(null, estado, estado, observacion, "SISTEMA", Instant.now(clock)));
	}

	public boolean estaVencido(Instant ahora) {
		return !fechaLimiteSla.isAfter(Objects.requireNonNull(ahora, "ahora es obligatorio"));
	}

	public boolean estaProximoAVencer(Instant ahora, Duration umbral) {
		Objects.requireNonNull(ahora, "ahora es obligatorio");
		Objects.requireNonNull(umbral, "umbral es obligatorio");
		if (umbral.isNegative()) {
			throw new IllegalArgumentException("umbral no puede ser negativo");
		}
		return fechaLimiteSla.isAfter(ahora)
				&& !fechaLimiteSla.isAfter(ahora.plus(umbral));
	}
}
