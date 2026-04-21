package com.sofka.plataforma_danos_back.folios.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Fuente tecnica preliminar usada para la previsualizacion de coberturas")
public enum TechnicalPreviewSource {
    CORE_TARIFF,
    FIRE_TARIFF,
    CAT_TARIFF,
    FHM_TARIFF,
    ELECTRONIC_FACTOR,
    UNRESOLVED
}