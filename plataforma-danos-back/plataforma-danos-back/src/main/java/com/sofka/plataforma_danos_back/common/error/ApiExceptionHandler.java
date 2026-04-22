package com.sofka.plataforma_danos_back.common.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sofka.plataforma_danos_back.folios.application.exception.CoverageOptionsCatalogValidationException;
import com.sofka.plataforma_danos_back.folios.application.exception.CoverageOptionsVersionConflictException;
import com.sofka.plataforma_danos_back.folios.application.exception.GeneralInfoCatalogValidationException;
import com.sofka.plataforma_danos_back.folios.application.exception.GeneralInfoVersionConflictException;
import com.sofka.plataforma_danos_back.folios.application.exception.IdempotencyConflictException;
import com.sofka.plataforma_danos_back.folios.application.exception.InvalidCoverageOptionsPayloadException;
import com.sofka.plataforma_danos_back.folios.application.exception.InvalidLocationsPayloadException;
import com.sofka.plataforma_danos_back.folios.application.exception.LocationNotFoundException;
import com.sofka.plataforma_danos_back.folios.application.exception.LocationVersionConflictException;
import com.sofka.plataforma_danos_back.folios.application.exception.LocationsLayoutValidationException;
import com.sofka.plataforma_danos_back.folios.application.exception.LocationsLayoutVersionConflictException;
import com.sofka.plataforma_danos_back.folios.application.exception.MissingIdempotencyKeyException;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteCalculationRejectedException;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteCalculationVersionConflictException;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;

import jakarta.validation.ConstraintViolationException;

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

    @ExceptionHandler(QuoteCalculationVersionConflictException.class)
    public ResponseEntity<ProblemDetail> handleQuoteCalculationVersionConflict(QuoteCalculationVersionConflictException exception) {
        return problemDetail(HttpStatus.CONFLICT, "Conflicto de concurrencia", exception.getMessage());
    }

    @ExceptionHandler(QuoteCalculationRejectedException.class)
    public ResponseEntity<ProblemDetail> handleQuoteCalculationRejected(QuoteCalculationRejectedException exception) {
        return problemDetail(HttpStatus.UNPROCESSABLE_ENTITY, "Calculo no disponible", exception.getMessage());
    }

    @ExceptionHandler(GeneralInfoVersionConflictException.class)
    public ResponseEntity<ProblemDetail> handleGeneralInfoVersionConflict(GeneralInfoVersionConflictException exception) {
        return problemDetail(HttpStatus.CONFLICT, "Conflicto de concurrencia", exception.getMessage());
    }

    @ExceptionHandler(CoverageOptionsVersionConflictException.class)
    public ResponseEntity<ProblemDetail> handleCoverageOptionsVersionConflict(CoverageOptionsVersionConflictException exception) {
        return problemDetail(HttpStatus.CONFLICT, "Conflicto de concurrencia", exception.getMessage());
    }

    @ExceptionHandler(LocationVersionConflictException.class)
    public ResponseEntity<ProblemDetail> handleLocationVersionConflict(LocationVersionConflictException exception) {
        return problemDetail(HttpStatus.CONFLICT, "Conflicto de concurrencia", exception.getMessage());
    }

    @ExceptionHandler(LocationsLayoutVersionConflictException.class)
    public ResponseEntity<ProblemDetail> handleLocationsLayoutVersionConflict(LocationsLayoutVersionConflictException exception) {
        return problemDetail(HttpStatus.CONFLICT, "Conflicto de concurrencia", exception.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetail> handleOptimisticLockFailure(ObjectOptimisticLockingFailureException exception) {
        return problemDetail(HttpStatus.CONFLICT, "Conflicto de concurrencia", "La cotizacion fue modificada por otro usuario. Vuelva a consultarla e intente nuevamente.");
    }

    @ExceptionHandler(GeneralInfoCatalogValidationException.class)
    public ResponseEntity<ProblemDetail> handleGeneralInfoCatalogValidation(GeneralInfoCatalogValidationException exception) {
        return problemDetail(HttpStatus.UNPROCESSABLE_ENTITY, "Referencia de catalogo invalida", exception.getMessage());
    }

    @ExceptionHandler(CoverageOptionsCatalogValidationException.class)
    public ResponseEntity<ProblemDetail> handleCoverageOptionsCatalogValidation(CoverageOptionsCatalogValidationException exception) {
        return problemDetail(HttpStatus.UNPROCESSABLE_ENTITY, "Referencia de catalogo invalida", exception.getMessage());
    }

    @ExceptionHandler(LocationsLayoutValidationException.class)
    public ResponseEntity<ProblemDetail> handleLocationsLayoutValidation(LocationsLayoutValidationException exception) {
        return problemDetail(HttpStatus.UNPROCESSABLE_ENTITY, "Layout de ubicaciones invalido", exception.getMessage());
    }

    @ExceptionHandler(InvalidLocationsPayloadException.class)
    public ResponseEntity<ProblemDetail> handleInvalidLocationsPayload(InvalidLocationsPayloadException exception) {
        return problemDetail(HttpStatus.BAD_REQUEST, "Solicitud invalida", exception.getMessage());
    }

    @ExceptionHandler(InvalidCoverageOptionsPayloadException.class)
    public ResponseEntity<ProblemDetail> handleInvalidCoverageOptionsPayload(InvalidCoverageOptionsPayloadException exception) {
        return problemDetail(HttpStatus.BAD_REQUEST, "Solicitud invalida", exception.getMessage());
    }

    @ExceptionHandler(LocationNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleLocationNotFound(LocationNotFoundException exception) {
        return problemDetail(HttpStatus.NOT_FOUND, "Ubicacion no encontrada", exception.getMessage());
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
