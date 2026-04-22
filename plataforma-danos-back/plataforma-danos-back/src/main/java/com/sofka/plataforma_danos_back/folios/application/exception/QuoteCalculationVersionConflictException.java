package com.sofka.plataforma_danos_back.folios.application.exception;

public class QuoteCalculationVersionConflictException extends RuntimeException {
    public QuoteCalculationVersionConflictException(String numeroFolio, Long requestVersion, Long currentVersion) {
        super("La version de calculo para el folio %s no coincide. Solicitada: %s, vigente: %s".formatted(
                numeroFolio,
                requestVersion,
                currentVersion
        ));
    }
}