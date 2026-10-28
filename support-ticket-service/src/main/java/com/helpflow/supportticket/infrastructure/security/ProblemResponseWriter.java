package com.helpflow.supportticket.infrastructure.security;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;

final class ProblemResponseWriter {

	private ProblemResponseWriter() {
	}

	static void write(
			HttpServletRequest request,
			HttpServletResponse response,
			int status,
			String title,
			String detail) throws IOException {
		String instance = request.getRequestURI();
		String traceId = MDC.get("traceId");
		if (traceId == null || traceId.isBlank()) {
			traceId = UUID.randomUUID().toString();
		}
		String code = status == 401 ? "autenticacion-requerida" : "acceso-denegado";
		String body = "{\"type\":\"urn:helpflow:problem:" + code + "\",\"title\":\"" + escape(title)
				+ "\",\"status\":" + status
				+ ",\"detail\":\"" + escape(detail)
				+ "\",\"instance\":\"" + escape(instance)
				+ "\",\"timestamp\":\"" + Instant.now()
				+ "\",\"traceId\":\"" + escape(traceId) + "\"}";
		response.setStatus(status);
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/problem+json");
		response.getWriter().write(body);
	}

	private static String escape(String value) {
		StringBuilder escaped = new StringBuilder(value.length());
		for (int index = 0; index < value.length(); index++) {
			char character = value.charAt(index);
			switch (character) {
				case '"' -> escaped.append("\\\"");
				case '\\' -> escaped.append("\\\\");
				case '\b' -> escaped.append("\\b");
				case '\f' -> escaped.append("\\f");
				case '\n' -> escaped.append("\\n");
				case '\r' -> escaped.append("\\r");
				case '\t' -> escaped.append("\\t");
				default -> {
					if (character < 0x20) {
						escaped.append(String.format("\\u%04x", (int) character));
					} else {
						escaped.append(character);
					}
				}
			}
		}
		return escaped.toString();
	}
}
