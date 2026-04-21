package com.sofka.plataforma_danos_back.folios.domain;

import java.util.List;

public record UbicacionEvaluacion(
        UbicacionDetalle detalle,
        EstadoValidacion estadoValidacion,
        List<AlertaBloqueante> alertasBloqueantes
) {
    public UbicacionEvaluacion {
        alertasBloqueantes = alertasBloqueantes == null ? List.of() : List.copyOf(alertasBloqueantes);
    }

    public static UbicacionEvaluacion empty(UbicacionDetalle detalle) {
        return new UbicacionEvaluacion(detalle, EstadoValidacion.EMPTY, List.of());
    }

    public static UbicacionEvaluacion withAlerts(UbicacionDetalle detalle, EstadoValidacion estadoValidacion, List<AlertaBloqueante> alertasBloqueantes) {
        return new UbicacionEvaluacion(detalle, estadoValidacion, alertasBloqueantes);
    }
}