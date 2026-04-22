package com.sofka.plataforma_danos_back.folios.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.EstadoCalculo;
import com.sofka.plataforma_danos_back.folios.domain.EstadoCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.PrimaPorUbicacion;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record QuoteStateResponse(
        @Schema(description = "Numero de folio consultado", example = "1000001")
        String numeroFolio,
        @Schema(description = "Estado global consolidado de la cotizacion", example = "EN_CAPTURA")
        EstadoCotizacion estadoCotizacion,
        @Schema(description = "Version optimista vigente del agregado", example = "4")
        Long version,
        @Schema(description = "Marca de tiempo de la ultima actualizacion logica", example = "2026-04-21T00:00:00Z")
        Instant fechaUltimaActualizacion,
        @Schema(description = "Estado de completitud por seccion funcional")
        ProgresoCotizacion progreso,
        @Schema(description = "Conteos operativos de ubicaciones esperadas y persistidas")
        ResumenUbicaciones resumenUbicaciones,
        @Schema(description = "Indica si existe al menos una alerta vigente", example = "true")
        boolean tieneAlertas,
        @Schema(description = "Alertas vigentes agregadas desde las ubicaciones y secciones")
        List<AlertaVigente> alertasVigentes,
        @Schema(description = "Indica si el folio ya cumple la elegibilidad minima para calcular", example = "false")
        boolean readyToCalculate,
        @Schema(description = "Resumen compacto del ultimo resultado financiero persistido cuando exista")
        ResultadoFinancieroResumen resultadoFinanciero
) {
    public QuoteStateResponse {
        alertasVigentes = alertasVigentes == null ? List.of() : List.copyOf(alertasVigentes);
    }

    @Schema(description = "Estado de completitud de una seccion funcional")
    public enum EstadoSeccion {
        COMPLETED,
        INCOMPLETE
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record ProgresoCotizacion(
            @Schema(description = "Estado de la seccion de datos generales", example = "COMPLETED")
            EstadoSeccion datosGenerales,
            @Schema(description = "Estado de la seccion de layout de ubicaciones", example = "COMPLETED")
            EstadoSeccion layoutUbicaciones,
            @Schema(description = "Estado de la captura de ubicaciones contra el layout", example = "INCOMPLETE")
            EstadoSeccion ubicaciones,
            @Schema(description = "Estado de la seccion global de opciones de cobertura", example = "INCOMPLETE")
            EstadoSeccion opcionesCobertura
    ) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record ResumenUbicaciones(
            @Schema(description = "Cantidad de slots esperados segun el layout", example = "3")
            int totalEsperado,
            @Schema(description = "Cantidad de ubicaciones realmente persistidas", example = "2")
            int totalActual,
            @Schema(description = "Cantidad de ubicaciones calculables", example = "1")
            int calculables,
            @Schema(description = "Cantidad de ubicaciones incompletas", example = "0")
            int incompletas,
            @Schema(description = "Cantidad de ubicaciones invalidas", example = "1")
            int invalidas,
            @Schema(description = "Cantidad de ubicaciones con alertas vigentes", example = "1")
            int conAlertas
    ) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record AlertaVigente(
            @Schema(description = "Codigo canonico de la alerta", example = "UBICACION_SIN_ZIP")
            String codigo,
            @Schema(description = "Mensaje legible de la alerta", example = "La ubicacion no tiene codigo postal valido.")
            String mensaje,
            @Schema(description = "Severidad de la alerta", example = "Warning")
            String severidad
    ) {
    }

    @Schema(description = "Estado operativo del ultimo calculo persistido")
    public enum EstadoCalculoResumen {
        CALCULADO,
        PARCIAL,
        RECHAZADO
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record ResultadoFinancieroResumen(
            @Schema(description = "Prima neta vigente del ultimo calculo", example = "60000.00")
            BigDecimal primaNeta,
            @Schema(description = "Prima comercial vigente del ultimo calculo", example = "70200.00")
            BigDecimal primaComercial,
            @Schema(description = "Cantidad de ubicaciones calculadas en el ultimo snapshot", example = "1")
            int ubicacionesCalculadas,
            @Schema(description = "Cantidad de ubicaciones excluidas o no calculables en el ultimo snapshot", example = "2")
            int ubicacionesNoCalculables,
            @Schema(description = "Estado operativo del ultimo calculo", example = "PARCIAL")
            EstadoCalculoResumen estadoCalculo,
            @Schema(description = "Momento en que se consolido el ultimo resultado financiero", example = "2026-04-21T00:00:00Z")
            Instant calculatedAt,
            @Schema(description = "Version del calculationParameters usada por el ultimo calculo", example = "1.0.0")
            String calculationParameterVersion
    ) {
                public static ResultadoFinancieroResumen from(Cotizacion cotizacion, List<PrimaPorUbicacion> primasPorUbicacion) {
                        if (cotizacion == null || cotizacion.primaNeta() == null || cotizacion.primaComercial() == null || cotizacion.estadoCalculo() == null || cotizacion.calculatedAt() == null) {
                                return null;
                        }

                        List<PrimaPorUbicacion> primas = primasPorUbicacion == null ? List.of() : primasPorUbicacion;
                        int ubicacionesCalculadas = (int) primas.stream().filter(PrimaPorUbicacion::ubicacionCalculable).count();
                        int ubicacionesNoCalculables = primas.size() - ubicacionesCalculadas;

                        return new ResultadoFinancieroResumen(
                                        cotizacion.primaNeta(),
                                        cotizacion.primaComercial(),
                                        ubicacionesCalculadas,
                                        ubicacionesNoCalculables,
                                        mapEstadoCalculo(cotizacion.estadoCalculo()),
                                        cotizacion.calculatedAt(),
                                        cotizacion.calculationParameterVersion()
                        );
                }

                private static EstadoCalculoResumen mapEstadoCalculo(EstadoCalculo estadoCalculo) {
                        return estadoCalculo == null ? null : EstadoCalculoResumen.valueOf(estadoCalculo.name());
                }
    }
}
