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
import com.sofka.plataforma_danos_back.folios.application.GetLocationsLayoutUseCase;
import com.sofka.plataforma_danos_back.folios.application.UpdateLocationsLayoutUseCase;
import com.sofka.plataforma_danos_back.folios.application.dto.LocationsLayoutResponse;
import com.sofka.plataforma_danos_back.folios.application.dto.UpdateLocationsLayoutRequest;

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
@Tag(name = "Layout de Ubicaciones", description = "Consulta y actualizacion de la configuracionLayout de una cotizacion")
public class LocationsLayoutController {

    private final GetLocationsLayoutUseCase getLocationsLayoutUseCase;
    private final UpdateLocationsLayoutUseCase updateLocationsLayoutUseCase;

    public LocationsLayoutController(
            GetLocationsLayoutUseCase getLocationsLayoutUseCase,
            UpdateLocationsLayoutUseCase updateLocationsLayoutUseCase
    ) {
        this.getLocationsLayoutUseCase = getLocationsLayoutUseCase;
        this.updateLocationsLayoutUseCase = updateLocationsLayoutUseCase;
    }

    @GetMapping("/{folio}/locations/layout")
    @Operation(
            summary = "Consultar layout de ubicaciones",
            description = "Retorna la configuracionLayout persistida o una estructura vacia para una cotizacion existente"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Layout consultado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No existe una cotizacion con el numeroFolio solicitado")
    })
    public ResponseEntity<ApiResponse<LocationsLayoutResponse>> getLocationsLayout(@PathVariable("folio") String folio) {
        return ResponseEntity.ok(ApiResponse.of(getLocationsLayoutUseCase.handle(folio)));
    }

    @PutMapping("/{folio}/locations/layout")
    @Operation(
            summary = "Guardar layout de ubicaciones",
            description = "Crea o actualiza la configuracionLayout sin modificar el resto del agregado"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Layout guardado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Solicitud invalida"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No existe una cotizacion con el numeroFolio solicitado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Version desactualizada o conflicto de concurrencia"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Regla de negocio invalida para el layout")
    })
    public ResponseEntity<ApiResponse<LocationsLayoutResponse>> updateLocationsLayout(
            @Parameter(description = "Numero de folio de la cotizacion", required = true, example = "1000001")
            @PathVariable("folio") String folio,
            @Valid @RequestBody UpdateLocationsLayoutRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.of(updateLocationsLayoutUseCase.handle(folio, request)));
    }
}