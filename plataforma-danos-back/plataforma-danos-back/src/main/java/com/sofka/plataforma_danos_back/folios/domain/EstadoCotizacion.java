package com.sofka.plataforma_danos_back.folios.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estados de negocio de la cotizacion")
public enum EstadoCotizacion {
    BORRADOR,
    EN_CAPTURA,
    LISTA_PARA_CALCULO,
    CALCULADA
}
