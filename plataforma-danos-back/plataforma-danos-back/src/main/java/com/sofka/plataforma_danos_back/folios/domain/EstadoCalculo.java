package com.sofka.plataforma_danos_back.folios.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estado operativo del ultimo calculo financiero")
public enum EstadoCalculo {
    CALCULADO,
    PARCIAL,
    RECHAZADO
}