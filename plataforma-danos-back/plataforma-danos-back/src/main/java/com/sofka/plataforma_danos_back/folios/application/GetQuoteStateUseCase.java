package com.sofka.plataforma_danos_back.folios.application;

import com.sofka.plataforma_danos_back.folios.application.dto.QuoteStateResponse;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.port.CotizacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetQuoteStateUseCase {

    private final CotizacionRepository cotizacionRepository;

    public GetQuoteStateUseCase(CotizacionRepository cotizacionRepository) {
        this.cotizacionRepository = cotizacionRepository;
    }

    @Transactional(readOnly = true)
    public QuoteStateResponse handle(String numeroFolio) {
        return cotizacionRepository.findByNumeroFolio(numeroFolio)
                .map(QuoteStateResponse::from)
                .orElseThrow(() -> new QuoteNotFoundException(numeroFolio));
    }
}
