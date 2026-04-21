package com.sofka.plataforma_danos_back.folios.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Modo de captura del layout de ubicaciones")
public enum ModoCaptura {
    UNICA,
    MULTIPLE
}