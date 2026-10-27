package com.helpflow.supportticket.application.dto;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public record PaginaResultado<T>(
		List<T> contenido,
		int pagina,
		int tamano,
		long totalElementos,
		int totalPaginas,
		boolean primera,
		boolean ultima) {

	public PaginaResultado {
		contenido = List.copyOf(Objects.requireNonNull(contenido, "contenido es obligatorio"));
	}

	public static <S, T> PaginaResultado<T> map(
			PaginaResultado<S> pagina,
			Function<? super S, T> mapper) {
		Objects.requireNonNull(pagina, "pagina es obligatoria");
		Objects.requireNonNull(mapper, "mapper es obligatorio");
		return new PaginaResultado<>(
				pagina.contenido().stream().map(mapper).toList(),
				pagina.pagina(),
				pagina.tamano(),
				pagina.totalElementos(),
				pagina.totalPaginas(),
				pagina.primera(),
				pagina.ultima());
	}
}
