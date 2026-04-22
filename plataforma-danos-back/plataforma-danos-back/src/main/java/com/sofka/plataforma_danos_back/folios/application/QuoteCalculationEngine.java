package com.sofka.plataforma_danos_back.folios.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.sofka.plataforma_danos_back.folios.application.exception.QuoteCalculationRejectedException;
import com.sofka.plataforma_danos_back.folios.domain.ActiveCalculationParameters;
import com.sofka.plataforma_danos_back.folios.domain.AlertaBloqueante;
import com.sofka.plataforma_danos_back.folios.domain.ComponenteComercialCalculado;
import com.sofka.plataforma_danos_back.folios.domain.ComponenteTecnicoCalculado;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.EstadoCalculo;
import com.sofka.plataforma_danos_back.folios.domain.GarantiaCalculada;
import com.sofka.plataforma_danos_back.folios.domain.PrimaPorUbicacion;
import com.sofka.plataforma_danos_back.folios.domain.SelectedGuarantee;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionDetalle;
import com.sofka.plataforma_danos_back.folios.domain.ZonaCatastrofica;
import com.sofka.plataforma_danos_back.folios.infrastructure.reference.ReferenceCalculationCatalog;
import com.sofka.plataforma_danos_back.folios.infrastructure.reference.ReferenceCalculationCatalog.LookupRecord;

@Service
public class QuoteCalculationEngine {

    private static final BigDecimal TECHNICAL_BASE = new BigDecimal("1000000");
    private static final String COMPONENT_CORE = "base";
    private static final String COMPONENT_CAT = "factor_zona";
    private static final String COMPONENT_FIRE = "factor_incendio";
    private static final String COMPONENT_ADMIN = "RECARGO_ADMINISTRACION";
    private static final String COMPONENT_MARGIN = "MARGEN_COMERCIAL";
    private static final String ALERT_INVALID_ZIP = "UBICACION_SIN_ZIP";
    private static final String ALERT_INVALID_TECH = "UBICACION_SIN_DATOS_TECNICOS";
    private static final String ALERT_NO_ROUTE = "UBICACION_SIN_RUTA_TECNICA";
    private static final String ALERT_OPTIONAL_ELECTRONIC = "EQUIPO_ELECTRONICO_PENDIENTE";

    private final ReferenceCalculationCatalog catalog;
    private final Clock clock;

    public QuoteCalculationEngine(ReferenceCalculationCatalog catalog) {
        this(catalog, Clock.systemUTC());
    }

    public QuoteCalculationEngine(ReferenceCalculationCatalog catalog, Clock clock) {
        this.catalog = Objects.requireNonNull(catalog, "catalog es obligatorio");
        this.clock = Objects.requireNonNull(clock, "clock es obligatorio");
    }

    public QuoteCalculationResult calculate(
            Cotizacion cotizacion,
            List<UbicacionCotizacion> ubicaciones,
            List<SelectedGuarantee> garantiasSeleccionadas
    ) {
        ActiveCalculationParameters parameters = catalog.activeParameters();
        Instant calculatedAt = Instant.now(clock);
        List<PrimaPorUbicacion> primasPorUbicacion = ubicaciones.stream()
                .sorted(Comparator.comparing(ubicacion -> ubicacion.detalle().indice()))
                .map(ubicacion -> calculateLocation(ubicacion, garantiasSeleccionadas, parameters))
                .toList();

        long ubicacionesCalculadas = primasPorUbicacion.stream().filter(PrimaPorUbicacion::ubicacionCalculable).count();
        if (ubicacionesCalculadas == 0) {
            throw new QuoteCalculationRejectedException("La cotizacion no tiene ubicaciones calculables");
        }

        int ubicacionesNoCalculables = primasPorUbicacion.size() - (int) ubicacionesCalculadas;
        EstadoCalculo estadoCalculo = ubicacionesNoCalculables == 0 ? EstadoCalculo.CALCULADO : EstadoCalculo.PARCIAL;
        BigDecimal primaNeta = sum(primasPorUbicacion.stream()
                .filter(PrimaPorUbicacion::ubicacionCalculable)
                .map(PrimaPorUbicacion::primaNetaUbicacion)
                .toList());
        BigDecimal primaComercial = sum(primasPorUbicacion.stream()
                .filter(PrimaPorUbicacion::ubicacionCalculable)
                .map(PrimaPorUbicacion::primaComercialUbicacion)
                .toList());
        List<AlertaBloqueante> alertasVigentes = deduplicateAlerts(primasPorUbicacion);

        return new QuoteCalculationResult(
                primaNeta,
                primaComercial,
                estadoCalculo,
                parameters.version(),
                calculatedAt,
                primasPorUbicacion,
                alertasVigentes,
                (int) ubicacionesCalculadas,
                ubicacionesNoCalculables
        );
    }

