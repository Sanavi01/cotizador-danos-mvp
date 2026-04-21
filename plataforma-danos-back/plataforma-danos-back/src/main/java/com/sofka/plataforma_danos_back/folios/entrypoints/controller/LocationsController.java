package com.sofka.plataforma_danos_back.folios.entrypoints.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sofka.plataforma_danos_back.common.http.ApiResponse;
import com.sofka.plataforma_danos_back.folios.application.GetLocationsSummaryUseCase;
import com.sofka.plataforma_danos_back.folios.application.GetLocationsUseCase;
import com.sofka.plataforma_danos_back.folios.application.UpdateLocationUseCase;
import com.sofka.plataforma_danos_back.folios.application.UpdateLocationsUseCase;
import com.sofka.plataforma_danos_back.folios.application.dto.LocationResponse;
import com.sofka.plataforma_danos_back.folios.application.dto.LocationsResponse;
import com.sofka.plataforma_danos_back.folios.application.dto.LocationsSummaryResponse;
import com.sofka.plataforma_danos_back.folios.application.dto.UpdateLocationRequest;
import com.sofka.plataforma_danos_back.folios.application.dto.UpdateLocationsRequest;

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
        methods = {RequestMethod.GET, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.OPTIONS}
)
@Tag(name = "Ubicaciones", description = "Consulta, actualizacion y resumen de ubicaciones de una cotizacion")
public class LocationsController {

    private final GetLocationsUseCase getLocationsUseCase;
    private final UpdateLocationsUseCase updateLocationsUseCase;
    private final UpdateLocationUseCase updateLocationUseCase;
    private final GetLocationsSummaryUseCase getLocationsSummaryUseCase;

    public LocationsController(
            GetLocationsUseCase getLocationsUseCase,
            UpdateLocationsUseCase updateLocationsUseCase,
            UpdateLocationUseCase updateLocationUseCase,
            GetLocationsSummaryUseCase getLocationsSummaryUseCase
    ) {
        this.getLocationsUseCase = getLocationsUseCase;
        this.updateLocationsUseCase = updateLocationsUseCase;
        this.updateLocationUseCase = updateLocationUseCase;
        this.getLocationsSummaryUseCase = getLocationsSummaryUseCase;
    }

    @GetMapping("/{folio}/locations")
    @Operation(summary = "Consultar ubicaciones", description = "Retorna la lista actual de ubicaciones del folio")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ubicaciones consultadas"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No existe una cotizacion con el numeroFolio solicitado")
    })
    public ResponseEntity<ApiResponse<LocationsResponse>> getLocations(@PathVariable("folio") String folio) {
        return ResponseEntity.ok(ApiResponse.of(getLocationsUseCase.handle(folio)));
    }

    @PutMapping("/{folio}/locations")
    @Operation(summary = "Guardar ubicaciones", description = "Reemplaza la seccion de ubicaciones del folio")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ubicaciones guardadas"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Solicitud invalida"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No existe una cotizacion con el numeroFolio solicitado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Version desactualizada o conflicto de concurrencia"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Regla de negocio invalida para las ubicaciones")
    })
    public ResponseEntity<ApiResponse<LocationsResponse>> updateLocations(
            @Parameter(description = "Numero de folio de la cotizacion", required = true, example = "1000001")
            @PathVariable("folio") String folio,
            @Valid @RequestBody UpdateLocationsRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.of(updateLocationsUseCase.handle(folio, request)));
    }

    @PatchMapping("/{folio}/locations/{indice}")
    @Operation(summary = "Actualizar una ubicacion", description = "Modifica parcialmente la ubicacion solicitada por indice")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ubicacion actualizada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Solicitud invalida"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No existe una cotizacion o ubicacion con los parametros solicitados"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Version desactualizada o conflicto de concurrencia"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Regla de negocio invalida para la ubicacion")
    })
    public ResponseEntity<ApiResponse<LocationResponse>> updateLocation(
            @Parameter(description = "Numero de folio de la cotizacion", required = true, example = "1000001")
            @PathVariable("folio") String folio,
            @Parameter(description = "Indice unico de la ubicacion dentro del folio", required = true, example = "2")
            @PathVariable("indice") Integer indice,
            @Valid @RequestBody UpdateLocationRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.of(updateLocationUseCase.handle(folio, indice, request)));
    }

    @GetMapping("/{folio}/locations/summary")
    @Operation(summary = "Resumen de ubicaciones", description = "Consulta el resumen operativo de ubicaciones del folio")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Resumen consultado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No existe una cotizacion con el numeroFolio solicitado")
    })
    public ResponseEntity<ApiResponse<LocationsSummaryResponse>> getLocationsSummary(@PathVariable("folio") String folio) {
        return ResponseEntity.ok(ApiResponse.of(getLocationsSummaryUseCase.handle(folio)));
    }
}