package com.helpflow.supportticket.infrastructure.adapter.in.rest.dto;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.helpflow.supportticket.application.dto.PaginaResultado;

public record PaginaResponse<T>(
		List<T> contenido,
		int pagina,
		int tamano,
		long totalElementos,
		int totalPaginas,
		boolean primera,
		boolean ultima) {

	public PaginaResponse {
		contenido = contenido == null ? List.of() : List.copyOf(contenido);
	}

	public static <S, T> PaginaResponse<T> desde(
			PaginaResultado<S> resultado,
			Function<? super S, T> mapper) {
		Objects.requireNonNull(resultado, "resultado es obligatorio");
		Objects.requireNonNull(mapper, "mapper es obligatorio");
		return new PaginaResponse<>(
				resultado.contenido().stream().map(mapper).toList(),
				resultado.pagina(),
				resultado.tamano(),
				resultado.totalElementos(),
				resultado.totalPaginas(),
				resultado.primera(),
				resultado.ultima());
	}
}

