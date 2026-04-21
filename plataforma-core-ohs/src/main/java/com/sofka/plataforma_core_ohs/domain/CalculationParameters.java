package com.sofka.plataforma_core_ohs.domain;

import java.time.OffsetDateTime;

public record CalculationParameters(String codigo, boolean activo, String descripcion, String version, OffsetDateTime fechaCorte) {
}