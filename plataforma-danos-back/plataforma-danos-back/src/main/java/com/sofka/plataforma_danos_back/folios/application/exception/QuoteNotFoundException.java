package com.sofka.plataforma_danos_back.folios.application.exception;

public class QuoteNotFoundException extends RuntimeException {
    public QuoteNotFoundException(String numeroFolio) {
        super("No existe una cotizacion con numeroFolio %s".formatted(numeroFolio));
    }
}
