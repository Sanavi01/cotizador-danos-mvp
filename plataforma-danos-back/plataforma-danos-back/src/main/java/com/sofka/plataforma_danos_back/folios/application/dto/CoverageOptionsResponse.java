package com.sofka.plataforma_danos_back.folios.application.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.CoverageGuaranteePreview;
import com.sofka.plataforma_danos_back.folios.domain.CoverageOptions;
import com.sofka.plataforma_danos_back.folios.domain.CoverageProjectionPerLocation;
import com.sofka.plataforma_danos_back.folios.domain.SelectedGuarantee;
import com.sofka.plataforma_danos_back.folios.domain.TechnicalPreviewSource;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record CoverageOptionsResponse(
        @Schema(description = "Numero de folio consultado", example = "1000001")
        String numeroFolio,
        @Schema(description = "Version optimista del agregado raiz", example = "3")
        Long version,
        @Schema(description = "Marca de tiempo de la ultima actualizacion logica", example = "2026-04-21T00:00:00Z")
        Instant fechaUltimaActualizacion,
        @Schema(description = "Configuracion vigente de opciones de cobertura")
        CoverageOptionsView opcionesCobertura,
        @Schema(description = "Proyeccion derivada por ubicacion")
        List<ProjectionPerLocationView> projectionPerLocation
) {
    public static CoverageOptionsResponse from(
            Cotizacion cotizacion,
            CoverageOptions coverageOptions,
            List<CoverageProjectionPerLocation> projectionPerLocation
    ) {
        CoverageOptions options = coverageOptions == null ? CoverageOptions.empty(cotizacion.id()) : coverageOptions;
        return new CoverageOptionsResponse(
                cotizacion.numeroFolio(),
                cotizacion.version(),
                cotizacion.fechaUltimaActualizacion(),
                new CoverageOptionsView(
                        options.garantiasSeleccionadas().stream().map(SelectedGuaranteeView::from).toList(),
                        options.observaciones()
                ),
                projectionPerLocation == null ? List.of() : projectionPerLocation.stream().map(ProjectionPerLocationView::from).toList()
        );
    }

    public static CoverageOptionsResponse empty(Cotizacion cotizacion) {
        return new CoverageOptionsResponse(
                cotizacion.numeroFolio(),
                cotizacion.version(),
                cotizacion.fechaUltimaActualizacion(),
                new CoverageOptionsView(List.of(), null),
                List.of()
        );
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record CoverageOptionsView(
            List<SelectedGuaranteeView> garantiasSeleccionadas,
            String observaciones
    ) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record SelectedGuaranteeView(
            String garantiaCode,
            List<String> terminos
    ) {
        public static SelectedGuaranteeView from(SelectedGuarantee selectedGuarantee) {
            return new SelectedGuaranteeView(selectedGuarantee.garantiaCode(), selectedGuarantee.terminos());
        }
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record ProjectionPerLocationView(
            Integer indice,
            List<DerivedGuaranteeView> garantiasDerivadas,
            boolean calculablePreview
    ) {
        public static ProjectionPerLocationView from(CoverageProjectionPerLocation projection) {
            return new ProjectionPerLocationView(
                    projection.indice(),
                    projection.garantiasDerivadas().stream().map(DerivedGuaranteeView::from).toList(),
                    projection.calculablePreview()
            );
        }
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record DerivedGuaranteeView(
            String garantiaCode,
            boolean tariffablePreview,
            TechnicalPreviewSource fuenteTecnicaPreview,
            String lookupKeyPreview,
            List<String> motivosNoTarifable
    ) {
        public static DerivedGuaranteeView from(CoverageGuaranteePreview preview) {
            return new DerivedGuaranteeView(
                    preview.garantiaCode(),
                    preview.tariffablePreview(),
                    preview.fuenteTecnicaPreview(),
                    preview.lookupKeyPreview(),
                    preview.motivosNoTarifable()
            );
        }
    }
}