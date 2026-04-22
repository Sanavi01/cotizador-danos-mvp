package com.sofka.plataforma_danos_back.folios.application.dto;

import java.util.List;
import java.util.Locale;

import com.sofka.plataforma_danos_back.folios.domain.CoverageOptions;
import com.sofka.plataforma_danos_back.folios.domain.SelectedGuarantee;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CoverageOptionsRequest(
        @Schema(description = "Version actual del agregado para control optimista", example = "2")
        @NotNull(message = "La version es obligatoria")
        Long version,
        @Schema(description = "Configuracion global de opciones de cobertura")
        @Valid
        @NotNull(message = "Las opciones de cobertura son obligatorias")
        CoverageOptionsView opcionesCobertura
) {
    public record CoverageOptionsView(
            @Schema(description = "Garantias seleccionadas para el folio")
            @NotNull(message = "Las garantias seleccionadas son obligatorias")
            @Valid
            List<SelectedGuaranteeRequest> garantiasSeleccionadas,
            @Schema(description = "Observaciones opcionales de la configuracion", example = "Cobertura base para el analisis inicial")
            @Size(max = 500, message = "Las observaciones no pueden superar 500 caracteres")
            String observaciones
    ) {
        public CoverageOptions toDomain(Long cotizacionId) {
            List<SelectedGuarantee> selectedGuarantees = garantiasSeleccionadas == null
                    ? List.of()
                    : garantiasSeleccionadas.stream().map(SelectedGuaranteeRequest::toDomain).toList();
            return new CoverageOptions(cotizacionId, selectedGuarantees, observaciones);
        }
    }

    public record SelectedGuaranteeRequest(
            @Schema(description = "Codigo canonico de la garantia", example = "GAR-INC-ED")
            @NotBlank(message = "El codigo de la garantia es obligatorio")
            @Size(max = 64, message = "El codigo de la garantia no puede superar 64 caracteres")
            String garantiaCode,
            @Schema(description = "Terminos libres asociados a la garantia")
            @NotNull(message = "Los terminos son obligatorios")
            @Size(max = 50, message = "La lista de terminos no puede superar 50 elementos")
            List<@Size(max = 200, message = "Cada termino no puede superar 200 caracteres") String> terminos
    ) {
        public SelectedGuarantee toDomain() {
            return new SelectedGuarantee(normalizeCode(garantiaCode), normalizeTerms(terminos));
        }
    }

    private static String normalizeCode(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private static List<String> normalizeTerms(List<String> terminos) {
        if (terminos == null) {
            return List.of();
        }
        return terminos.stream()
                .map(CoverageOptionsRequest::normalizeTerm)
                .filter(term -> term != null && !term.isBlank())
                .toList();
    }

    private static String normalizeTerm(String value) {
        return value == null ? null : value.trim();
    }
}