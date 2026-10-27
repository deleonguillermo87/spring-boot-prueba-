package com.helpflow.supportticket.infrastructure.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import com.helpflow.supportticket.application.port.out.TokenProviderPort;
import com.helpflow.supportticket.domain.model.Usuario;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProviderAdapter implements TokenProviderPort {

	private final JwtEncoder encoder;
	private final JwtProperties properties;

	public JwtTokenProviderAdapter(JwtEncoder encoder, JwtProperties properties) {
		this.encoder = encoder;
		this.properties = properties;
	}

	@Override
	public String generar(Usuario usuario) {
		Objects.requireNonNull(usuario, "usuario es obligatorio");
		Instant issuedAt = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.subject(String.valueOf(usuario.getId()))
				.claim("rol", usuario.getRol().name())
				.claim("correo", usuario.getCorreoCorporativo())
				.issuedAt(issuedAt)
				.expiresAt(issuedAt.plus(properties.getExpirationMinutes(), ChronoUnit.MINUTES))
				.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}
}
