package com.helpflow.supportticket.unit.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.helpflow.supportticket.domain.model.EstadoUsuario;
import com.helpflow.supportticket.domain.model.Rol;
import com.helpflow.supportticket.domain.model.Usuario;
import com.helpflow.supportticket.infrastructure.security.ActorResolver;
import com.helpflow.supportticket.infrastructure.security.BCryptPasswordHasherAdapter;
import com.helpflow.supportticket.infrastructure.security.JwtProperties;
import com.helpflow.supportticket.infrastructure.security.JwtTokenProviderAdapter;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

class JwtSecurityComponentsTest {

	private static final String SECRET = "a-long-test-secret-key-at-least-32-bytes";

	@Test
	void generatedTokenHasExpectedClaimsAndDecodes() {
		JwtProperties properties = properties(SECRET, 60);
		JwtEncoder encoder = encoder(SECRET);
		JwtTokenProviderAdapter provider = new JwtTokenProviderAdapter(encoder, properties);
		NimbusJwtDecoder decoder = decoder(SECRET);

		Jwt jwt = decoder.decode(provider.generar(usuario()));

		assertEquals("42", jwt.getSubject());
		assertEquals("ROLE_TECNICO", jwt.getClaimAsString("rol"));
		assertEquals("tec@helpflow.test", jwt.getClaimAsString("correo"));
		assertEquals(MacAlgorithm.HS256.getName(), jwt.getHeaders().get("alg"));
		assertNotEquals(null, jwt.getIssuedAt());
		assertEquals(jwt.getIssuedAt().plusSeconds(3_600), jwt.getExpiresAt());
	}

	@Test
	void decoderRejectsExpiredToken() {
		Instant expiredAt = Instant.now().minusSeconds(180);
		String token = encoder(SECRET).encode(JwtEncoderParameters.from(
				JwsHeader.with(MacAlgorithm.HS256).build(),
				JwtClaimsSet.builder()
						.subject("42")
						.claim("rol", "ROLE_USUARIO")
						.issuedAt(expiredAt.minusSeconds(60))
						.expiresAt(expiredAt)
						.build())).getTokenValue();

		assertThrows(JwtException.class, () -> decoder(SECRET).decode(token));
	}

	@Test
	void decoderRejectsInvalidSignature() {
		String token = encoder(SECRET).encode(JwtEncoderParameters.from(
				JwsHeader.with(MacAlgorithm.HS256).build(),
				JwtClaimsSet.builder()
						.subject("42")
						.claim("rol", "ROLE_USUARIO")
						.issuedAt(Instant.now())
						.expiresAt(Instant.now().plusSeconds(60))
						.build())).getTokenValue();

		assertThrows(JwtException.class, () -> decoder("different-invalid-signature-key-32bytes").decode(token));
	}

	@Test
	void actorResolverMapsSubjectAndRole() {
		Jwt jwt = Jwt.withTokenValue("token")
				.header("alg", "HS256")
				.subject("51")
				.claim("rol", "ROLE_ADMIN")
				.issuedAt(Instant.now())
				.expiresAt(Instant.now().plusSeconds(60))
				.build();

		var actor = new ActorResolver().resolver(jwt);

		assertEquals(51L, actor.id());
		assertEquals(Rol.ROLE_ADMIN, actor.rol());
	}

	@Test
	void actorResolverRejectsUnknownRole() {
		Jwt jwt = Jwt.withTokenValue("token")
				.header("alg", "HS256")
				.subject("51")
				.claim("rol", "ADMIN")
				.issuedAt(Instant.now())
				.expiresAt(Instant.now().plusSeconds(60))
				.build();

		assertThrows(IllegalArgumentException.class, () -> new ActorResolver().resolver(jwt));
	}

	@Test
	void passwordHasherUsesOneWayBcryptHash() {
		BCryptPasswordHasherAdapter hasher = new BCryptPasswordHasherAdapter(new BCryptPasswordEncoder());

		String encoded = hasher.hash("safe-password");

		assertNotEquals("safe-password", encoded);
		assertTrue(hasher.coincide("safe-password", encoded));
	}

	private JwtProperties properties(String secret, long expirationMinutes) {
		JwtProperties properties = new JwtProperties();
		properties.setSecret(secret);
		properties.setExpirationMinutes(expirationMinutes);
		properties.validate();
		return properties;
	}

	private JwtEncoder encoder(String secret) {
		return new NimbusJwtEncoder(new ImmutableSecret<>(key(secret)));
	}

	private NimbusJwtDecoder decoder(String secret) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key(secret))
				.macAlgorithm(MacAlgorithm.HS256)
				.build();
		decoder.setJwtValidator(new JwtTimestampValidator(java.time.Duration.ZERO));
		return decoder;
	}

	private SecretKey key(String value) {
		return new SecretKeySpec(value.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
	}

	private Usuario usuario() {
		return new Usuario(
				42L, "DOC-42", "Técnico", "tec@helpflow.test", "CENTRAL", "TI",
				EstadoUsuario.ACTIVO, Rol.ROLE_TECNICO, "bcrypt-hash");
	}
}
