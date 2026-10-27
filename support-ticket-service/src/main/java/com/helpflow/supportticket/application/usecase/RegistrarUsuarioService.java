package com.helpflow.supportticket.application.usecase;

import java.util.Locale;
import java.util.Objects;

import com.helpflow.supportticket.application.dto.RegistrarUsuarioCommand;
import com.helpflow.supportticket.application.port.in.RegistrarUsuarioUseCase;
import com.helpflow.supportticket.application.port.out.PasswordHasherPort;
import com.helpflow.supportticket.application.port.out.TecnicoRepositoryPort;
import com.helpflow.supportticket.application.port.out.UsuarioRepositoryPort;
import com.helpflow.supportticket.domain.exception.AccesoNoPermitidoException;
import com.helpflow.supportticket.domain.model.EstadoUsuario;
import com.helpflow.supportticket.domain.model.Rol;
import com.helpflow.supportticket.domain.model.Tecnico;
import com.helpflow.supportticket.domain.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrarUsuarioService implements RegistrarUsuarioUseCase {

	private static final java.util.Set<String> DOMINIOS_PERSONALES = java.util.Set.of(
			"gmail.com", "yahoo.com", "hotmail.com", "outlook.com", "icloud.com");

	private final UsuarioRepositoryPort usuarioRepository;
	private final PasswordHasherPort passwordHasher;
	private final TecnicoRepositoryPort tecnicoRepository;

	public RegistrarUsuarioService(
			UsuarioRepositoryPort usuarioRepository,
			PasswordHasherPort passwordHasher,
			TecnicoRepositoryPort tecnicoRepository) {
		this.usuarioRepository = usuarioRepository;
		this.passwordHasher = passwordHasher;
		this.tecnicoRepository = tecnicoRepository;
	}

	@Override
	@Transactional
	public Usuario ejecutar(RegistrarUsuarioCommand command) {
		Objects.requireNonNull(command, "command es obligatorio");
		if (command.actor() == null
				|| (command.actor().rol() != Rol.ROLE_USUARIO && command.actor().rol() != Rol.ROLE_ADMIN)) {
			throw new AccesoNoPermitidoException("El actor no puede registrar usuarios.");
		}
		requerido(command.documento(), "documento");
		requerido(command.nombre(), "nombre");
		requerido(command.sede(), "sede");
		requerido(command.area(), "area");
		requerido(command.password(), "password");
		if (!esCorreoCorporativo(command.correoCorporativo())) {
			throw new IllegalArgumentException("Se requiere una dirección de correo corporativa válida.");
		}
		if (usuarioRepository.existePorDocumento(command.documento())) {
			throw new IllegalArgumentException("El documento ya está registrado.");
		}
		String correo = command.correoCorporativo().trim().toLowerCase(Locale.ROOT);
		if (usuarioRepository.existePorCorreo(correo)) {
			throw new IllegalArgumentException("El correo ya está registrado.");
		}
		Tecnico tecnicoParaVincular = null;
		Rol rol = Rol.ROLE_USUARIO;
		if ("TECNICO".equalsIgnoreCase(command.rolSolicitado())) {
			if (command.codigoTecnico() == null || command.codigoTecnico().isBlank()) {
				throw new IllegalArgumentException("codigoTecnico es obligatorio para el registro de técnico.");
			}
			tecnicoParaVincular = tecnicoRepository.buscarPorCodigo(command.codigoTecnico().trim())
					.orElseThrow(() -> new IllegalArgumentException("El código de técnico no existe."));
			if (tecnicoParaVincular.getUsuarioId() != null
					|| !tecnicoParaVincular.getDocumento().equals(command.documento().trim())) {
				throw new IllegalArgumentException(
						"El técnico ya está vinculado o el documento no coincide.");
			}
			rol = Rol.ROLE_TECNICO;
		}
		Usuario usuario = new Usuario(
				null, command.documento().trim(), command.nombre().trim(), correo,
				command.sede().trim(), command.area().trim(), EstadoUsuario.ACTIVO,
				rol, passwordHasher.hash(command.password()));
		Usuario guardado = usuarioRepository.guardar(usuario);
		if (tecnicoParaVincular != null) {
			tecnicoRepository.guardar(new Tecnico(
					tecnicoParaVincular.getId(),
					guardado.getId(),
					tecnicoParaVincular.getCodigo(),
					tecnicoParaVincular.getDocumento(),
					tecnicoParaVincular.getNombre(),
					tecnicoParaVincular.getSede(),
					tecnicoParaVincular.getEspecialidades(),
					tecnicoParaVincular.getCargaMaxima(),
					tecnicoParaVincular.getTicketsActivos(),
					tecnicoParaVincular.getEstado()));
		}
		return guardado;
	}

	private boolean esCorreoCorporativo(String correo) {
		if (correo == null || !correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
			return false;
		}
		String dominio = correo.substring(correo.lastIndexOf('@') + 1).toLowerCase(Locale.ROOT);
		return !DOMINIOS_PERSONALES.contains(dominio);
	}

	private void requerido(String value, String campo) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(campo + " es obligatorio.");
		}
	}
}
