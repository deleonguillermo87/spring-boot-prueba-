package com.helpflow.supportticket.infrastructure.adapter.out.persistence.entity;

import java.util.Objects;

import com.helpflow.supportticket.domain.model.EstadoUsuario;
import com.helpflow.supportticket.domain.model.Rol;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class UsuarioEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false, unique = true, length = 80)
	private String documento;
	@Column(nullable = false, length = 160)
	private String nombre;
	@Column(name = "correo_corporativo", nullable = false, unique = true, length = 200)
	private String correoCorporativo;
	@Column(nullable = false, length = 120)
	private String sede;
	@Column(nullable = false, length = 120)
	private String area;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EstadoUsuario estado;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private Rol rol;
	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	public UsuarioEntity() {
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public String getDocumento() { return documento; }
	public void setDocumento(String documento) { this.documento = documento; }
	public String getNombre() { return nombre; }
	public void setNombre(String nombre) { this.nombre = nombre; }
	public String getCorreoCorporativo() { return correoCorporativo; }
	public void setCorreoCorporativo(String correoCorporativo) { this.correoCorporativo = correoCorporativo; }
	public String getSede() { return sede; }
	public void setSede(String sede) { this.sede = sede; }
	public String getArea() { return area; }
	public void setArea(String area) { this.area = area; }
	public EstadoUsuario getEstado() { return estado; }
	public void setEstado(EstadoUsuario estado) { this.estado = estado; }
	public Rol getRol() { return rol; }
	public void setRol(Rol rol) { this.rol = rol; }
	public String getPasswordHash() { return passwordHash; }
	public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}
