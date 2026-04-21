package com.sofka.plataforma_danos_back.folios.application.exception;

public class InvalidLocationsPayloadException extends RuntimeException {
    public InvalidLocationsPayloadException(String message) {
        super(message);
    }
}