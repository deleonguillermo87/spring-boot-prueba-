package com.helpflow.supportticket.application.port.in;

import com.helpflow.supportticket.application.dto.LoginCommand;
import com.helpflow.supportticket.application.dto.LoginResult;

public interface LoginUseCase {

	LoginResult ejecutar(LoginCommand command);
}
