package com.sofka.plataforma_danos_back.folios.application.exception;

public class LocationVersionConflictException extends RuntimeException {
    public LocationVersionConflictException(String numeroFolio, Long expectedVersion, Long currentVersion) {
        super("La cotizacion %s tiene version %s y no coincide con la version actual %s".formatted(
                numeroFolio,
                expectedVersion,
                currentVersion
        ));
    }
}