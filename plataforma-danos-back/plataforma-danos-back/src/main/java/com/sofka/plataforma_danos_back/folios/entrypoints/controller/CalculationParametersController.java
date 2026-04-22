package com.sofka.plataforma_danos_back.folios.entrypoints.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sofka.plataforma_danos_back.common.http.ApiResponse;
import com.sofka.plataforma_danos_back.folios.application.dto.CalculationParametersResponse;
import com.sofka.plataforma_danos_back.folios.infrastructure.reference.ReferenceCalculationCatalog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/v1")
@Tag(name = "Parametros de Calculo", description = "Consulta la configuracion activa de calculo del MVP")
public class CalculationParametersController {

    private final ReferenceCalculationCatalog referenceCalculationCatalog;

    public CalculationParametersController(ReferenceCalculationCatalog referenceCalculationCatalog) {
        this.referenceCalculationCatalog = referenceCalculationCatalog;
    }

    @GetMapping("/calculation-parameters/active")
    @Operation(summary = "Obtener parametros activos de calculo")
    public ResponseEntity<ApiResponse<CalculationParametersResponse>> getActiveCalculationParameters() {
        return ResponseEntity.ok(ApiResponse.of(CalculationParametersResponse.from(referenceCalculationCatalog.activeParameters())));
    }
}