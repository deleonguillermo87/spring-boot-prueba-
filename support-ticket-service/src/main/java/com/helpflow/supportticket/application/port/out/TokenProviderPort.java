package com.helpflow.supportticket.application.port.out;

import com.helpflow.supportticket.domain.model.Usuario;

public interface TokenProviderPort {

	String generar(Usuario usuario);
}
