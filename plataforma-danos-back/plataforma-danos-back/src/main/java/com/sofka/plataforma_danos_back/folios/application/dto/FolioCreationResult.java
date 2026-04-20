package com.sofka.plataforma_danos_back.folios.application.dto;

public record FolioCreationResult(
        CreateFolioResponse response,
        boolean created
) {
}
