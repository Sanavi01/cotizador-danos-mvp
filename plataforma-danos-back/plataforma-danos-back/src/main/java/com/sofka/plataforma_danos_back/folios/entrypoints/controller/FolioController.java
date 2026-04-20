package com.sofka.plataforma_danos_back.folios.entrypoints.controller;

import com.sofka.plataforma_danos_back.common.http.ApiResponse;
import com.sofka.plataforma_danos_back.folios.application.CreateFolioUseCase;
import com.sofka.plataforma_danos_back.folios.application.GetQuoteStateUseCase;
import com.sofka.plataforma_danos_back.folios.application.dto.CreateFolioRequest;
import com.sofka.plataforma_danos_back.folios.application.dto.CreateFolioResponse;
import com.sofka.plataforma_danos_back.folios.application.dto.FolioCreationResult;
import com.sofka.plataforma_danos_back.folios.application.dto.QuoteStateResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class FolioController {

    private final CreateFolioUseCase createFolioUseCase;
    private final GetQuoteStateUseCase getQuoteStateUseCase;

    public FolioController(CreateFolioUseCase createFolioUseCase, GetQuoteStateUseCase getQuoteStateUseCase) {
        this.createFolioUseCase = createFolioUseCase;
        this.getQuoteStateUseCase = getQuoteStateUseCase;
    }

    @PostMapping("/folios")
    public ResponseEntity<ApiResponse<CreateFolioResponse>> createFolio(
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody(required = false) CreateFolioRequest request
    ) {
        FolioCreationResult result = createFolioUseCase.handle(request, idempotencyKey);
        return ResponseEntity.status(result.created() ? 201 : 200).body(ApiResponse.of(result.response()));
    }

    @GetMapping("/quotes/{folio}/state")
    public ResponseEntity<ApiResponse<QuoteStateResponse>> getQuoteState(@PathVariable("folio") String folio) {
        QuoteStateResponse response = getQuoteStateUseCase.handle(folio);
        return ResponseEntity.ok(ApiResponse.of(response));
    }
}
