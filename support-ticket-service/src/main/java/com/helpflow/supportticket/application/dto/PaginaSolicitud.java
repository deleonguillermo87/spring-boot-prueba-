package com.helpflow.supportticket.application.dto;

public record PaginaSolicitud(
		int pagina,
		int tamano,
		String ordenarPor,
		boolean descendente) {

	public PaginaSolicitud {
		pagina = Math.max(0, pagina);
		tamano = tamano <= 0 ? 10 : Math.min(tamano, 100);
		ordenarPor = ordenarPor == null || ordenarPor.isBlank() ? "fechaCreacion" : ordenarPor;
	}

	public PaginaSolicitud() {
		this(0, 10, "fechaCreacion", true);
	}

	public PaginaSolicitud(int pagina, int tamano) {
		this(pagina, tamano, "fechaCreacion", true);
	}

	public PaginaSolicitud(int pagina, int tamano, String ordenarPor) {
		this(pagina, tamano, ordenarPor, true);
	}
}
