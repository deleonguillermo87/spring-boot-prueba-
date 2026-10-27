package com.helpflow.supportticket.domain.model;

public enum Prioridad {
	BAJA,
	MEDIA,
	ALTA,
	CRITICA;

	public Prioridad siguienteNivel() {
		return switch (this) {
			case BAJA -> MEDIA;
			case MEDIA -> ALTA;
			case ALTA, CRITICA -> CRITICA;
		};
	}
}
