package com.sofka.plataforma_danos_back.folios.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sofka.plataforma_danos_back.folios.application.exception.InvalidLocationsPayloadException;
import com.sofka.plataforma_danos_back.folios.domain.AlertaBloqueante;
import com.sofka.plataforma_danos_back.folios.domain.EstadoValidacion;
import com.sofka.plataforma_danos_back.folios.domain.Giro;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionDetalle;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionEvaluacion;
import com.sofka.plataforma_danos_back.folios.domain.ZonaCatastrofica;

@Service
public class LocationEvaluationService {

    public UbicacionEvaluacion evaluate(UbicacionDetalle detalle) {
        if (detalle == null) {
            throw new InvalidLocationsPayloadException("La ubicacion es obligatoria");
        }

        if (!detalle.hasAnyData()) {
            return UbicacionEvaluacion.empty(detalle);
        }

        if (!detalle.hasMinimalDraftData()) {
            return UbicacionEvaluacion.withAlerts(normalizeDetail(detalle, null, null), EstadoValidacion.INCOMPLETE, List.of());
        }

        String codigoPostal = normalize(detalle.codigoPostal());
        PostalProfile profile = resolveProfile(codigoPostal);

        if (!isValidPostalCode(codigoPostal)) {
            List<AlertaBloqueante> alertas = List.of(new AlertaBloqueante(
                    "UBICACION_SIN_ZIP",
                    "La ubicacion no tiene codigo postal valido.",
                    "Warning"
            ));
            return UbicacionEvaluacion.withAlerts(normalizeInvalidDetail(detalle), EstadoValidacion.INVALID, alertas);
        }

        if (isPartialGiro(detalle.giro())) {
            return UbicacionEvaluacion.withAlerts(
                    normalizeDetail(detalle, profile, profile == null ? detalle.zonaCatastrofica() : profile.zonaCatastrofica()),
                    EstadoValidacion.INCOMPLETE,
                    List.of()
            );
        }

        ZonaCatastrofica zonaCatastrofica = detalle.zonaCatastrofica() != null
                ? detalle.zonaCatastrofica()
                : profile == null ? null : profile.zonaCatastrofica();
        UbicacionDetalle normalizedDetalle = normalizeDetail(detalle, profile, zonaCatastrofica);

        boolean technicalComplete = hasText(normalizedDetalle.tipoConstructivo())
                && normalizedDetalle.nivel() != null
                && normalizedDetalle.anioConstruccion() != null;
        boolean giroReady = hasCompleteGiro(normalizedDetalle.giro());

        if (!technicalComplete) {
            return UbicacionEvaluacion.withAlerts(normalizedDetalle, EstadoValidacion.INCOMPLETE, List.of());
        }

        EstadoValidacion estadoValidacion = technicalComplete && giroReady && zonaCatastrofica != null
                ? EstadoValidacion.CALCULABLE
                : EstadoValidacion.VALID;

        return UbicacionEvaluacion.withAlerts(normalizedDetalle, estadoValidacion, List.of());
    }

    public void validatePatchPayload(boolean hasAnyChange) {
        if (!hasAnyChange) {
            throw new InvalidLocationsPayloadException("La solicitud no contiene cambios para la ubicacion");
        }
    }

    private UbicacionDetalle normalizeDetail(UbicacionDetalle detalle, PostalProfile profile, ZonaCatastrofica zonaCatastrofica) {
        return new UbicacionDetalle(
                detalle.indice(),
                normalize(detalle.nombreUbicacion()),
                normalize(detalle.direccion()),
                normalize(detalle.codigoPostal()),
                profile != null ? profile.estado() : normalize(detalle.estado()),
                profile != null ? profile.municipio() : normalize(detalle.municipio()),
                profile != null ? profile.colonia() : normalize(detalle.colonia()),
                profile != null ? profile.ciudad() : normalize(detalle.ciudad()),
                normalize(detalle.tipoConstructivo()),
                detalle.nivel(),
                detalle.anioConstruccion(),
                normalizeGiro(detalle.giro()),
                zonaCatastrofica
        );
    }

    private UbicacionDetalle normalizeInvalidDetail(UbicacionDetalle detalle) {
        return new UbicacionDetalle(
                detalle.indice(),
                normalize(detalle.nombreUbicacion()),
                normalize(detalle.direccion()),
                normalize(detalle.codigoPostal()),
                null,
                null,
                null,
                null,
                normalize(detalle.tipoConstructivo()),
                detalle.nivel(),
                detalle.anioConstruccion(),
                normalizeGiro(detalle.giro()),
                null
        );
    }

    private PostalProfile resolveProfile(String codigoPostal) {
        if ("110111".equals(codigoPostal) || codigoPostal != null && codigoPostal.startsWith("11")) {
            return new PostalProfile(
                    "Bogota D.C.",
                    "Bogota",
                    "Chapinero",
                    "Bogota",
                    new ZonaCatastrofica("Z-TEV-01", "Z-FHM-01")
            );
        }
        if ("050001".equals(codigoPostal) || codigoPostal != null && codigoPostal.startsWith("05")) {
            return new PostalProfile(
                    "Antioquia",
                    "Medellin",
                    null,
                    "Medellin",
                    new ZonaCatastrofica("Z-TEV-02", "Z-FHM-01")
            );
        }
        return null;
    }

    private boolean isValidPostalCode(String codigoPostal) {
        return codigoPostal != null && codigoPostal.matches("^(?!000000)\\d{6}$");
    }

    private boolean isPartialGiro(Giro giro) {
        if (giro == null) {
            return false;
        }
        boolean hasAnyField = hasText(giro.codigo()) || hasText(giro.nombre()) || hasText(giro.claveIncendio());
        boolean hasAllFields = hasText(giro.codigo()) && hasText(giro.nombre()) && hasText(giro.claveIncendio());
        return hasAnyField && !hasAllFields;
    }

    private boolean hasCompleteGiro(Giro giro) {
        return giro != null
                && hasText(giro.codigo())
                && hasText(giro.nombre())
                && hasText(giro.claveIncendio());
    }

    private Giro normalizeGiro(Giro giro) {
        if (giro == null) {
            return null;
        }
        return new Giro(normalize(giro.codigo()), normalize(giro.nombre()), normalize(giro.claveIncendio()));
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record PostalProfile(
            String estado,
            String municipio,
            String colonia,
            String ciudad,
            ZonaCatastrofica zonaCatastrofica
    ) {
    }
}