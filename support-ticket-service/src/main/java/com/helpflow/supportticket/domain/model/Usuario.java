package com.helpflow.supportticket.domain.model;

import java.util.Objects;

public class Usuario {

	private final Long id;
	private final String documento;
	private final String nombre;
	private final String correoCorporativo;
	private final String sede;
	private final String area;
	private final EstadoUsuario estado;
	private final Rol rol;
	private final String passwordHash;

	public Usuario(
			Long id,
			String documento,
			String nombre,
			String correoCorporativo,
			String sede,
			String area,
			EstadoUsuario estado,
			Rol rol,
			String passwordHash) {
		this.id = id;
		this.documento = Objects.requireNonNull(documento, "documento es obligatorio");
		this.nombre = Objects.requireNonNull(nombre, "nombre es obligatorio");
		this.correoCorporativo = Objects.requireNonNull(correoCorporativo, "correoCorporativo es obligatorio");
		this.sede = Objects.requireNonNull(sede, "sede es obligatoria");
		this.area = Objects.requireNonNull(area, "area es obligatoria");
		this.estado = Objects.requireNonNull(estado, "estado es obligatorio");
		this.rol = Objects.requireNonNull(rol, "rol es obligatorio");
		this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash es obligatorio");
	}

	public Long getId() {
		return id;
	}

	public String getDocumento() {
		return documento;
	}

	public String getNombre() {
		return nombre;
	}

	public String getCorreoCorporativo() {
		return correoCorporativo;
	}

	public String getSede() {
		return sede;
	}

	public String getArea() {
		return area;
	}

	public EstadoUsuario getEstado() {
		return estado;
	}

	public Rol getRol() {
		return rol;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public boolean estaActivo() {
		return estado == EstadoUsuario.ACTIVO;
	}
}
