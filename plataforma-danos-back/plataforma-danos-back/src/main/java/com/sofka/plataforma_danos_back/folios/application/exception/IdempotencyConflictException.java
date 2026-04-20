package com.sofka.plataforma_danos_back.folios.application.exception;

public class IdempotencyConflictException extends RuntimeException {
    public IdempotencyConflictException() {
        super("La llave de idempotencia ya fue utilizada con una solicitud diferente");
    }
}
