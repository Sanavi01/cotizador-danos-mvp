package com.sofka.plataforma_danos_back.folios.application;

import com.sofka.plataforma_danos_back.folios.application.dto.GeneralInfoResponse;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.port.CotizacionRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.DatosGeneralesCotizacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetGeneralInfoUseCase {

    private final CotizacionRepository cotizacionRepository;
    private final DatosGeneralesCotizacionRepository datosGeneralesCotizacionRepository;

    public GetGeneralInfoUseCase(
            CotizacionRepository cotizacionRepository,
            DatosGeneralesCotizacionRepository datosGeneralesCotizacionRepository
    ) {
        this.cotizacionRepository = cotizacionRepository;
        this.datosGeneralesCotizacionRepository = datosGeneralesCotizacionRepository;
    }

    @Transactional(readOnly = true)
    public GeneralInfoResponse handle(String numeroFolio) {
        return cotizacionRepository.findByNumeroFolio(numeroFolio)
                .map(cotizacion -> datosGeneralesCotizacionRepository.findByCotizacionId(cotizacion.id())
                        .map(datosGeneralesCotizacion -> GeneralInfoResponse.from(cotizacion, datosGeneralesCotizacion))
                        .orElseGet(() -> GeneralInfoResponse.empty(cotizacion)))
                .orElseThrow(() -> new QuoteNotFoundException(numeroFolio));
    }
}