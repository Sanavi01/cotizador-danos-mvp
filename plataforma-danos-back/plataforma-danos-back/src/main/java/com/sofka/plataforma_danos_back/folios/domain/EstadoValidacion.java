package com.sofka.plataforma_danos_back.folios.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estado de validacion de una ubicacion")
public enum EstadoValidacion {
    EMPTY,
    INCOMPLETE,
    INVALID,
    VALID,
    CALCULABLE
}