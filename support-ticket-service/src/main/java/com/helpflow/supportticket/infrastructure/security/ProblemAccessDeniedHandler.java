package com.helpflow.supportticket.infrastructure.security;

import java.io.IOException;

import com.helpflow.supportticket.application.port.out.MetricasPort;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class ProblemAccessDeniedHandler implements AccessDeniedHandler {

	private final MetricasPort metricas;

	public ProblemAccessDeniedHandler(MetricasPort metricas) {
		this.metricas = metricas;
	}

	@Override
	public void handle(
			HttpServletRequest request,
			HttpServletResponse response,
			AccessDeniedException exception) throws IOException, ServletException {
		metricas.accesoDenegado();
		ProblemResponseWriter.write(request, response, 403, "Forbidden", "No tiene permiso para acceder al recurso.");
	}
}