    private PrimaPorUbicacion calculateLocation(
            UbicacionCotizacion ubicacion,
            List<SelectedGuarantee> garantiasSeleccionadas,
            ActiveCalculationParameters parameters
    ) {
        List<AlertaBloqueante> alertas = new ArrayList<>();
        UbicacionDetalle detalle = ubicacion.detalle();
        if (detalle == null) {
            return emptyLocation(null, List.of(new AlertaBloqueante(ALERT_INVALID_TECH, "La ubicacion no contiene detalle tecnico.", "Warning")));
        }

        if (!isValidZip(detalle.codigoPostal())) {
            alertas.add(new AlertaBloqueante(ALERT_INVALID_ZIP, "La ubicacion no tiene codigo postal valido.", "Warning"));
            return emptyLocation(detalle.indice(), alertas);
        }

        if (detalle.giro() == null || !hasText(detalle.giro().codigo()) || !hasText(detalle.giro().claveIncendio())) {
            alertas.add(new AlertaBloqueante(ALERT_INVALID_TECH, "La ubicacion no tiene giro.claveIncendio valido.", "Warning"));
            return emptyLocation(detalle.indice(), alertas);
        }

        ZonaCatastrofica zonaCatastrofica = detalle.zonaCatastrofica();
        if (zonaCatastrofica == null || !hasText(zonaCatastrofica.zonaTev())) {
            alertas.add(new AlertaBloqueante(ALERT_NO_ROUTE, "La ubicacion no tiene zona TEV valida para resolver tarifas.", "Warning"));
            return emptyLocation(detalle.indice(), alertas);
        }

        if (garantiasSeleccionadas == null || garantiasSeleccionadas.isEmpty()) {
            alertas.add(new AlertaBloqueante(ALERT_NO_ROUTE, "La cotizacion no tiene garantias seleccionadas.", "Warning"));
            return emptyLocation(detalle.indice(), alertas);
        }

        List<GarantiaCalculada> garantiasCalculadas = new ArrayList<>();
        for (SelectedGuarantee garantiaSeleccionada : garantiasSeleccionadas) {
            GarantiaCalculada garantiaCalculada = calculateGuarantee(detalle, garantiaSeleccionada, parameters, alertas);
            if (garantiaCalculada != null) {
                garantiasCalculadas.add(garantiaCalculada);
            }
        }

        if (garantiasCalculadas.isEmpty()) {
            alertas.add(new AlertaBloqueante(ALERT_NO_ROUTE, "Ninguna garantia seleccionada pudo resolverse con la informacion persistida.", "Warning"));
            return emptyLocation(detalle.indice(), alertas);
        }

        BigDecimal primaNeta = sum(garantiasCalculadas.stream().map(GarantiaCalculada::primaGarantia).toList());
        BigDecimal recargoAdministracion = round(primaNeta.multiply(parameters.recargoAdministracion()));
        BigDecimal margenComercial = round(primaNeta.multiply(parameters.margenComercial()));
        BigDecimal primaComercial = round(primaNeta.add(recargoAdministracion).add(margenComercial));

        List<ComponenteComercialCalculado> componentesComerciales = List.of(
                new ComponenteComercialCalculado(COMPONENT_ADMIN, parameters.recargoAdministracion(), primaNeta, recargoAdministracion),
                new ComponenteComercialCalculado(COMPONENT_MARGIN, parameters.margenComercial(), primaNeta, margenComercial)
        );

        return new PrimaPorUbicacion(
                detalle.indice(),
                true,
                primaNeta,
                primaComercial,
                garantiasCalculadas,
                componentesComerciales,
                alertas
        );
    }

