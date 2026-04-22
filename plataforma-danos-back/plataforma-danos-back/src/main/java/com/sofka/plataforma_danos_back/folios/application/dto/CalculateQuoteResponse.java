package com.sofka.plataforma_danos_back.folios.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sofka.plataforma_danos_back.folios.domain.AlertaBloqueante;
import com.sofka.plataforma_danos_back.folios.domain.ComponenteComercialCalculado;
import com.sofka.plataforma_danos_back.folios.domain.ComponenteTecnicoCalculado;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.EstadoCalculo;
import com.sofka.plataforma_danos_back.folios.domain.EstadoCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.GarantiaCalculada;
import com.sofka.plataforma_danos_back.folios.domain.PrimaPorUbicacion;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record CalculateQuoteResponse(
        @Schema(description = "Numero de folio calculado", example = "1000001")
        String numeroFolio,
        @Schema(description = "Estado global de la cotizacion", example = "CALCULADA")
        EstadoCotizacion estadoCotizacion,
        @Schema(description = "Estado operativo del ultimo calculo", example = "PARCIAL")
        EstadoCalculo estadoCalculo,
        @Schema(description = "Prima neta consolidada del folio", example = "60000.00")
        BigDecimal primaNeta,
        @Schema(description = "Prima comercial consolidada del folio", example = "70200.00")
        BigDecimal primaComercial,
        @Schema(description = "Version del calculationParameters usada por el calculo", example = "1.0.0")
        String calculationParameterVersion,
        @Schema(description = "Desglose financiero por ubicacion")
        List<PrimaPorUbicacionView> primasPorUbicacion,
        @Schema(description = "Alertas vigentes consolidadas de la ejecucion del calculo")
        List<AlertaVigenteView> alertasVigentes,
        @Schema(description = "Momento en que se consolido el resultado", example = "2026-04-21T00:00:00Z")
        Instant calculatedAt,
        @Schema(description = "Version optimista luego de persistir el resultado", example = "5")
        Long version,
        @Schema(description = "Marca de tiempo de la ultima actualizacion logica", example = "2026-04-21T00:00:00Z")
        Instant fechaUltimaActualizacion
) {
    public CalculateQuoteResponse {
        primasPorUbicacion = primasPorUbicacion == null ? List.of() : List.copyOf(primasPorUbicacion);
        alertasVigentes = alertasVigentes == null ? List.of() : List.copyOf(alertasVigentes);
    }

    public static CalculateQuoteResponse from(
            Cotizacion cotizacion,
            List<PrimaPorUbicacion> primasPorUbicacion,
            List<AlertaBloqueante> alertasVigentes
    ) {
        List<PrimaPorUbicacionView> primas = primasPorUbicacion == null
                ? List.of()
                : primasPorUbicacion.stream().map(PrimaPorUbicacionView::from).toList();
        List<AlertaVigenteView> alertas = alertasVigentes == null
                ? List.of()
                : alertasVigentes.stream().map(AlertaVigenteView::from).toList();
        return new CalculateQuoteResponse(
                cotizacion.numeroFolio(),
                cotizacion.estadoCotizacion(),
                cotizacion.estadoCalculo(),
                cotizacion.primaNeta(),
                cotizacion.primaComercial(),
                cotizacion.calculationParameterVersion(),
                primas,
                alertas,
                cotizacion.calculatedAt(),
                cotizacion.version(),
                cotizacion.fechaUltimaActualizacion()
        );
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record PrimaPorUbicacionView(
            Integer indiceUbicacion,
            boolean ubicacionCalculable,
            BigDecimal primaNetaUbicacion,
            BigDecimal primaComercialUbicacion,
            List<GarantiaCalculadaView> garantiasCalculadas,
            List<ComponenteComercialView> componentesComerciales,
            List<AlertaVigenteView> alertas
    ) {
        public PrimaPorUbicacionView {
            garantiasCalculadas = garantiasCalculadas == null ? List.of() : List.copyOf(garantiasCalculadas);
            componentesComerciales = componentesComerciales == null ? List.of() : List.copyOf(componentesComerciales);
            alertas = alertas == null ? List.of() : List.copyOf(alertas);
        }

        public static PrimaPorUbicacionView from(PrimaPorUbicacion primaPorUbicacion) {
            return new PrimaPorUbicacionView(
                    primaPorUbicacion.indiceUbicacion(),
                    primaPorUbicacion.ubicacionCalculable(),
                    primaPorUbicacion.primaNetaUbicacion(),
                    primaPorUbicacion.primaComercialUbicacion(),
                    primaPorUbicacion.garantiasCalculadas().stream().map(GarantiaCalculadaView::from).toList(),
                    primaPorUbicacion.componentesComerciales().stream().map(ComponenteComercialView::from).toList(),
                    primaPorUbicacion.alertas().stream().map(AlertaVigenteView::from).toList()
            );
        }
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record GarantiaCalculadaView(
            String garantiaCode,
            BigDecimal primaGarantia,
            List<ComponenteTecnicoView> componentes
    ) {
        public GarantiaCalculadaView {
            componentes = componentes == null ? List.of() : List.copyOf(componentes);
        }

        public static GarantiaCalculadaView from(GarantiaCalculada garantiaCalculada) {
            return new GarantiaCalculadaView(
                    garantiaCalculada.garantiaCode(),
                    garantiaCalculada.primaGarantia(),
                    garantiaCalculada.componentes().stream().map(ComponenteTecnicoView::from).toList()
            );
        }
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record ComponenteTecnicoView(
            String tipo,
            String fuente,
            String lookupKey,
            BigDecimal rate,
            BigDecimal factor,
            BigDecimal monto
    ) {
        public static ComponenteTecnicoView from(ComponenteTecnicoCalculado componenteTecnicoCalculado) {
            return new ComponenteTecnicoView(
                    componenteTecnicoCalculado.tipo(),
                    componenteTecnicoCalculado.fuente(),
                    componenteTecnicoCalculado.lookupKey(),
                    componenteTecnicoCalculado.rate(),
                    componenteTecnicoCalculado.factor(),
                    componenteTecnicoCalculado.monto()
            );
        }
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record ComponenteComercialView(
            String tipo,
            BigDecimal porcentaje,
            BigDecimal base,
            BigDecimal monto
    ) {
        public static ComponenteComercialView from(ComponenteComercialCalculado componenteComercialCalculado) {
            return new ComponenteComercialView(
                    componenteComercialCalculado.tipo(),
                    componenteComercialCalculado.porcentaje(),
                    componenteComercialCalculado.base(),
                    componenteComercialCalculado.monto()
            );
        }
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record AlertaVigenteView(
            String codigo,
            String mensaje,
            String severidad
    ) {
        public static AlertaVigenteView from(AlertaBloqueante alertaBloqueante) {
            return new AlertaVigenteView(alertaBloqueante.codigo(), alertaBloqueante.mensaje(), alertaBloqueante.severidad());
        }
    }
}