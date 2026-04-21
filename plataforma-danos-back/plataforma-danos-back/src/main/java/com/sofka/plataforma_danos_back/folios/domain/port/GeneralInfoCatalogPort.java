package com.sofka.plataforma_danos_back.folios.domain.port;

public interface GeneralInfoCatalogPort {
    boolean isActiveAgent(String codigoAgente);

    boolean isActiveRiskClassification(String clasificacionRiesgo);

    boolean isActiveBusinessLine(String tipoNegocio);
}