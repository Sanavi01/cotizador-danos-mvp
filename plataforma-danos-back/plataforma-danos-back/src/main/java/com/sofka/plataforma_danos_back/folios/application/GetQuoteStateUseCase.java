package com.sofka.plataforma_danos_back.folios.application;

import com.sofka.plataforma_danos_back.folios.application.dto.QuoteStateResponse;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.port.ConfiguracionLayoutRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.CotizacionRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.DatosGeneralesCotizacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetQuoteStateUseCase {

    private final CotizacionRepository cotizacionRepository;
    private final DatosGeneralesCotizacionRepository datosGeneralesCotizacionRepository;
    private final ConfiguracionLayoutRepository configuracionLayoutRepository;

    public GetQuoteStateUseCase(CotizacionRepository cotizacionRepository) {
        this(cotizacionRepository, null, null);
    }

    @Autowired
    public GetQuoteStateUseCase(
            CotizacionRepository cotizacionRepository,
            DatosGeneralesCotizacionRepository datosGeneralesCotizacionRepository,
            ConfiguracionLayoutRepository configuracionLayoutRepository
    ) {
        this.cotizacionRepository = cotizacionRepository;
        this.datosGeneralesCotizacionRepository = datosGeneralesCotizacionRepository;
        this.configuracionLayoutRepository = configuracionLayoutRepository;
    }

    @Transactional(readOnly = true)
    public QuoteStateResponse handle(String numeroFolio) {
        return cotizacionRepository.findByNumeroFolio(numeroFolio)
                .map(cotizacion -> QuoteStateResponse.from(
                        cotizacion,
                        hasGeneralInfo(cotizacion.id()),
                        hasLocationsLayout(cotizacion.id())
                ))
                .orElseThrow(() -> new QuoteNotFoundException(numeroFolio));
    }

    private boolean hasGeneralInfo(Long cotizacionId) {
        return datosGeneralesCotizacionRepository != null && datosGeneralesCotizacionRepository.existsByCotizacionId(cotizacionId);
    }

    private boolean hasLocationsLayout(Long cotizacionId) {
        return configuracionLayoutRepository != null && configuracionLayoutRepository.existsByCotizacionId(cotizacionId);
    }
}
