package com.sofka.plataforma_danos_back.folios.entrypoints.controller;

import com.sofka.plataforma_danos_back.common.http.ApiResponse;
import com.sofka.plataforma_danos_back.folios.application.GetGeneralInfoUseCase;
import com.sofka.plataforma_danos_back.folios.application.UpdateGeneralInfoUseCase;
import com.sofka.plataforma_danos_back.folios.application.dto.GeneralInfoRequest;
import com.sofka.plataforma_danos_back.folios.application.dto.GeneralInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/quotes")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.PUT, RequestMethod.OPTIONS}
)
@Tag(name = "Datos Generales", description = "Consulta y actualizacion de la seccion general de una cotizacion")
public class GeneralInfoController {

    private final GetGeneralInfoUseCase getGeneralInfoUseCase;
    private final UpdateGeneralInfoUseCase updateGeneralInfoUseCase;

    public GeneralInfoController(
            GetGeneralInfoUseCase getGeneralInfoUseCase,
            UpdateGeneralInfoUseCase updateGeneralInfoUseCase
    ) {
        this.getGeneralInfoUseCase = getGeneralInfoUseCase;
        this.updateGeneralInfoUseCase = updateGeneralInfoUseCase;
    }

    @GetMapping("/{folio}/general-info")
    @Operation(
            summary = "Consultar datos generales",
            description = "Retorna la seccion general persistida o una estructura vacia para una cotizacion existente"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Datos generales consultados"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No existe una cotizacion con el numeroFolio solicitado")
    })
    public ResponseEntity<ApiResponse<GeneralInfoResponse>> getGeneralInfo(@PathVariable("folio") String folio) {
        return ResponseEntity.ok(ApiResponse.of(getGeneralInfoUseCase.handle(folio)));
    }

    @PutMapping("/{folio}/general-info")
    @Operation(
            summary = "Guardar datos generales",
            description = "Crea o actualiza la seccion general sin sobrescribir el resto del agregado"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Datos generales guardados"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Solicitud invalida"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No existe una cotizacion con el numeroFolio solicitado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Version desactualizada o conflicto de concurrencia"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Referencia de catalogo invalida")
    })
    public ResponseEntity<ApiResponse<GeneralInfoResponse>> updateGeneralInfo(
            @PathVariable("folio") String folio,
            @Valid @RequestBody GeneralInfoRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.of(updateGeneralInfoUseCase.handle(folio, request)));
    }
}