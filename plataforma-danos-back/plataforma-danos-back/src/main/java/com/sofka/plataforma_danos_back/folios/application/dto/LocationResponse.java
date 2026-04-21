package com.sofka.plataforma_danos_back.folios.application.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sofka.plataforma_danos_back.folios.domain.AlertaBloqueante;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.EstadoValidacion;
import com.sofka.plataforma_danos_back.folios.domain.Garantia;
import com.sofka.plataforma_danos_back.folios.domain.Giro;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionDetalle;
import com.sofka.plataforma_danos_back.folios.domain.ZonaCatastrofica;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record LocationResponse(
        @Schema(description = "Numero de folio consultado", example = "1000001")
        String numeroFolio,
        @Schema(description = "Version optimista del agregado raiz", example = "3")
        Long version,
        @Schema(description = "Marca de tiempo de la ultima actualizacion logica", example = "2026-04-21T00:00:00Z")
        Instant fechaUltimaActualizacion,
        @Schema(description = "Ubicacion actualizada")
        LocationView ubicacion
) {
    public static LocationResponse from(Cotizacion cotizacion, UbicacionCotizacion ubicacion) {
        return new LocationResponse(cotizacion.numeroFolio(), cotizacion.version(), cotizacion.fechaUltimaActualizacion(), LocationView.from(ubicacion));
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record LocationView(
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
            GiroView giro,
            List<GarantiaView> garantias,
            ZonaCatastroficaView zonaCatastrofica,
            EstadoValidacion estadoValidacion,
            List<AlertaBloqueanteView> alertasBloqueantes,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static LocationView from(UbicacionCotizacion ubicacion) {
            UbicacionDetalle detalle = ubicacion.detalle();
            return new LocationView(
                    detalle.indice(),
                    detalle.nombreUbicacion(),
                    detalle.direccion(),
                    detalle.codigoPostal(),
                    detalle.estado(),
                    detalle.municipio(),
                    detalle.colonia(),
                    detalle.ciudad(),
                    detalle.tipoConstructivo(),
                    detalle.nivel(),
                    detalle.anioConstruccion(),
                    detalle.giro() == null ? null : GiroView.from(detalle.giro()),
                    resolveGarantias(ubicacion),
                    detalle.zonaCatastrofica() == null ? null : ZonaCatastroficaView.from(detalle.zonaCatastrofica()),
                    ubicacion.estadoValidacion(),
                    ubicacion.alertasBloqueantes().stream().map(AlertaBloqueanteView::from).toList(),
                    ubicacion.createdAt(),
                    ubicacion.updatedAt()
            );
        }

        private static List<GarantiaView> resolveGarantias(UbicacionCotizacion ubicacion) {
            if (ubicacion.estadoValidacion() != EstadoValidacion.CALCULABLE) {
                return List.of();
            }
            return List.of(new GarantiaView("GAR-INC-ED", "GLOBAL", true));
        }
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record GiroView(
            String codigo,
            String nombre,
            String claveIncendio
    ) {
        public static GiroView from(Giro giro) {
            return new GiroView(giro.codigo(), giro.nombre(), giro.claveIncendio());
        }
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record GarantiaView(
            String garantiaCode,
            String origen,
            boolean tariffablePreview
    ) {
        public static GarantiaView from(Garantia garantia) {
            return new GarantiaView(garantia.garantiaCode(), garantia.origen(), garantia.tariffablePreview());
        }
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record ZonaCatastroficaView(
            String zonaTev,
            String zonaFhm
    ) {
        public static ZonaCatastroficaView from(ZonaCatastrofica zonaCatastrofica) {
            return new ZonaCatastroficaView(zonaCatastrofica.zonaTev(), zonaCatastrofica.zonaFhm());
        }
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record AlertaBloqueanteView(
            String codigo,
            String mensaje,
            String severidad
    ) {
        public static AlertaBloqueanteView from(AlertaBloqueante alertaBloqueante) {
            return new AlertaBloqueanteView(alertaBloqueante.codigo(), alertaBloqueante.mensaje(), alertaBloqueante.severidad());
        }
    }
}