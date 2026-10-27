package com.helpflow.supportticket.application.port.out;

import java.util.Optional;

import com.helpflow.supportticket.domain.model.Usuario;

public interface UsuarioRepositoryPort {

	Usuario guardar(Usuario usuario);

	Optional<Usuario> buscarPorId(Long id);

	Optional<Usuario> buscarPorCorreo(String correoCorporativo);

	boolean existePorDocumento(String documento);

	boolean existePorCorreo(String correoCorporativo);
}
