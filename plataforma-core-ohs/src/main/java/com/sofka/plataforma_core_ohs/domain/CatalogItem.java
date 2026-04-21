package com.sofka.plataforma_core_ohs.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CatalogItem(String codigo, String nombre, boolean activo, String claveIncendio) {
}