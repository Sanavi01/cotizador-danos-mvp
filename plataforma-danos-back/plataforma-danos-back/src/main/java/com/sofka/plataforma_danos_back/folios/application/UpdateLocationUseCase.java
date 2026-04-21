package com.sofka.plataforma_danos_back.folios.application;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sofka.plataforma_danos_back.folios.application.dto.LocationResponse;
import com.sofka.plataforma_danos_back.folios.application.dto.UpdateLocationRequest;
import com.sofka.plataforma_danos_back.folios.application.exception.LocationNotFoundException;
import com.sofka.plataforma_danos_back.folios.application.exception.LocationVersionConflictException;
import com.sofka.plataforma_danos_back.folios.application.exception.LocationsLayoutValidationException;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.ConfiguracionLayout;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.EstadoCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.LayoutUbicacionSlot;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionDetalle;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionEvaluacion;
import com.sofka.plataforma_danos_back.folios.domain.port.ConfiguracionLayoutRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.CotizacionRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.UbicacionCotizacionRepository;

@Service
public class UpdateLocationUseCase {

    private final CotizacionRepository cotizacionRepository;
    private final ConfiguracionLayoutRepository configuracionLayoutRepository;
    private final UbicacionCotizacionRepository ubicacionCotizacionRepository;
    private final LocationEvaluationService locationEvaluationService;
    private final Clock clock;

    @Autowired
    public UpdateLocationUseCase(
            CotizacionRepository cotizacionRepository,
            ConfiguracionLayoutRepository configuracionLayoutRepository,
            UbicacionCotizacionRepository ubicacionCotizacionRepository,
            LocationEvaluationService locationEvaluationService
    ) {
        this(cotizacionRepository, configuracionLayoutRepository, ubicacionCotizacionRepository, locationEvaluationService, Clock.systemUTC());
    }

    public UpdateLocationUseCase(
            CotizacionRepository cotizacionRepository,
            ConfiguracionLayoutRepository configuracionLayoutRepository,
            UbicacionCotizacionRepository ubicacionCotizacionRepository,
            LocationEvaluationService locationEvaluationService,
            Clock clock
    ) {
        this.cotizacionRepository = cotizacionRepository;
        this.configuracionLayoutRepository = configuracionLayoutRepository;
        this.ubicacionCotizacionRepository = ubicacionCotizacionRepository;
        this.locationEvaluationService = locationEvaluationService;
        this.clock = clock;
    }

    @Transactional
    public LocationResponse handle(String numeroFolio, Integer indice, UpdateLocationRequest request) {
        Cotizacion cotizacion = cotizacionRepository.findByNumeroFolio(numeroFolio)
                .orElseThrow(() -> new QuoteNotFoundException(numeroFolio));

        validateVersion(numeroFolio, request.version(), cotizacion.version());
        ConfiguracionLayout configuracionLayout = configuracionLayoutRepository.findByCotizacionId(cotizacion.id())
                .orElseThrow(() -> new LocationsLayoutValidationException("La cotizacion no tiene configuracionLayout registrado"));
        validateIndex(indice, configuracionLayout);

        UbicacionCotizacion existing = ubicacionCotizacionRepository.findByCotizacionIdAndIndice(cotizacion.id(), indice)
                .orElseThrow(() -> new LocationNotFoundException(numeroFolio, indice));

        locationEvaluationService.validatePatchPayload(request.changes().hasAnyChange());
        UbicacionDetalle mergedDetalle = existing.detalle().merge(request.changes().toPatch());
        UbicacionEvaluacion evaluacion = locationEvaluationService.evaluate(mergedDetalle);
        Instant now = Instant.now(clock);
        UbicacionCotizacion persisted = ubicacionCotizacionRepository.save(new UbicacionCotizacion(
                existing.id(),
                cotizacion.id(),
                evaluacion.detalle(),
                evaluacion.estadoValidacion(),
                evaluacion.alertasBloqueantes(),
                existing.createdAt(),
                now
        ));

        Cotizacion cotizacionActualizada = cotizacionRepository.save(
                cotizacion.withEstado(resolverEstado(cotizacion)).withPersistence(cotizacion.id(), cotizacion.version(), now)
        );
        return LocationResponse.from(cotizacionActualizada, persisted);
    }

    private void validateVersion(String numeroFolio, Long requestVersion, Long currentVersion) {
        if (!Objects.equals(currentVersion, requestVersion)) {
            throw new LocationVersionConflictException(numeroFolio, requestVersion, currentVersion);
        }
    }

    private void validateIndex(Integer indice, ConfiguracionLayout configuracionLayout) {
        Set<Integer> validIndices = configuracionLayout.ubicaciones().stream()
                .map(LayoutUbicacionSlot::indice)
                .collect(Collectors.toSet());
        if (!validIndices.contains(indice)) {
            throw new LocationsLayoutValidationException("El indice de una ubicacion excede el rango permitido por el layout");
        }
    }

    private EstadoCotizacion resolverEstado(Cotizacion cotizacion) {
        if (cotizacion.estadoCotizacion() == EstadoCotizacion.BORRADOR) {
            return EstadoCotizacion.EN_CAPTURA;
        }
        return cotizacion.estadoCotizacion();
    }
}