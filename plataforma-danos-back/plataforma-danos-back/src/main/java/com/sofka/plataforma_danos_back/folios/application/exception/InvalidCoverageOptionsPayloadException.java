package com.sofka.plataforma_danos_back.folios.application.exception;

public class InvalidCoverageOptionsPayloadException extends RuntimeException {
    public InvalidCoverageOptionsPayloadException(String message) {
        super(message);
    }
}