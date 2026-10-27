package com.helpflow.supportticket.unit.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.helpflow.supportticket.application.port.out.PasswordHasherPort;
import com.helpflow.supportticket.application.port.out.UsuarioRepositoryPort;
import com.helpflow.supportticket.domain.model.Rol;
import com.helpflow.supportticket.domain.model.Usuario;
import com.helpflow.supportticket.infrastructure.security.AdminBootstrap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.mock.env.MockEnvironment;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapTest {

	@Mock UsuarioRepositoryPort usuarios;
	@Mock PasswordHasherPort hasher;

	@Test
	void doesNothingWhenAnyRequiredEnvironmentVariableIsMissing() throws Exception {
		MockEnvironment environment = new MockEnvironment()
				.withProperty("ADMIN_EMAIL", "admin@helpflow.test")
				.withProperty("ADMIN_DOCUMENTO", "DOC-ADMIN");

		new AdminBootstrap(usuarios, hasher, environment).run(new DefaultApplicationArguments());

		verify(usuarios, never()).guardar(any());
		verify(hasher, never()).hash(any());
	}

	@Test
	void createsAdminWhenAllRequiredValuesArePresent() throws Exception {
		MockEnvironment environment = completeEnvironment();
		when(usuarios.existePorCorreo("admin@helpflow.test")).thenReturn(false);
		when(hasher.hash("admin-secret")).thenReturn("bcrypt-hash");

		new AdminBootstrap(usuarios, hasher, environment).run(new DefaultApplicationArguments());

		verify(usuarios).guardar(org.mockito.ArgumentMatchers.argThat(usuario ->
				usuario.getRol() == Rol.ROLE_ADMIN
						&& usuario.getCorreoCorporativo().equals("admin@helpflow.test")
						&& usuario.getPasswordHash().equals("bcrypt-hash")));
	}

	@Test
	void doesNotDuplicateAnExistingAdminEmail() throws Exception {
		MockEnvironment environment = completeEnvironment();
		when(usuarios.existePorCorreo("admin@helpflow.test")).thenReturn(true);

		new AdminBootstrap(usuarios, hasher, environment).run(new DefaultApplicationArguments());

		verify(usuarios, never()).guardar(any());
		verify(hasher, never()).hash(any());
	}

	private MockEnvironment completeEnvironment() {
		return new MockEnvironment()
				.withProperty("ADMIN_EMAIL", "Admin@Helpflow.test")
				.withProperty("ADMIN_PASSWORD", "admin-secret")
				.withProperty("ADMIN_DOCUMENTO", "DOC-ADMIN");
	}
}
