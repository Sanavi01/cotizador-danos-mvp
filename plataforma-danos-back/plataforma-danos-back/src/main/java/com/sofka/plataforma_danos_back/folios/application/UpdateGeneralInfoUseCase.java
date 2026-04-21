package com.sofka.plataforma_danos_back.folios.application;

import com.sofka.plataforma_danos_back.folios.application.dto.GeneralInfoRequest;
import com.sofka.plataforma_danos_back.folios.application.dto.GeneralInfoResponse;
import com.sofka.plataforma_danos_back.folios.application.exception.GeneralInfoCatalogValidationException;
import com.sofka.plataforma_danos_back.folios.application.exception.GeneralInfoVersionConflictException;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.DatosGeneralesCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.port.CotizacionRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.DatosGeneralesCotizacionRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.GeneralInfoCatalogPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class UpdateGeneralInfoUseCase {

    private final CotizacionRepository cotizacionRepository;
    private final DatosGeneralesCotizacionRepository datosGeneralesCotizacionRepository;
    private final GeneralInfoCatalogPort generalInfoCatalogPort;
    private final Clock clock;

    @Autowired
    public UpdateGeneralInfoUseCase(
            CotizacionRepository cotizacionRepository,
            DatosGeneralesCotizacionRepository datosGeneralesCotizacionRepository,
            GeneralInfoCatalogPort generalInfoCatalogPort
    ) {
        this(cotizacionRepository, datosGeneralesCotizacionRepository, generalInfoCatalogPort, Clock.systemUTC());
    }

    public UpdateGeneralInfoUseCase(
            CotizacionRepository cotizacionRepository,
            DatosGeneralesCotizacionRepository datosGeneralesCotizacionRepository,
            GeneralInfoCatalogPort generalInfoCatalogPort,
            Clock clock
    ) {
        this.cotizacionRepository = cotizacionRepository;
        this.datosGeneralesCotizacionRepository = datosGeneralesCotizacionRepository;
        this.generalInfoCatalogPort = generalInfoCatalogPort;
        this.clock = clock;
    }

    @Transactional
    public GeneralInfoResponse handle(String numeroFolio, GeneralInfoRequest request) {
        Cotizacion cotizacion = cotizacionRepository.findByNumeroFolio(numeroFolio)
                .orElseThrow(() -> new QuoteNotFoundException(numeroFolio));

        validateVersion(numeroFolio, request.version(), cotizacion.version());
        validateCatalogReferences(request);

        Instant now = Instant.now(clock);
        DatosGeneralesCotizacion datosGeneralesCotizacion = datosGeneralesCotizacionRepository.save(
                DatosGeneralesCotizacion.nueva(
                        cotizacion.id(),
                        request.datosAsegurado().toDomain(),
                        request.datosConduccion().toDomain(),
                        now
                )
        );
        Cotizacion cotizacionActualizada = cotizacionRepository.save(
                cotizacion.withPersistence(cotizacion.id(), cotizacion.version(), now)
        );
        return GeneralInfoResponse.from(cotizacionActualizada, datosGeneralesCotizacion);
    }

    private void validateVersion(String numeroFolio, Long requestVersion, Long currentVersion) {
        if (!Objects.equals(currentVersion, requestVersion)) {
            throw new GeneralInfoVersionConflictException(numeroFolio, requestVersion, currentVersion);
        }
    }

    private void validateCatalogReferences(GeneralInfoRequest request) {
        List<String> invalidFields = new ArrayList<>();

        if (!generalInfoCatalogPort.isActiveAgent(request.datosConduccion().codigoAgente())) {
            invalidFields.add("codigoAgente");
        }
        if (!generalInfoCatalogPort.isActiveRiskClassification(request.datosConduccion().clasificacionRiesgo())) {
            invalidFields.add("clasificacionRiesgo");
        }
        if (!generalInfoCatalogPort.isActiveBusinessLine(request.datosConduccion().tipoNegocio())) {
            invalidFields.add("tipoNegocio");
        }

        if (!invalidFields.isEmpty()) {
            throw new GeneralInfoCatalogValidationException(invalidFields);
        }
    }
}