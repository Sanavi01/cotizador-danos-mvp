package com.sofka.plataforma_danos_back.folios.application.exception;

public class LocationNotFoundException extends RuntimeException {
    public LocationNotFoundException(String numeroFolio, Integer indice) {
        super("No existe una ubicacion con indice %s en la cotizacion %s".formatted(indice, numeroFolio));
    }
}