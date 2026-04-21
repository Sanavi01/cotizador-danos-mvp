package com.sofka.plataforma_danos_back.folios.domain;

public record UbicacionDetalle(
        Integer indice,
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
    public UbicacionDetalle merge(UbicacionDetallePatch patch) {
        return new UbicacionDetalle(
                indice,
                patch.nombreUbicacion() != null ? patch.nombreUbicacion() : nombreUbicacion,
                patch.direccion() != null ? patch.direccion() : direccion,
                patch.codigoPostal() != null ? patch.codigoPostal() : codigoPostal,
                patch.estado() != null ? patch.estado() : estado,
                patch.municipio() != null ? patch.municipio() : municipio,
                patch.colonia() != null ? patch.colonia() : colonia,
                patch.ciudad() != null ? patch.ciudad() : ciudad,
                patch.tipoConstructivo() != null ? patch.tipoConstructivo() : tipoConstructivo,
                patch.nivel() != null ? patch.nivel() : nivel,
                patch.anioConstruccion() != null ? patch.anioConstruccion() : anioConstruccion,
                patch.giro() != null ? patch.giro() : giro,
                patch.zonaCatastrofica() != null ? patch.zonaCatastrofica() : zonaCatastrofica
        );
    }

    public boolean hasAnyData() {
        return hasText(nombreUbicacion)
                || hasText(direccion)
                || hasText(codigoPostal)
                || hasText(estado)
                || hasText(municipio)
                || hasText(colonia)
                || hasText(ciudad)
                || hasText(tipoConstructivo)
                || nivel != null
                || anioConstruccion != null
                || giro != null
                || zonaCatastrofica != null;
    }

    public boolean hasMinimalDraftData() {
        return hasText(nombreUbicacion) && hasText(codigoPostal);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}