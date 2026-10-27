package com.helpflow.supportticket.application.usecase;

import java.util.Objects;

import com.helpflow.supportticket.application.dto.LoginCommand;
import com.helpflow.supportticket.application.dto.LoginResult;
import com.helpflow.supportticket.application.port.in.LoginUseCase;
import com.helpflow.supportticket.application.port.out.MetricasPort;
import com.helpflow.supportticket.application.port.out.PasswordHasherPort;
import com.helpflow.supportticket.application.port.out.TokenProviderPort;
import com.helpflow.supportticket.application.port.out.UsuarioRepositoryPort;
import com.helpflow.supportticket.domain.exception.UsuarioInactivoException;
import com.helpflow.supportticket.domain.model.Usuario;
import org.springframework.stereotype.Service;

@Service
public class LoginService implements LoginUseCase {

	private final UsuarioRepositoryPort usuarioRepository;
	private final PasswordHasherPort passwordHasher;
	private final TokenProviderPort tokenProvider;
	private final MetricasPort metricas;

	public LoginService(
			UsuarioRepositoryPort usuarioRepository,
			PasswordHasherPort passwordHasher,
			TokenProviderPort tokenProvider,
			MetricasPort metricas) {
		this.usuarioRepository = usuarioRepository;
		this.passwordHasher = passwordHasher;
		this.tokenProvider = tokenProvider;
		this.metricas = metricas;
	}

	@Override
	public LoginResult ejecutar(LoginCommand command) {
		Objects.requireNonNull(command, "command es obligatorio");
		Usuario usuario = usuarioRepository.buscarPorCorreo(command.correoCorporativo())
				.orElse(null);
		if (usuario == null || !passwordHasher.coincide(command.password(), usuario.getPasswordHash())) {
			metricas.falloAutenticacion();
			throw new IllegalArgumentException("Credenciales inválidas.");
		}
		if (!usuario.estaActivo()) {
			metricas.falloAutenticacion();
			throw new UsuarioInactivoException("El usuario está inactivo.");
		}
		return new LoginResult(usuario.getId(), usuario.getRol(), tokenProvider.generar(usuario));
	}
}
