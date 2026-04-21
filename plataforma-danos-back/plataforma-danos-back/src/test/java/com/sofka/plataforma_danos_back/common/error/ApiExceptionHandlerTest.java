package com.sofka.plataforma_danos_back.common.error;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.sofka.plataforma_danos_back.folios.application.exception.IdempotencyConflictException;
import com.sofka.plataforma_danos_back.folios.application.exception.MissingIdempotencyKeyException;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void handleMissingIdempotencyKey_returnsBadRequestProblemDetail() {
        MissingIdempotencyKeyException exception = new MissingIdempotencyKeyException();

        var response = handler.handleMissingIdempotencyKey(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail problemDetail = assertInstanceOf(ProblemDetail.class, response.getBody());
        assertEquals("Idempotency Key requerida", problemDetail.getTitle());
        assertEquals("Idempotency-Key es obligatorio para crear folios", problemDetail.getDetail());
    }

    @Test
    void handleIdempotencyConflict_returnsConflictProblemDetail() {
        IdempotencyConflictException exception = new IdempotencyConflictException();

        var response = handler.handleIdempotencyConflict(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ProblemDetail problemDetail = assertInstanceOf(ProblemDetail.class, response.getBody());
        assertEquals("Conflicto de idempotencia", problemDetail.getTitle());
        assertEquals("La llave de idempotencia ya fue utilizada con una solicitud diferente", problemDetail.getDetail());
    }

    @Test
    void handleQuoteNotFound_returnsNotFoundProblemDetail() {
        QuoteNotFoundException exception = new QuoteNotFoundException("9999999");

        var response = handler.handleQuoteNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ProblemDetail problemDetail = assertInstanceOf(ProblemDetail.class, response.getBody());
        assertEquals("Cotizacion no encontrada", problemDetail.getTitle());
        assertEquals("No existe una cotizacion con numeroFolio 9999999", problemDetail.getDetail());
    }

    @Test
    void handleValidation_returnsBadRequestProblemDetail() throws Exception {
        Method method = SamplePayload.class.getDeclaredMethod("sample");
        BindingResult bindingResult = new BeanPropertyBindingResult(new SamplePayload(), "samplePayload");
        bindingResult.addError(new org.springframework.validation.FieldError(
                "samplePayload",
                "origin",
                "La solicitud contiene campos invalidos"
        ));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(new org.springframework.core.MethodParameter(method, -1), bindingResult);

        var response = handler.handleValidation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail problemDetail = assertInstanceOf(ProblemDetail.class, response.getBody());
        assertEquals("Validacion invalida", problemDetail.getTitle());
        assertEquals("La solicitud contiene campos invalidos", problemDetail.getDetail());
    }

    @Test
    void handleConstraintViolation_returnsBadRequestProblemDetail() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("El codigo postal debe tener 6 digitos");
        Set<ConstraintViolation<?>> violations = new LinkedHashSet<>();
        violations.add(violation);
        ConstraintViolationException exception = new ConstraintViolationException(violations);

        var response = handler.handleConstraintViolation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail problemDetail = assertInstanceOf(ProblemDetail.class, response.getBody());
        assertEquals("Validacion invalida", problemDetail.getTitle());
        assertTrue(problemDetail.getDetail().contains("El codigo postal debe tener 6 digitos"));
    }

    @Test
    void handleIllegalState_returnsInternalServerErrorProblemDetail() {
        IllegalStateException exception = new IllegalStateException("No fue posible procesar la solicitud");

        var response = handler.handleIllegalState(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ProblemDetail problemDetail = assertInstanceOf(ProblemDetail.class, response.getBody());
        assertEquals("Error interno", problemDetail.getTitle());
        assertEquals("No fue posible procesar la solicitud", problemDetail.getDetail());
    }

    private static final class SamplePayload {

        @SuppressWarnings("unused")
        void sample() {
        }
    }
}