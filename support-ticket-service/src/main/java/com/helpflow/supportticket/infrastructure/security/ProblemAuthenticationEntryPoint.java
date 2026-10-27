package com.helpflow.supportticket.infrastructure.security;

import java.io.IOException;

import com.helpflow.supportticket.application.port.out.MetricasPort;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class ProblemAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final MetricasPort metricas;

	public ProblemAuthenticationEntryPoint(MetricasPort metricas) {
		this.metricas = metricas;
	}

	@Override
	public void commence(
			HttpServletRequest request,
			HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		metricas.falloAutenticacion();
		ProblemResponseWriter.write(request, response, 401, "Unauthorized", "Autenticación requerida o inválida.");
	}
}
