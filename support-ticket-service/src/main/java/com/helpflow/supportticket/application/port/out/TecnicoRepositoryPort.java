package com.helpflow.supportticket.application.port.out;

import java.util.List;
import java.util.Optional;

import com.helpflow.supportticket.domain.model.Categoria;
import com.helpflow.supportticket.domain.model.Tecnico;

public interface TecnicoRepositoryPort {

	Optional<Tecnico> buscarPorCodigo(String codigo);

	List<Tecnico> buscarCandidatos(Categoria categoria, String sede);

	Tecnico guardar(Tecnico tecnico);
}
