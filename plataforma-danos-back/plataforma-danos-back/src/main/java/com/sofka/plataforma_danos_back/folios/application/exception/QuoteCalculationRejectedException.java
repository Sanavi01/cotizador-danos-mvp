package com.sofka.plataforma_danos_back.folios.application.exception;

public class QuoteCalculationRejectedException extends RuntimeException {
    public QuoteCalculationRejectedException(String message) {
        super(message);
    }
}