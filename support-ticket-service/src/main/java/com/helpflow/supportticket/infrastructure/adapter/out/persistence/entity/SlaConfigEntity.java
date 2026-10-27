package com.helpflow.supportticket.infrastructure.adapter.out.persistence.entity;

import com.helpflow.supportticket.domain.model.Prioridad;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sla_config")
public class SlaConfigEntity {

	@Id
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Prioridad prioridad;
	@Column(nullable = false)
	private int horas;

	public SlaConfigEntity() {
	}

	public Prioridad getPrioridad() { return prioridad; }
	public void setPrioridad(Prioridad prioridad) { this.prioridad = prioridad; }
	public int getHoras() { return horas; }
	public void setHoras(int horas) { this.horas = horas; }
}
