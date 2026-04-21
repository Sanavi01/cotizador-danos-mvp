package com.sofka.plataforma_danos_back.folios.application.exception;

public class CoverageOptionsVersionConflictException extends RuntimeException {
    public CoverageOptionsVersionConflictException(String numeroFolio, Long requestVersion, Long currentVersion) {
        super("La version de opcionesCobertura para el folio %s no coincide. Solicitada: %s, vigente: %s".formatted(
                numeroFolio,
                requestVersion,
                currentVersion
        ));
    }
}