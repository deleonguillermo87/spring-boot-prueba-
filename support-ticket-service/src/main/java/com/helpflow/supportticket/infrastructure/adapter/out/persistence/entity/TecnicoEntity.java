package com.helpflow.supportticket.infrastructure.adapter.out.persistence.entity;

import java.util.HashSet;
import java.util.Set;

import com.helpflow.supportticket.domain.model.Categoria;
import com.helpflow.supportticket.domain.model.EstadoUsuario;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "tecnicos")
public class TecnicoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false, unique = true, length = 20)
	private String codigo;
	@Column(nullable = false, unique = true, length = 80)
	private String documento;
	@Column(nullable = false, length = 160)
	private String nombre;
	@Column(nullable = false, length = 120)
	private String sede;
	@Column(name = "carga_maxima", nullable = false)
	private int cargaMaxima;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EstadoUsuario estado;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_id")
	private UsuarioEntity usuario;
	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "tecnico_especialidades", joinColumns = @JoinColumn(name = "tecnico_id"))
	@Column(name = "categoria", nullable = false, length = 30)
	@Enumerated(EnumType.STRING)
	@BatchSize(size = 50)
	private Set<Categoria> especialidades = new HashSet<>();

	public TecnicoEntity() {
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public String getCodigo() { return codigo; }
	public void setCodigo(String codigo) { this.codigo = codigo; }
	public String getDocumento() { return documento; }
	public void setDocumento(String documento) { this.documento = documento; }
	public String getNombre() { return nombre; }
	public void setNombre(String nombre) { this.nombre = nombre; }
	public String getSede() { return sede; }
	public void setSede(String sede) { this.sede = sede; }
	public int getCargaMaxima() { return cargaMaxima; }
	public void setCargaMaxima(int cargaMaxima) { this.cargaMaxima = cargaMaxima; }
	public EstadoUsuario getEstado() { return estado; }
	public void setEstado(EstadoUsuario estado) { this.estado = estado; }
	public UsuarioEntity getUsuario() { return usuario; }
	public void setUsuario(UsuarioEntity usuario) { this.usuario = usuario; }
	public Set<Categoria> getEspecialidades() { return especialidades; }
	public void setEspecialidades(Set<Categoria> especialidades) { this.especialidades = especialidades; }
}
