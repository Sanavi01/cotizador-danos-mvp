package com.sofka.plataforma_danos_back.folios.application;

import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sofka.plataforma_danos_back.folios.application.dto.LocationsResponse;
import com.sofka.plataforma_danos_back.folios.application.dto.UpdateLocationsRequest;
import com.sofka.plataforma_danos_back.folios.application.exception.InvalidLocationsPayloadException;
import com.sofka.plataforma_danos_back.folios.application.exception.LocationsLayoutValidationException;
import com.sofka.plataforma_danos_back.folios.application.exception.LocationsLayoutVersionConflictException;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.ConfiguracionLayout;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.EstadoCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.LayoutUbicacionSlot;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionEvaluacion;
import com.sofka.plataforma_danos_back.folios.domain.port.ConfiguracionLayoutRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.CotizacionRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.UbicacionCotizacionRepository;

@Service
public class UpdateLocationsUseCase {

    private static final int MAX_LOCATIONS = 100;

    private final CotizacionRepository cotizacionRepository;
    private final ConfiguracionLayoutRepository configuracionLayoutRepository;
    private final UbicacionCotizacionRepository ubicacionCotizacionRepository;
    private final LocationEvaluationService locationEvaluationService;
    private final Clock clock;

    @Autowired
    public UpdateLocationsUseCase(
            CotizacionRepository cotizacionRepository,
            ConfiguracionLayoutRepository configuracionLayoutRepository,
            UbicacionCotizacionRepository ubicacionCotizacionRepository,
            LocationEvaluationService locationEvaluationService
    ) {
        this(cotizacionRepository, configuracionLayoutRepository, ubicacionCotizacionRepository, locationEvaluationService, Clock.systemUTC());
    }

    public UpdateLocationsUseCase(
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
    public LocationsResponse handle(String numeroFolio, UpdateLocationsRequest request) {
        Cotizacion cotizacion = cotizacionRepository.findByNumeroFolio(numeroFolio)
                .orElseThrow(() -> new QuoteNotFoundException(numeroFolio));

        validateVersion(numeroFolio, request.version(), cotizacion.version());
        ConfiguracionLayout configuracionLayout = configuracionLayoutRepository.findByCotizacionId(cotizacion.id())
                .orElseThrow(() -> new LocationsLayoutValidationException("La cotizacion no tiene configuracionLayout registrado"));

        validateRequest(request.ubicaciones(), configuracionLayout);

        Map<Integer, UbicacionCotizacion> existingByIndex = ubicacionCotizacionRepository.findAllByCotizacionId(cotizacion.id()).stream()
                .collect(Collectors.toMap(ubicacion -> ubicacion.detalle().indice(), Function.identity(), (left, right) -> left));

        Instant now = Instant.now(clock);
        List<UbicacionCotizacion> ubicacionesPersistidas = request.ubicaciones().stream()
                .map(location -> persistLocation(cotizacion.id(), existingByIndex.get(location.indice()), location, now))
                .toList();

        Set<Integer> requestedIndices = request.ubicaciones().stream()
                .map(UpdateLocationsRequest.LocationUpsertRequest::indice)
                .collect(Collectors.toCollection(HashSet::new));
        ubicacionCotizacionRepository.deleteByCotizacionIdAndIndiceNotIn(cotizacion.id(), requestedIndices);
        List<UbicacionCotizacion> savedLocations = ubicacionCotizacionRepository.saveAll(ubicacionesPersistidas);

        Cotizacion cotizacionActualizada = cotizacionRepository.save(
                cotizacion.withEstado(resolverEstado(cotizacion)).withPersistence(cotizacion.id(), cotizacion.version(), now)
        );
        return LocationsResponse.from(cotizacionActualizada, savedLocations);
    }

    private UbicacionCotizacion persistLocation(Long cotizacionId, UbicacionCotizacion existing, UpdateLocationsRequest.LocationUpsertRequest request, Instant now) {
        UbicacionEvaluacion evaluacion = locationEvaluationService.evaluate(request.toDomain());
        if (existing == null) {
            return UbicacionCotizacion.nueva(cotizacionId, evaluacion, now);
        }
        return new UbicacionCotizacion(
                existing.id(),
                cotizacionId,
                evaluacion.detalle(),
                evaluacion.estadoValidacion(),
                evaluacion.alertasBloqueantes(),
                existing.createdAt(),
                now
        );
    }

    private EstadoCotizacion resolverEstado(Cotizacion cotizacion) {
        if (cotizacion.estadoCotizacion() == EstadoCotizacion.BORRADOR) {
            return EstadoCotizacion.EN_CAPTURA;
        }
        return cotizacion.estadoCotizacion();
    }

    private void validateVersion(String numeroFolio, Long requestVersion, Long currentVersion) {
        if (!Objects.equals(currentVersion, requestVersion)) {
            throw new LocationsLayoutVersionConflictException(numeroFolio, requestVersion, currentVersion);
        }
    }

    private void validateRequest(List<UpdateLocationsRequest.LocationUpsertRequest> ubicaciones, ConfiguracionLayout configuracionLayout) {
        if (ubicaciones.size() > MAX_LOCATIONS) {
            throw new InvalidLocationsPayloadException("La coleccion no puede superar 100 ubicaciones");
        }

        Set<Integer> validIndices = configuracionLayout.ubicaciones().stream()
                .map(LayoutUbicacionSlot::indice)
                .collect(Collectors.toSet());
        Set<Integer> indices = new HashSet<>();
        for (UpdateLocationsRequest.LocationUpsertRequest ubicacion : ubicaciones) {
            if (!indices.add(ubicacion.indice())) {
                throw new InvalidLocationsPayloadException("No se permiten indices duplicados en la solicitud");
            }
            if (!validIndices.contains(ubicacion.indice())) {
                throw new LocationsLayoutValidationException("El indice de una ubicacion excede el rango permitido por el layout");
            }
        }
    }
}