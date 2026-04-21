package com.sofka.plataforma_danos_back.folios.entrypoints.controller;

import com.sofka.plataforma_danos_back.common.http.ApiResponse;
import com.sofka.plataforma_danos_back.folios.application.CreateFolioUseCase;
import com.sofka.plataforma_danos_back.folios.application.GetQuoteStateUseCase;
import com.sofka.plataforma_danos_back.folios.application.dto.CreateFolioRequest;
import com.sofka.plataforma_danos_back.folios.application.dto.CreateFolioResponse;
import com.sofka.plataforma_danos_back.folios.application.dto.FolioCreationResult;
import com.sofka.plataforma_danos_back.folios.application.dto.QuoteStateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@CrossOrigin(
    origins = "http://localhost:5173",
    allowedHeaders = "*",
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS}
)
@Tag(name = "Folios", description = "Contrato para crear folios y consultar el estado base de una cotizacion")
public class FolioController {

    private final CreateFolioUseCase createFolioUseCase;
    private final GetQuoteStateUseCase getQuoteStateUseCase;

    public FolioController(CreateFolioUseCase createFolioUseCase, GetQuoteStateUseCase getQuoteStateUseCase) {
        this.createFolioUseCase = createFolioUseCase;
        this.getQuoteStateUseCase = getQuoteStateUseCase;
    }

    @PostMapping("/folios")
        @Operation(
            summary = "Crear folio inicial",
            description = "Crea una cotizacion nueva en estado BORRADOR con control de idempotencia por Idempotency-Key"
        )
        @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Folio creado por primera vez"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reutilizacion idempotente de la respuesta previa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Idempotency-Key ausente, vacia o solicitud invalida"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "La llave de idempotencia ya fue usada con otro payload")
        })
    public ResponseEntity<ApiResponse<CreateFolioResponse>> createFolio(
            @Parameter(description = "Llave obligatoria para garantizar idempotencia de la creacion", required = true, example = "9f2b5f7e-2b2d-4c75-9a90-6df6f5a6c321")
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody(required = false) CreateFolioRequest request
    ) {
        FolioCreationResult result = createFolioUseCase.handle(request, idempotencyKey);
        return ResponseEntity.status(result.created() ? 201 : 200).body(ApiResponse.of(result.response()));
    }

    @GetMapping("/quotes/{folio}/state")
        @Operation(
            summary = "Consultar estado base del folio",
            description = "Retorna el estado actual de la cotizacion para continuar la captura desde la ultima informacion persistida"
        )
        @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Estado actual del folio"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No existe una cotizacion con el numeroFolio solicitado")
        })
    public ResponseEntity<ApiResponse<QuoteStateResponse>> getQuoteState(@PathVariable("folio") String folio) {
        QuoteStateResponse response = getQuoteStateUseCase.handle(folio);
        return ResponseEntity.ok(ApiResponse.of(response));
    }
}
