package com.sofka.plataforma_danos_back.folios.application.exception;

import java.util.List;

public class GeneralInfoCatalogValidationException extends RuntimeException {
    public GeneralInfoCatalogValidationException(List<String> invalidFields) {
        super("Las referencias de catalogo no son validas: %s".formatted(String.join(", ", invalidFields)));
    }
}