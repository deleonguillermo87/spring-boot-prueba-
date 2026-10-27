package com.helpflow.supportticket.domain.model;

import java.util.Objects;
import java.util.Set;

public class Tecnico {

	private final Long id;
	private final String codigo;
	private final String documento;
	private final String nombre;
	private final String sede;
	private final Set<Categoria> especialidades;
	private final int cargaMaxima;
	private final int ticketsActivos;
	private final EstadoUsuario estado;

	public Tecnico(
			Long id,
			String codigo,
			String documento,
			String nombre,
			String sede,
			Set<Categoria> especialidades,
			int cargaMaxima,
			int ticketsActivos,
			EstadoUsuario estado) {
		if (cargaMaxima <= 0) {
			throw new IllegalArgumentException("cargaMaxima debe ser positiva");
		}
		if (ticketsActivos < 0) {
			throw new IllegalArgumentException("ticketsActivos no puede ser negativo");
		}
		this.id = id;
		this.codigo = Objects.requireNonNull(codigo, "codigo es obligatorio");
		this.documento = Objects.requireNonNull(documento, "documento es obligatorio");
		this.nombre = Objects.requireNonNull(nombre, "nombre es obligatorio");
		this.sede = Objects.requireNonNull(sede, "sede es obligatoria");
		this.especialidades = Set.copyOf(Objects.requireNonNull(especialidades, "especialidades son obligatorias"));
		this.cargaMaxima = cargaMaxima;
		this.ticketsActivos = ticketsActivos;
		this.estado = Objects.requireNonNull(estado, "estado es obligatorio");
	}

	public Long getId() {
		return id;
	}

	public String getCodigo() {
		return codigo;
	}

	public String getDocumento() {
		return documento;
	}

	public String getNombre() {
		return nombre;
	}

	public String getSede() {
		return sede;
	}

	public Set<Categoria> getEspecialidades() {
		return especialidades;
	}

	public int getCargaMaxima() {
		return cargaMaxima;
	}

	public int getTicketsActivos() {
		return ticketsActivos;
	}

	public EstadoUsuario getEstado() {
		return estado;
	}

	public boolean puedeAtender(Categoria categoria) {
		return categoria != null && especialidades.contains(categoria);
	}

	public boolean tieneCapacidad() {
		return ticketsActivos < cargaMaxima;
	}

	public boolean estaActivo() {
		return estado == EstadoUsuario.ACTIVO;
	}
}
