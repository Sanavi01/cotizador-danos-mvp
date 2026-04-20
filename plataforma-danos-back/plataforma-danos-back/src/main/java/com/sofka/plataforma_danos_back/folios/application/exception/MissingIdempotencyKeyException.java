package com.sofka.plataforma_danos_back.folios.application.exception;

public class MissingIdempotencyKeyException extends RuntimeException {
    public MissingIdempotencyKeyException() {
        super("Idempotency-Key es obligatorio para crear folios");
    }
}
