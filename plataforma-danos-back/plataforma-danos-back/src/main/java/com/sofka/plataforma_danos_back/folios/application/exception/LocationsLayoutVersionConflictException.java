package com.sofka.plataforma_danos_back.folios.application.exception;

public class LocationsLayoutVersionConflictException extends RuntimeException {
    public LocationsLayoutVersionConflictException(String numeroFolio, Long expectedVersion, Long currentVersion) {
        super("La cotizacion %s tiene version %s y no coincide con la version actual %s".formatted(
                numeroFolio,
                expectedVersion,
                currentVersion
        ));
    }
}