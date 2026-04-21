package com.sofka.plataforma_danos_back.folios.application.dto;

import com.sofka.plataforma_danos_back.folios.domain.Giro;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionDetallePatch;
import com.sofka.plataforma_danos_back.folios.domain.ZonaCatastrofica;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateLocationRequest(
        @Schema(description = "Version actual del agregado para control optimista", example = "3")
        @NotNull(message = "La version es obligatoria")
        Long version,
        @Schema(description = "Campos a modificar en la ubicacion")
        @NotNull(message = "La solicitud de cambios es obligatoria")
        @Valid
        LocationChangesRequest changes
) {
    public record LocationChangesRequest(
            @Schema(description = "Nombre visible de la ubicacion", example = "Bodega secundaria")
            @Size(max = 160, message = "El nombre de la ubicacion no puede superar 160 caracteres")
            String nombreUbicacion,
            @Schema(description = "Direccion de la ubicacion", example = "Calle 50 # 20-30")
            @Size(max = 255, message = "La direccion no puede superar 255 caracteres")
            String direccion,
            @Schema(description = "Codigo postal capturado", example = "050001")
            @Size(max = 16, message = "El codigo postal no puede superar 16 caracteres")
            String codigoPostal,
            @Schema(description = "Estado o departamento de la ubicacion", example = "Antioquia")
            @Size(max = 100, message = "El estado no puede superar 100 caracteres")
            String estado,
            @Schema(description = "Municipio de la ubicacion", example = "Medellin")
            @Size(max = 100, message = "El municipio no puede superar 100 caracteres")
            String municipio,
            @Schema(description = "Colonia o barrio de la ubicacion")
            @Size(max = 100, message = "La colonia no puede superar 100 caracteres")
            String colonia,
            @Schema(description = "Ciudad de la ubicacion", example = "Medellin")
            @Size(max = 100, message = "La ciudad no puede superar 100 caracteres")
            String ciudad,
            @Schema(description = "Tipo constructivo del riesgo")
            @Size(max = 64, message = "El tipo constructivo no puede superar 64 caracteres")
            String tipoConstructivo,
            @Schema(description = "Nivel o piso", example = "1")
            @Min(value = 1, message = "El nivel debe ser al menos 1")
            @Max(value = 999, message = "El nivel no puede superar 999")
            Integer nivel,
            @Schema(description = "Anio de construccion", example = "2018")
            @Min(value = 1900, message = "El anio de construccion no es valido")
            @Max(value = 2100, message = "El anio de construccion no es valido")
            Integer anioConstruccion,
            @Schema(description = "Giro tecnico de la ubicacion")
            @Valid
            GiroRequest giro,
            @Schema(description = "Zona catastrofica derivada o capturada")
            @Valid
            ZonaCatastroficaRequest zonaCatastrofica
    ) {
        public UbicacionDetallePatch toPatch() {
            return new UbicacionDetallePatch(
                    normalize(nombreUbicacion),
                    normalize(direccion),
                    normalize(codigoPostal),
                    normalize(estado),
                    normalize(municipio),
                    normalize(colonia),
                    normalize(ciudad),
                    normalize(tipoConstructivo),
                    nivel,
                    anioConstruccion,
                    giro == null ? null : giro.toDomain(),
                    zonaCatastrofica == null ? null : zonaCatastrofica.toDomain()
            );
        }

        public boolean hasAnyChange() {
            return nombreUbicacion != null
                    || direccion != null
                    || codigoPostal != null
                    || estado != null
                    || municipio != null
                    || colonia != null
                    || ciudad != null
                    || tipoConstructivo != null
                    || nivel != null
                    || anioConstruccion != null
                    || giro != null
                    || zonaCatastrofica != null;
        }
    }

    public record GiroRequest(
            @Schema(description = "Codigo del giro")
            @Size(max = 32, message = "El codigo del giro no puede superar 32 caracteres")
            String codigo,
            @Schema(description = "Nombre del giro")
            @Size(max = 160, message = "El nombre del giro no puede superar 160 caracteres")
            String nombre,
            @Schema(description = "Clave de incendio")
            @Size(max = 32, message = "La clave de incendio no puede superar 32 caracteres")
            String claveIncendio
    ) {
        public Giro toDomain() {
            return new Giro(normalize(codigo), normalize(nombre), normalize(claveIncendio));
        }
    }

    public record ZonaCatastroficaRequest(
            @Schema(description = "Zona tecnica TEV")
            @Size(max = 32, message = "La zona TEV no puede superar 32 caracteres")
            String zonaTev,
            @Schema(description = "Zona tecnica FHM")
            @Size(max = 32, message = "La zona FHM no puede superar 32 caracteres")
            String zonaFhm
    ) {
        public ZonaCatastrofica toDomain() {
            return new ZonaCatastrofica(normalize(zonaTev), normalize(zonaFhm));
        }
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}