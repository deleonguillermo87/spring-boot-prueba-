package com.helpflow.dispatch.exception;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DispatchExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidation(
			MethodArgumentNotValidException exception,
			HttpServletRequest request) {
		String detail = exception.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + " " + error.getDefaultMessage())
				.collect(Collectors.joining("; "));
		return badRequest(detail, request);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ProblemDetail handleUnreadableMessage(HttpServletRequest request) {
		return badRequest("El cuerpo JSON está malformado o contiene valores no válidos.", request);
	}

	private ProblemDetail badRequest(String detail, HttpServletRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
		problem.setType(URI.create("about:blank"));
		problem.setTitle("Bad Request");
		problem.setInstance(URI.create(request.getRequestURI()));
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}
}
