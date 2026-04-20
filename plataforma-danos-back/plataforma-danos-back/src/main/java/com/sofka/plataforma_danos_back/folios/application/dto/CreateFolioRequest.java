package com.sofka.plataforma_danos_back.folios.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record CreateFolioRequest(
    @Schema(description = "Origen de la solicitud de creacion", example = "spa", defaultValue = "spa")
        @Size(max = 64)
        String origin
) {
    public static CreateFolioRequest defaultRequest() {
        return new CreateFolioRequest("spa");
    }
}
