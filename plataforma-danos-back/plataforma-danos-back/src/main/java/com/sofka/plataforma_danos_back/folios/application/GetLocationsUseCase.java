package com.sofka.plataforma_danos_back.folios.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sofka.plataforma_danos_back.folios.application.dto.LocationsResponse;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.port.CotizacionRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.UbicacionCotizacionRepository;

@Service
public class GetLocationsUseCase {

    private final CotizacionRepository cotizacionRepository;
    private final UbicacionCotizacionRepository ubicacionCotizacionRepository;

    public GetLocationsUseCase(CotizacionRepository cotizacionRepository, UbicacionCotizacionRepository ubicacionCotizacionRepository) {
        this.cotizacionRepository = cotizacionRepository;
        this.ubicacionCotizacionRepository = ubicacionCotizacionRepository;
    }

    @Transactional(readOnly = true)
    public LocationsResponse handle(String numeroFolio) {
        Cotizacion cotizacion = cotizacionRepository.findByNumeroFolio(numeroFolio)
                .orElseThrow(() -> new QuoteNotFoundException(numeroFolio));

        return LocationsResponse.from(cotizacion, ubicacionCotizacionRepository.findAllByCotizacionId(cotizacion.id()));
    }
}