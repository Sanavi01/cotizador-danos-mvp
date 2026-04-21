package com.sofka.plataforma_core_ohs.web;

import com.sofka.plataforma_core_ohs.application.ReferenceCoreBadRequestException;
import com.sofka.plataforma_core_ohs.application.ReferenceCoreNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class ProblemDetailsHandler {

	@ExceptionHandler(ReferenceCoreBadRequestException.class)
	public ResponseEntity<ProblemDetail> handleBadRequest(ReferenceCoreBadRequestException exception, HttpServletRequest request) {
		return buildProblem(HttpStatus.BAD_REQUEST, "Solicitud invalida", exception.getMessage(), request, "CORE_BAD_REQUEST");
	}

	@ExceptionHandler(ReferenceCoreNotFoundException.class)
	public ResponseEntity<ProblemDetail> handleNotFound(ReferenceCoreNotFoundException exception, HttpServletRequest request) {
		return buildProblem(HttpStatus.NOT_FOUND, "Recurso no encontrado", exception.getMessage(), request, "CORE_NOT_FOUND");
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
		String detail = "El cuerpo de la solicitud contiene errores de validacion.";
		FieldError fieldError = exception.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
		if (fieldError != null && fieldError.getDefaultMessage() != null) {
			detail = fieldError.getDefaultMessage();
		}
		return buildProblem(HttpStatus.BAD_REQUEST, "Validacion fallida", detail, request, "CORE_VALIDATION_ERROR");
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException exception, HttpServletRequest request) {
		String detail = exception.getConstraintViolations().stream()
				.findFirst()
				.map(violation -> violation.getMessage())
				.orElse("La solicitud contiene datos invalidos.");
		return buildProblem(HttpStatus.BAD_REQUEST, "Validacion fallida", detail, request, "CORE_CONSTRAINT_VIOLATION");
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ProblemDetail> handleUnreadable(HttpMessageNotReadableException exception, HttpServletRequest request) {
		return buildProblem(HttpStatus.BAD_REQUEST, "Cuerpo invalido", "No fue posible interpretar el cuerpo de la solicitud.", request, "CORE_BODY_INVALID");
	}

	private ResponseEntity<ProblemDetail> buildProblem(HttpStatus status, String title, String detail, HttpServletRequest request, String code) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
		problemDetail.setTitle(title);
		problemDetail.setType(URI.create("about:blank"));
		problemDetail.setInstance(URI.create(request.getRequestURI()));
		problemDetail.setProperty("code", code);
		return ResponseEntity.status(status).header(HttpHeaders.CONTENT_TYPE, "application/problem+json").body(problemDetail);
	}
}