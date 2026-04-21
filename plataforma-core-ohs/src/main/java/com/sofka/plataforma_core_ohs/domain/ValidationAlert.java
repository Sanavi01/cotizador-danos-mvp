package com.sofka.plataforma_core_ohs.domain;

public record ValidationAlert(String codigo, String mensaje, AlertSeverity severidad) {
}