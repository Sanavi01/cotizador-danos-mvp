package com.sofka.plataforma_danos_back.folios.domain;

public record AlertaBloqueante(
        String codigo,
        String mensaje,
        String severidad
) {
}