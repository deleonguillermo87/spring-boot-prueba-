package com.helpflow.supportticket.infrastructure.adapter.in.rest.controller;

import com.helpflow.supportticket.application.dto.Actor;
import com.helpflow.supportticket.application.dto.LoginCommand;
import com.helpflow.supportticket.application.dto.LoginResult;
import com.helpflow.supportticket.application.dto.RegistrarUsuarioCommand;
import com.helpflow.supportticket.application.port.in.LoginUseCase;
import com.helpflow.supportticket.application.port.in.RegistrarUsuarioUseCase;
import com.helpflow.supportticket.domain.model.Usuario;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.dto.LoginRequest;
import com.helpflow.supportticket.infrastructure.adapter.in.rest.dto.RegistroRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final RegistrarUsuarioUseCase registrarUsuario;
	private final LoginUseCase login;

	public AuthController(RegistrarUsuarioUseCase registrarUsuario, LoginUseCase login) {
		this.registrarUsuario = registrarUsuario;
		this.login = login;
	}

	@PostMapping("/register")
	public ResponseEntity<RegistroResponse> registrar(@Valid @RequestBody RegistroRequest request) {
		Usuario usuario = registrarUsuario.ejecutar(new RegistrarUsuarioCommand(
				request.documento(),
				request.nombre(),
				request.correoCorporativo(),
				request.sede(),
				request.area(),
				request.password(),
				Actor.sistema(),
				request.rol(),
				request.codigoTecnico()));
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(new RegistroResponse(usuario.getId(), usuario.getRol().name()));
	}

	@PostMapping("/login")
	public LoginResult login(@Valid @RequestBody LoginRequest request) {
		return login.ejecutar(new LoginCommand(
				request.correoCorporativo(),
				request.password(),
				Actor.sistema()));
	}

	public record RegistroResponse(Long id, String rol) {
	}
}
