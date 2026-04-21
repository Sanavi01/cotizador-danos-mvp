package com.sofka.plataforma_danos_back.folios.domain;

public record Garantia(
        String garantiaCode,
        String origen,
        boolean tariffablePreview
) {
}