    private GarantiaCalculada calculateGuarantee(
            UbicacionDetalle detalle,
            SelectedGuarantee garantiaSeleccionada,
            ActiveCalculationParameters parameters,
            List<AlertaBloqueante> alertas
    ) {
        String garantiaCode = garantiaSeleccionada.garantiaCode();
        List<ComponenteTecnicoCalculado> componentes = new ArrayList<>();

        catalog.findCoreTariff(detalle.giro().codigo(), detalle.zonaCatastrofica().zonaTev(), garantiaCode)
                .filter(lookup -> isActive(lookup, parameters.fechaCorte()))
                .ifPresent(lookup -> componentes.add(buildComponent(COMPONENT_CORE, lookup)));

        catalog.findCatTariff(detalle.zonaCatastrofica().zonaTev(), garantiaCode)
                .filter(lookup -> isActive(lookup, parameters.fechaCorte()))
                .ifPresent(lookup -> componentes.add(buildComponent(COMPONENT_CAT, lookup)));

        if (componentes.isEmpty()) {
            catalog.findFireTariff(detalle.giro().codigo(), resolveTipoConstructivo(detalle.tipoConstructivo()), resolveNivelTarifario(detalle.nivel()))
                    .filter(lookup -> isActive(lookup, parameters.fechaCorte()))
                    .ifPresent(lookup -> componentes.add(buildComponent(COMPONENT_FIRE, lookup)));
        }

        if (componentes.isEmpty()) {
            if ("GAR-EL".equals(garantiaCode)) {
                alertas.add(new AlertaBloqueante(ALERT_OPTIONAL_ELECTRONIC, "La garantia de equipo electronico queda pendiente por no exponer clase tecnica en la ubicacion.", "Info"));
            }
            return null;
        }

        BigDecimal primaGarantia = sum(componentes.stream().map(ComponenteTecnicoCalculado::monto).toList());
        return new GarantiaCalculada(garantiaCode, primaGarantia, componentes);
    }

    private ComponenteTecnicoCalculado buildComponent(String tipo, LookupRecord lookupRecord) {
        BigDecimal monto = lookupRecord.rate() == null
                ? round(TECHNICAL_BASE.multiply(lookupRecord.factor()))
                : round(TECHNICAL_BASE.multiply(lookupRecord.rate()).multiply(lookupRecord.factor()));
        return new ComponenteTecnicoCalculado(
                tipo,
                lookupRecord.source(),
                lookupRecord.lookupKey(),
                lookupRecord.rate(),
                lookupRecord.factor(),
                monto
        );
    }

    private PrimaPorUbicacion emptyLocation(Integer indice, List<AlertaBloqueante> alertas) {
        return new PrimaPorUbicacion(
                indice,
                false,
                BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                List.of(),
                List.of(),
                List.copyOf(alertas)
        );
    }

    private List<AlertaBloqueante> deduplicateAlerts(List<PrimaPorUbicacion> primasPorUbicacion) {
        Map<String, AlertaBloqueante> deduplicated = new LinkedHashMap<>();
        primasPorUbicacion.stream()
                .flatMap(primaPorUbicacion -> primaPorUbicacion.alertas().stream())
                .forEach(alerta -> deduplicated.putIfAbsent(alertKey(alerta), alerta));
        return List.copyOf(deduplicated.values());
    }

    private String alertKey(AlertaBloqueante alertaBloqueante) {
        return String.join("|",
                Objects.toString(alertaBloqueante.codigo(), ""),
                Objects.toString(alertaBloqueante.mensaje(), ""),
                Objects.toString(alertaBloqueante.severidad(), "")
        );
    }

    private boolean isValidZip(String codigoPostal) {
        return codigoPostal != null && codigoPostal.matches("^(?!000000)\\d{6}$");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private boolean isActive(LookupRecord lookupRecord, Instant evaluationInstant) {
        return !evaluationInstant.isBefore(lookupRecord.vigenciaDesde()) && !evaluationInstant.isAfter(lookupRecord.vigenciaHasta());
    }

    private String resolveTipoConstructivo(String tipoConstructivo) {
        return tipoConstructivo == null ? null : tipoConstructivo.trim().toUpperCase(Locale.ROOT);
    }

    private String resolveNivelTarifario(Integer nivel) {
        if (nivel == null) {
            return null;
        }
        if (nivel <= 1) {
            return "BAS";
        }
        if (nivel == 2) {
            return "MED";
        }
        return "ALT";
    }

    private BigDecimal sum(List<BigDecimal> values) {
        return values.stream()
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal round(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public record QuoteCalculationResult(
            BigDecimal primaNeta,
            BigDecimal primaComercial,
            EstadoCalculo estadoCalculo,
            String calculationParameterVersion,
            Instant calculatedAt,
            List<PrimaPorUbicacion> primasPorUbicacion,
            List<AlertaBloqueante> alertasVigentes,
            int ubicacionesCalculadas,
            int ubicacionesNoCalculables
    ) {
        public QuoteCalculationResult {
            primasPorUbicacion = primasPorUbicacion == null ? List.of() : List.copyOf(primasPorUbicacion);
            alertasVigentes = alertasVigentes == null ? List.of() : List.copyOf(alertasVigentes);
        }
    }
}