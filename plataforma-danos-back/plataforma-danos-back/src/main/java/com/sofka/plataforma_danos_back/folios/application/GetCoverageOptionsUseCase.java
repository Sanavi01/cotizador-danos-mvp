package com.sofka.plataforma_danos_back.folios.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sofka.plataforma_danos_back.folios.application.dto.CoverageOptionsResponse;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.CoverageOptions;
import com.sofka.plataforma_danos_back.folios.domain.CoverageProjectionPerLocation;
import com.sofka.plataforma_danos_back.folios.domain.port.CotizacionRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.CoverageOptionsRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.UbicacionCotizacionRepository;

@Service
public class GetCoverageOptionsUseCase {

    private final CotizacionRepository cotizacionRepository;
    private final CoverageOptionsRepository coverageOptionsRepository;
    private final UbicacionCotizacionRepository ubicacionCotizacionRepository;
    private final CoverageOptionsProjectionService coverageOptionsProjectionService;

    public GetCoverageOptionsUseCase(
            CotizacionRepository cotizacionRepository,
            CoverageOptionsRepository coverageOptionsRepository,
            UbicacionCotizacionRepository ubicacionCotizacionRepository,
            CoverageOptionsProjectionService coverageOptionsProjectionService
    ) {
        this.cotizacionRepository = cotizacionRepository;
        this.coverageOptionsRepository = coverageOptionsRepository;
        this.ubicacionCotizacionRepository = ubicacionCotizacionRepository;
        this.coverageOptionsProjectionService = coverageOptionsProjectionService;
    }

    @Transactional(readOnly = true)
    public CoverageOptionsResponse handle(String numeroFolio) {
        Cotizacion cotizacion = cotizacionRepository.findByNumeroFolio(numeroFolio)
                .orElseThrow(() -> new QuoteNotFoundException(numeroFolio));

        CoverageOptions coverageOptions = coverageOptionsRepository.findByCotizacionId(cotizacion.id())
                .orElseGet(() -> CoverageOptions.empty(cotizacion.id()));
        List<CoverageProjectionPerLocation> projection = coverageOptions.garantiasSeleccionadas().isEmpty()
                ? List.of()
                : coverageOptionsProjectionService.buildProjection(
                        ubicacionCotizacionRepository.findAllByCotizacionId(cotizacion.id()),
                        coverageOptions.garantiasSeleccionadas()
                );
        return CoverageOptionsResponse.from(cotizacion, coverageOptions, projection);
    }
}