package com.helpflow.supportticket.infrastructure.security;

import java.util.Locale;

import com.helpflow.supportticket.application.port.out.PasswordHasherPort;
import com.helpflow.supportticket.application.port.out.UsuarioRepositoryPort;
import com.helpflow.supportticket.domain.model.EstadoUsuario;
import com.helpflow.supportticket.domain.model.Rol;
import com.helpflow.supportticket.domain.model.Usuario;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements ApplicationRunner {

	private final UsuarioRepositoryPort usuarios;
	private final PasswordHasherPort passwordHasher;
	private final Environment environment;

	public AdminBootstrap(
			UsuarioRepositoryPort usuarios,
			PasswordHasherPort passwordHasher,
			Environment environment) {
		this.usuarios = usuarios;
		this.passwordHasher = passwordHasher;
		this.environment = environment;
	}

	@Override
	public void run(ApplicationArguments arguments) {
		String email = environment.getProperty("ADMIN_EMAIL");
		String password = environment.getProperty("ADMIN_PASSWORD");
		String documento = environment.getProperty("ADMIN_DOCUMENTO");
		if (vacio(email) || vacio(password) || vacio(documento)) {
			return;
		}
		String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
		if (usuarios.existePorCorreo(normalizedEmail)) {
			return;
		}
		usuarios.guardar(new Usuario(
				null,
				documento.trim(),
				"Administrador inicial",
				normalizedEmail,
				"CENTRAL",
				"ADMINISTRACION",
				EstadoUsuario.ACTIVO,
				Rol.ROLE_ADMIN,
				passwordHasher.hash(password)));
	}

	private boolean vacio(String value) {
		return value == null || value.isBlank();
	}
}
