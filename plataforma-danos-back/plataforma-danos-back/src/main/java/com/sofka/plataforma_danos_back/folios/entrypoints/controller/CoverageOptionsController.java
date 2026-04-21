package com.sofka.plataforma_danos_back.folios.entrypoints.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sofka.plataforma_danos_back.common.http.ApiResponse;
import com.sofka.plataforma_danos_back.folios.application.GetCoverageOptionsUseCase;
import com.sofka.plataforma_danos_back.folios.application.UpdateCoverageOptionsUseCase;
import com.sofka.plataforma_danos_back.folios.application.dto.CoverageOptionsRequest;
import com.sofka.plataforma_danos_back.folios.application.dto.CoverageOptionsResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/quotes")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.PUT, RequestMethod.OPTIONS}
)
@Tag(name = "Opciones de Cobertura", description = "Consulta y actualizacion de las opcionesCobertura de una cotizacion")
public class CoverageOptionsController {

    private final GetCoverageOptionsUseCase getCoverageOptionsUseCase;
    private final UpdateCoverageOptionsUseCase updateCoverageOptionsUseCase;

    public CoverageOptionsController(
            GetCoverageOptionsUseCase getCoverageOptionsUseCase,
            UpdateCoverageOptionsUseCase updateCoverageOptionsUseCase
    ) {
        this.getCoverageOptionsUseCase = getCoverageOptionsUseCase;
        this.updateCoverageOptionsUseCase = updateCoverageOptionsUseCase;
    }

    @GetMapping("/{folio}/coverage-options")
    @Operation(
            summary = "Consultar opciones de cobertura",
            description = "Retorna la seccion opcionesCobertura persistida o una estructura vacia para una cotizacion existente"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Opciones de cobertura consultadas"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No existe una cotizacion con el numeroFolio solicitado")
    })
    public ResponseEntity<ApiResponse<CoverageOptionsResponse>> getCoverageOptions(@PathVariable("folio") String folio) {
        return ResponseEntity.ok(ApiResponse.of(getCoverageOptionsUseCase.handle(folio)));
    }

    @PutMapping("/{folio}/coverage-options")
    @Operation(
            summary = "Guardar opciones de cobertura",
            description = "Crea o actualiza la seccion global de coberturas sin sobrescribir el resto del agregado"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Opciones de cobertura guardadas"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Solicitud invalida"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No existe una cotizacion con el numeroFolio solicitado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Version desactualizada o conflicto de concurrencia"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Referencia de catalogo invalida")
    })
    public ResponseEntity<ApiResponse<CoverageOptionsResponse>> updateCoverageOptions(
            @Parameter(description = "Numero de folio de la cotizacion", required = true, example = "1000001")
            @PathVariable("folio") String folio,
            @Valid @RequestBody CoverageOptionsRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.of(updateCoverageOptionsUseCase.handle(folio, request)));
    }
}