package com.sofka.plataforma_danos_back.folios.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sofka.plataforma_danos_back.folios.application.dto.LocationsLayoutResponse;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.port.ConfiguracionLayoutRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.CotizacionRepository;

@Service
public class GetLocationsLayoutUseCase {

    private final CotizacionRepository cotizacionRepository;
    private final ConfiguracionLayoutRepository configuracionLayoutRepository;

    public GetLocationsLayoutUseCase(
            CotizacionRepository cotizacionRepository,
            ConfiguracionLayoutRepository configuracionLayoutRepository
    ) {
        this.cotizacionRepository = cotizacionRepository;
        this.configuracionLayoutRepository = configuracionLayoutRepository;
    }

    @Transactional(readOnly = true)
    public LocationsLayoutResponse handle(String numeroFolio) {
        return cotizacionRepository.findByNumeroFolio(numeroFolio)
                .map(cotizacion -> configuracionLayoutRepository.findByCotizacionId(cotizacion.id())
                        .map(configuracionLayout -> LocationsLayoutResponse.from(cotizacion, configuracionLayout))
                        .orElseGet(() -> LocationsLayoutResponse.empty(cotizacion)))
                .orElseThrow(() -> new QuoteNotFoundException(numeroFolio));
    }
}