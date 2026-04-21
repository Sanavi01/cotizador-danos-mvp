package com.sofka.plataforma_danos_back.folios.application;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sofka.plataforma_danos_back.folios.application.dto.CoverageOptionsRequest;
import com.sofka.plataforma_danos_back.folios.application.dto.CoverageOptionsResponse;
import com.sofka.plataforma_danos_back.folios.application.exception.CoverageOptionsCatalogValidationException;
import com.sofka.plataforma_danos_back.folios.application.exception.CoverageOptionsVersionConflictException;
import com.sofka.plataforma_danos_back.folios.application.exception.InvalidCoverageOptionsPayloadException;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.CoverageOptions;
import com.sofka.plataforma_danos_back.folios.domain.CoverageProjectionPerLocation;
import com.sofka.plataforma_danos_back.folios.domain.SelectedGuarantee;
import com.sofka.plataforma_danos_back.folios.domain.port.CotizacionRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.CoverageGuaranteeCatalogPort;
import com.sofka.plataforma_danos_back.folios.domain.port.CoverageOptionsRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.UbicacionCotizacionRepository;

@Service
public class UpdateCoverageOptionsUseCase {

    private final CotizacionRepository cotizacionRepository;
    private final CoverageOptionsRepository coverageOptionsRepository;
    private final UbicacionCotizacionRepository ubicacionCotizacionRepository;
    private final CoverageGuaranteeCatalogPort coverageGuaranteeCatalogPort;
    private final CoverageOptionsProjectionService coverageOptionsProjectionService;
    private final Clock clock;

    @Autowired
    public UpdateCoverageOptionsUseCase(
            CotizacionRepository cotizacionRepository,
            CoverageOptionsRepository coverageOptionsRepository,
            UbicacionCotizacionRepository ubicacionCotizacionRepository,
            CoverageGuaranteeCatalogPort coverageGuaranteeCatalogPort,
            CoverageOptionsProjectionService coverageOptionsProjectionService
    ) {
        this(cotizacionRepository, coverageOptionsRepository, ubicacionCotizacionRepository, coverageGuaranteeCatalogPort, coverageOptionsProjectionService, Clock.systemUTC());
    }

    public UpdateCoverageOptionsUseCase(
            CotizacionRepository cotizacionRepository,
            CoverageOptionsRepository coverageOptionsRepository,
            UbicacionCotizacionRepository ubicacionCotizacionRepository,
            CoverageGuaranteeCatalogPort coverageGuaranteeCatalogPort,
            CoverageOptionsProjectionService coverageOptionsProjectionService,
            Clock clock
    ) {
        this.cotizacionRepository = cotizacionRepository;
        this.coverageOptionsRepository = coverageOptionsRepository;
        this.ubicacionCotizacionRepository = ubicacionCotizacionRepository;
        this.coverageGuaranteeCatalogPort = coverageGuaranteeCatalogPort;
        this.coverageOptionsProjectionService = coverageOptionsProjectionService;
        this.clock = clock;
    }

    @Transactional
    public CoverageOptionsResponse handle(String numeroFolio, CoverageOptionsRequest request) {
        Cotizacion cotizacion = cotizacionRepository.findByNumeroFolio(numeroFolio)
                .orElseThrow(() -> new QuoteNotFoundException(numeroFolio));

        validateVersion(numeroFolio, request.version(), cotizacion.version());

        CoverageOptions coverageOptions = request.opcionesCobertura().toDomain(cotizacion.id());
        validateDuplicateGuarantees(coverageOptions.garantiasSeleccionadas());
        validateCatalogCoverage(coverageOptions.garantiasSeleccionadas());

        Instant now = Instant.now(clock);
        CoverageOptions savedCoverageOptions = coverageOptionsRepository.save(coverageOptions);
        Cotizacion savedCotizacion = cotizacionRepository.save(cotizacion.withPersistence(cotizacion.id(), cotizacion.version(), now));
        List<CoverageProjectionPerLocation> projection = coverageOptions.garantiasSeleccionadas().isEmpty()
                ? List.of()
                : coverageOptionsProjectionService.buildProjection(
                        ubicacionCotizacionRepository.findAllByCotizacionId(cotizacion.id()),
                        coverageOptions.garantiasSeleccionadas()
                );
        return CoverageOptionsResponse.from(savedCotizacion, savedCoverageOptions, projection);
    }

    private void validateVersion(String numeroFolio, Long requestVersion, Long currentVersion) {
        if (!Objects.equals(requestVersion, currentVersion)) {
            throw new CoverageOptionsVersionConflictException(numeroFolio, requestVersion, currentVersion);
        }
    }

    private void validateDuplicateGuarantees(List<SelectedGuarantee> garantiasSeleccionadas) {
        LinkedHashSet<String> uniqueCodes = new LinkedHashSet<>();
        List<String> duplicatedCodes = garantiasSeleccionadas.stream()
                .map(SelectedGuarantee::garantiaCode)
                .filter(code -> !uniqueCodes.add(code))
                .toList();

        if (!duplicatedCodes.isEmpty()) {
            throw new InvalidCoverageOptionsPayloadException("La solicitud contiene garantias duplicadas: %s".formatted(duplicatedCodes));
        }
    }

    private void validateCatalogCoverage(List<SelectedGuarantee> garantiasSeleccionadas) {
        List<String> invalidGuarantees = garantiasSeleccionadas.stream()
                .map(SelectedGuarantee::garantiaCode)
                .filter(code -> coverageGuaranteeCatalogPort.findByCode(code)
                        .filter(definition -> definition.activa())
                        .isEmpty())
                .toList();

        if (!invalidGuarantees.isEmpty()) {
            throw new CoverageOptionsCatalogValidationException(invalidGuarantees);
        }
    }
}