package com.sofka.plataforma_danos_back.folios.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sofka.plataforma_danos_back.folios.application.dto.CalculateQuoteRequest;
import com.sofka.plataforma_danos_back.folios.application.dto.CalculateQuoteResponse;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteCalculationRejectedException;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteCalculationVersionConflictException;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.EstadoCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.PrimaPorUbicacion;
import com.sofka.plataforma_danos_back.folios.domain.SelectedGuarantee;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.port.CotizacionRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.CoverageOptionsRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.PrimaPorUbicacionRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.UbicacionCotizacionRepository;

@Service
public class CalculateQuoteUseCase {

    private final CotizacionRepository cotizacionRepository;
    private final CoverageOptionsRepository coverageOptionsRepository;
    private final UbicacionCotizacionRepository ubicacionCotizacionRepository;
    private final PrimaPorUbicacionRepository primaPorUbicacionRepository;
    private final QuoteCalculationEngine quoteCalculationEngine;
    private final Clock clock;

    @Autowired
    public CalculateQuoteUseCase(
            CotizacionRepository cotizacionRepository,
            CoverageOptionsRepository coverageOptionsRepository,
            UbicacionCotizacionRepository ubicacionCotizacionRepository,
            PrimaPorUbicacionRepository primaPorUbicacionRepository,
            QuoteCalculationEngine quoteCalculationEngine
    ) {
        this(cotizacionRepository, coverageOptionsRepository, ubicacionCotizacionRepository, primaPorUbicacionRepository, quoteCalculationEngine, Clock.systemUTC());
    }

    public CalculateQuoteUseCase(
            CotizacionRepository cotizacionRepository,
            CoverageOptionsRepository coverageOptionsRepository,
            UbicacionCotizacionRepository ubicacionCotizacionRepository,
            PrimaPorUbicacionRepository primaPorUbicacionRepository,
            QuoteCalculationEngine quoteCalculationEngine,
            Clock clock
    ) {
        this.cotizacionRepository = Objects.requireNonNull(cotizacionRepository, "cotizacionRepository es obligatorio");
        this.coverageOptionsRepository = Objects.requireNonNull(coverageOptionsRepository, "coverageOptionsRepository es obligatorio");
        this.ubicacionCotizacionRepository = Objects.requireNonNull(ubicacionCotizacionRepository, "ubicacionCotizacionRepository es obligatorio");
        this.primaPorUbicacionRepository = Objects.requireNonNull(primaPorUbicacionRepository, "primaPorUbicacionRepository es obligatorio");
        this.quoteCalculationEngine = Objects.requireNonNull(quoteCalculationEngine, "quoteCalculationEngine es obligatorio");
        this.clock = Objects.requireNonNull(clock, "clock es obligatorio");
    }

    @Transactional
    public CalculateQuoteResponse handle(String numeroFolio, CalculateQuoteRequest request) {
        Cotizacion cotizacion = cotizacionRepository.findByNumeroFolio(numeroFolio)
                .orElseThrow(() -> new QuoteNotFoundException(numeroFolio));

        validateVersion(numeroFolio, request.version(), cotizacion.version());

        List<SelectedGuarantee> garantiasSeleccionadas = coverageOptionsRepository.findByCotizacionId(cotizacion.id())
                .map(coverageOptions -> coverageOptions.garantiasSeleccionadas())
                .orElseThrow(() -> new QuoteCalculationRejectedException("La cotizacion no tiene garantias activas seleccionadas"));
        if (garantiasSeleccionadas.isEmpty()) {
            throw new QuoteCalculationRejectedException("La cotizacion no tiene garantias activas seleccionadas");
        }

        List<UbicacionCotizacion> ubicaciones = ubicacionCotizacionRepository.findAllByCotizacionId(cotizacion.id());
        if (ubicaciones.isEmpty()) {
            throw new QuoteCalculationRejectedException("La cotizacion no tiene ubicaciones persistidas para calcular");
        }

        QuoteCalculationEngine.QuoteCalculationResult calculationResult = quoteCalculationEngine.calculate(
                cotizacion,
                ubicaciones,
                garantiasSeleccionadas
        );

        if (calculationResult.ubicacionesCalculadas() == 0) {
            throw new QuoteCalculationRejectedException("La cotizacion no tiene ubicaciones calculables");
        }

        primaPorUbicacionRepository.deleteByCotizacionId(cotizacion.id());
        List<PrimaPorUbicacion> savedPrimas = primaPorUbicacionRepository.saveAll(cotizacion.id(), calculationResult.primasPorUbicacion());

        Instant now = Instant.now(clock);
        Cotizacion cotizacionCalculada = cotizacion.withFinancialResult(
                        calculationResult.primaNeta(),
                        calculationResult.primaComercial(),
                        calculationResult.estadoCalculo(),
                        calculationResult.calculatedAt(),
                        calculationResult.calculationParameterVersion()
                )
                .withEstado(EstadoCotizacion.CALCULADA)
                .withPersistence(cotizacion.id(), cotizacion.version(), now);
        Cotizacion savedCotizacion = cotizacionRepository.save(cotizacionCalculada);

        return CalculateQuoteResponse.from(savedCotizacion, savedPrimas, calculationResult.alertasVigentes());
    }

    private void validateVersion(String numeroFolio, Long requestVersion, Long currentVersion) {
        if (!Objects.equals(currentVersion, requestVersion)) {
            throw new QuoteCalculationVersionConflictException(numeroFolio, requestVersion, currentVersion);
        }
    }
}