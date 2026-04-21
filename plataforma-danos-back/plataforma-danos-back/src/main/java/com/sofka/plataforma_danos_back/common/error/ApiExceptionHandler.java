package com.sofka.plataforma_danos_back.common.error;

import com.sofka.plataforma_danos_back.folios.application.exception.IdempotencyConflictException;
import com.sofka.plataforma_danos_back.folios.application.exception.MissingIdempotencyKeyException;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MissingIdempotencyKeyException.class)
    public ResponseEntity<ProblemDetail> handleMissingIdempotencyKey(MissingIdempotencyKeyException exception) {
        return problemDetail(HttpStatus.BAD_REQUEST, "Idempotency Key requerida", exception.getMessage());
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<ProblemDetail> handleIdempotencyConflict(IdempotencyConflictException exception) {
        return problemDetail(HttpStatus.CONFLICT, "Conflicto de idempotencia", exception.getMessage());
    }

    @ExceptionHandler(QuoteNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleQuoteNotFound(QuoteNotFoundException exception) {
        return problemDetail(HttpStatus.NOT_FOUND, "Cotizacion no encontrada", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException exception) {
        return problemDetail(HttpStatus.BAD_REQUEST, "Validacion invalida", "La solicitud contiene campos invalidos");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException exception) {
        return problemDetail(HttpStatus.BAD_REQUEST, "Validacion invalida", exception.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalState(IllegalStateException exception) {
        return problemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", exception.getMessage());
    }

    private ResponseEntity<ProblemDetail> problemDetail(HttpStatus status, String title, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle(title);
        problemDetail.setDetail(detail);
        return ResponseEntity.status(status).body(problemDetail);
    }
}
