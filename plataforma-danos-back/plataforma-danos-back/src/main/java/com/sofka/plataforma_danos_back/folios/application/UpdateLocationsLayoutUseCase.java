package com.sofka.plataforma_danos_back.folios.application;

import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sofka.plataforma_danos_back.folios.application.dto.LocationsLayoutResponse;
import com.sofka.plataforma_danos_back.folios.application.dto.UpdateLocationsLayoutRequest;
import com.sofka.plataforma_danos_back.folios.application.exception.LocationsLayoutValidationException;
import com.sofka.plataforma_danos_back.folios.application.exception.LocationsLayoutVersionConflictException;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.ConfiguracionLayout;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.EstadoCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.LayoutUbicacionSlot;
import com.sofka.plataforma_danos_back.folios.domain.ModoCaptura;
import com.sofka.plataforma_danos_back.folios.domain.port.ConfiguracionLayoutRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.CotizacionRepository;

@Service
public class UpdateLocationsLayoutUseCase {

    private final CotizacionRepository cotizacionRepository;
    private final ConfiguracionLayoutRepository configuracionLayoutRepository;
    private final Clock clock;

    @Autowired
    public UpdateLocationsLayoutUseCase(
            CotizacionRepository cotizacionRepository,
            ConfiguracionLayoutRepository configuracionLayoutRepository
    ) {
        this(cotizacionRepository, configuracionLayoutRepository, Clock.systemUTC());
    }

    public UpdateLocationsLayoutUseCase(
            CotizacionRepository cotizacionRepository,
            ConfiguracionLayoutRepository configuracionLayoutRepository,
            Clock clock
    ) {
        this.cotizacionRepository = cotizacionRepository;
        this.configuracionLayoutRepository = configuracionLayoutRepository;
        this.clock = clock;
    }

    @Transactional
    public LocationsLayoutResponse handle(String numeroFolio, UpdateLocationsLayoutRequest request) {
        Cotizacion cotizacion = cotizacionRepository.findByNumeroFolio(numeroFolio)
                .orElseThrow(() -> new QuoteNotFoundException(numeroFolio));

        validateVersion(numeroFolio, request.version(), cotizacion.version());
        validateLayout(
                request.configuracionLayout().modoCaptura(),
                request.configuracionLayout().cantidadUbicaciones(),
                request.configuracionLayout().ubicaciones()
        );

        Instant now = Instant.now(clock);
        ConfiguracionLayout configuracionLayout = configuracionLayoutRepository.save(
                ConfiguracionLayout.nueva(
                        cotizacion.id(),
                        request.configuracionLayout().modoCaptura(),
                        request.configuracionLayout().cantidadUbicaciones(),
                        request.configuracionLayout().ubicaciones().stream()
                                .sorted((left, right) -> Integer.compare(left.ordenCaptura(), right.ordenCaptura()))
                                .map(slot -> new LayoutUbicacionSlot(slot.indice(), slot.ordenCaptura()))
                                .toList(),
                        now
                )
        );

        Cotizacion cotizacionActualizada = cotizacionRepository.save(
                cotizacion.withEstado(resolverEstado(cotizacion)).withPersistence(cotizacion.id(), cotizacion.version(), now)
        );
        return LocationsLayoutResponse.from(cotizacionActualizada, configuracionLayout);
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

    private void validateLayout(
            ModoCaptura modoCaptura,
            Integer cantidadUbicaciones,
            List<UpdateLocationsLayoutRequest.LayoutUbicacionSlotRequest> ubicaciones
    ) {
        if (modoCaptura == ModoCaptura.UNICA && !Integer.valueOf(1).equals(cantidadUbicaciones)) {
            throw new LocationsLayoutValidationException("El modo de captura UNICA requiere exactamente una ubicacion");
        }
        if (modoCaptura == ModoCaptura.MULTIPLE && cantidadUbicaciones <= 1) {
            throw new LocationsLayoutValidationException("El modo de captura MULTIPLE requiere mas de una ubicacion");
        }
        if (ubicaciones.size() != cantidadUbicaciones) {
            throw new LocationsLayoutValidationException("La cantidad de ubicaciones no coincide con la lista de slots enviada");
        }

        HashSet<Integer> indices = new HashSet<>();
        HashSet<Integer> ordenes = new HashSet<>();
        for (UpdateLocationsLayoutRequest.LayoutUbicacionSlotRequest slot : ubicaciones) {
            if (slot.indice() > cantidadUbicaciones) {
                throw new LocationsLayoutValidationException("El indice de una ubicacion excede el rango permitido por el layout");
            }
            if (slot.ordenCaptura() > cantidadUbicaciones) {
                throw new LocationsLayoutValidationException("El orden de captura de una ubicacion excede el rango permitido por el layout");
            }
            if (!indices.add(slot.indice())) {
                throw new LocationsLayoutValidationException("No se permiten indices duplicados en el layout");
            }
            if (!ordenes.add(slot.ordenCaptura())) {
                throw new LocationsLayoutValidationException("No se permiten ordenes de captura duplicadas en el layout");
            }
        }
    }
}