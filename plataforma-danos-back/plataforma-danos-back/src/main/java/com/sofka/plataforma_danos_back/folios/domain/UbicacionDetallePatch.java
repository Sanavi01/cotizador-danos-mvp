package com.sofka.plataforma_danos_back.folios.domain;

public record UbicacionDetallePatch(
        String nombreUbicacion,
        String direccion,
        String codigoPostal,
        String estado,
        String municipio,
        String colonia,
        String ciudad,
        String tipoConstructivo,
        Integer nivel,
        Integer anioConstruccion,
        Giro giro,
        ZonaCatastrofica zonaCatastrofica
) {
}