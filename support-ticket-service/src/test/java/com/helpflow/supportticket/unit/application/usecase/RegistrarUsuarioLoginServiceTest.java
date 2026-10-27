package com.helpflow.supportticket.unit.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.helpflow.supportticket.application.dto.LoginCommand;
import com.helpflow.supportticket.application.dto.RegistrarUsuarioCommand;
import com.helpflow.supportticket.application.port.out.MetricasPort;
import com.helpflow.supportticket.application.port.out.PasswordHasherPort;
import com.helpflow.supportticket.application.port.out.TokenProviderPort;
import com.helpflow.supportticket.application.port.out.TecnicoRepositoryPort;
import com.helpflow.supportticket.application.port.out.UsuarioRepositoryPort;
import com.helpflow.supportticket.application.usecase.LoginService;
import com.helpflow.supportticket.application.usecase.RegistrarUsuarioService;
import com.helpflow.supportticket.domain.exception.UsuarioInactivoException;
import com.helpflow.supportticket.domain.model.EstadoUsuario;
import com.helpflow.supportticket.domain.model.Usuario;
import com.helpflow.supportticket.domain.model.Tecnico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegistrarUsuarioLoginServiceTest {

	@Mock UsuarioRepositoryPort usuarioRepository;
	@Mock TecnicoRepositoryPort tecnicoRepository;
	@Mock PasswordHasherPort passwordHasher;
	@Mock TokenProviderPort tokenProvider;
	@Mock MetricasPort metricas;

	private RegistrarUsuarioService registrarService;
	private LoginService loginService;

	@BeforeEach
	void setUp() {
		registrarService = new RegistrarUsuarioService(usuarioRepository, passwordHasher, tecnicoRepository);
		loginService = new LoginService(usuarioRepository, passwordHasher, tokenProvider, metricas);
	}

	@Test
	void rejectsDuplicateDocument() {
		when(usuarioRepository.existePorDocumento("DOC-5")).thenReturn(true);

		assertThrows(IllegalArgumentException.class, () -> registrarService.ejecutar(registerCommand()));
		verify(passwordHasher, never()).hash(any());
	}

	@Test
	void rejectsDuplicateEmail() {
		when(usuarioRepository.existePorDocumento("DOC-5")).thenReturn(false);
		when(usuarioRepository.existePorCorreo("new@helpflow.com")).thenReturn(true);

		assertThrows(IllegalArgumentException.class, () -> registrarService.ejecutar(registerCommand()));
	}

	@Test
	void rejectsPersonalEmailAddress() {
		RegistrarUsuarioCommand command = new RegistrarUsuarioCommand(
				"DOC-5", "New", "new@gmail.com", "CENTRAL", "TI",
				"raw-password", ApplicationTestFixtures.adminActor());

		assertThrows(IllegalArgumentException.class, () -> registrarService.ejecutar(command));
	}

	@Test
	void hashesPasswordAndPersistsCorporateUser() {
		when(usuarioRepository.existePorDocumento("DOC-5")).thenReturn(false);
		when(usuarioRepository.existePorCorreo("new@helpflow.com")).thenReturn(false);
		when(passwordHasher.hash("raw-password")).thenReturn("hashed");
		when(usuarioRepository.guardar(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Usuario result = registrarService.ejecutar(registerCommand());

		assertEquals(EstadoUsuario.ACTIVO, result.getEstado());
		assertEquals("hashed", result.getPasswordHash());
		verify(passwordHasher).hash("raw-password");
	}

	@Test
	void registersAndLinksTechnicianOnlyWhenCodeAndDocumentMatch() {
		Tecnico tecnico = new Tecnico(
				14L, "TEC-014", "TEC-DOC-14", "Técnico TEC-014", "CENTRAL",
				java.util.Set.of(com.helpflow.supportticket.domain.model.Categoria.RED),
				5, 1, EstadoUsuario.ACTIVO);
		when(usuarioRepository.existePorDocumento("TEC-DOC-14")).thenReturn(false);
		when(usuarioRepository.existePorCorreo("new@helpflow.com")).thenReturn(false);
		when(tecnicoRepository.buscarPorCodigo("TEC-014")).thenReturn(Optional.of(tecnico));
		when(passwordHasher.hash("raw-password")).thenReturn("hashed");
		when(usuarioRepository.guardar(any(Usuario.class))).thenAnswer(invocation -> {
			Usuario value = invocation.getArgument(0);
			return new Usuario(
					42L, value.getDocumento(), value.getNombre(), value.getCorreoCorporativo(),
					value.getSede(), value.getArea(), value.getEstado(), value.getRol(), value.getPasswordHash());
		});

		Usuario registered = registrarService.ejecutar(new RegistrarUsuarioCommand(
				"TEC-DOC-14", "Tecnico", "new@helpflow.com", "CENTRAL", "TI",
				"raw-password", ApplicationTestFixtures.adminActor(), "TECNICO", "TEC-014"));

		assertEquals(com.helpflow.supportticket.domain.model.Rol.ROLE_TECNICO, registered.getRol());
		verify(tecnicoRepository).guardar(org.mockito.ArgumentMatchers.argThat(linked ->
				linked.getId().equals(tecnico.getId()) && linked.getUsuarioId().equals(42L)));
	}

	@Test
	void doesNotAllowAdminRoleThroughRegistration() {
		when(usuarioRepository.existePorDocumento("DOC-5")).thenReturn(false);
		when(usuarioRepository.existePorCorreo("new@helpflow.com")).thenReturn(false);
		when(passwordHasher.hash("raw-password")).thenReturn("hashed");
		when(usuarioRepository.guardar(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Usuario registered = registrarService.ejecutar(new RegistrarUsuarioCommand(
				"DOC-5", "Nuevo Usuario", "new@helpflow.com", "CENTRAL", "TI",
				"raw-password", ApplicationTestFixtures.adminActor(), "ADMIN", null));

		assertEquals(com.helpflow.supportticket.domain.model.Rol.ROLE_USUARIO, registered.getRol());
	}

	@Test
	void rejectsInvalidCredentialsAndRecordsMetric() {
		when(usuarioRepository.buscarPorCorreo("user1@helpflow.com")).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> loginService.ejecutar(
				new LoginCommand("user1@helpflow.com", "wrong", ApplicationTestFixtures.usuarioActor())));

		verify(metricas).falloAutenticacion();
	}

	@Test
	void rejectsInactiveUserAndRecordsMetric() {
		Usuario inactive = ApplicationTestFixtures.usuario(1L, EstadoUsuario.INACTIVO);
		when(usuarioRepository.buscarPorCorreo(inactive.getCorreoCorporativo())).thenReturn(Optional.of(inactive));
		when(passwordHasher.coincide("correct", inactive.getPasswordHash())).thenReturn(true);

		assertThrows(UsuarioInactivoException.class, () -> loginService.ejecutar(
				new LoginCommand(inactive.getCorreoCorporativo(), "correct", ApplicationTestFixtures.usuarioActor())));

		verify(metricas).falloAutenticacion();
	}

	@Test
	void issuesTokenForValidCredentials() {
		Usuario active = ApplicationTestFixtures.usuario();
		when(usuarioRepository.buscarPorCorreo(active.getCorreoCorporativo())).thenReturn(Optional.of(active));
		when(passwordHasher.coincide("correct", active.getPasswordHash())).thenReturn(true);
		when(tokenProvider.generar(active)).thenReturn("signed-token");

		assertEquals("signed-token", loginService.ejecutar(new LoginCommand(
				active.getCorreoCorporativo(), "correct", ApplicationTestFixtures.usuarioActor())).token());
	}

	private RegistrarUsuarioCommand registerCommand() {
		return new RegistrarUsuarioCommand(
				"DOC-5", "Nuevo Usuario", "new@helpflow.com", "CENTRAL", "TI",
				"raw-password", ApplicationTestFixtures.adminActor());
	}
}
