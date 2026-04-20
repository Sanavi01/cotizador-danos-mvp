package com.sofka.plataforma_danos_back.folios.application.dto;

import jakarta.validation.constraints.Size;

public record CreateFolioRequest(
        @Size(max = 64)
        String origin
) {
    public static CreateFolioRequest defaultRequest() {
        return new CreateFolioRequest("spa");
    }
}
