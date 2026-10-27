package com.helpflow.supportticket.application.port.in;

import com.helpflow.supportticket.application.dto.RegistrarUsuarioCommand;
import com.helpflow.supportticket.domain.model.Usuario;

public interface RegistrarUsuarioUseCase {

	Usuario ejecutar(RegistrarUsuarioCommand command);
}
