package com.sofka.plataforma_danos_back.folios.application.exception;

import java.util.List;

public class CoverageOptionsCatalogValidationException extends RuntimeException {
    public CoverageOptionsCatalogValidationException(List<String> invalidGuaranteeCodes) {
        super("Las garantias no estan activas o no existen en el catalogo aprobado: %s".formatted(invalidGuaranteeCodes));
    }
}