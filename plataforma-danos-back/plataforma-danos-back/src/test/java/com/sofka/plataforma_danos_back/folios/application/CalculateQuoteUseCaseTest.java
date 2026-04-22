package com.sofka.plataforma_danos_back.folios.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sofka.plataforma_danos_back.folios.application.dto.CalculateQuoteRequest;
import com.sofka.plataforma_danos_back.folios.application.dto.CalculateQuoteResponse;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteCalculationRejectedException;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteCalculationVersionConflictException;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.CoverageOptions;
import com.sofka.plataforma_danos_back.folios.domain.EstadoCalculo;
import com.sofka.plataforma_danos_back.folios.domain.EstadoCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.EstadoValidacion;
import com.sofka.plataforma_danos_back.folios.domain.Giro;
import com.sofka.plataforma_danos_back.folios.domain.SelectedGuarantee;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionDetalle;
import com.sofka.plataforma_danos_back.folios.domain.ZonaCatastrofica;
import com.sofka.plataforma_danos_back.folios.domain.port.CotizacionRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.CoverageOptionsRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.PrimaPorUbicacionRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.UbicacionCotizacionRepository;
import com.sofka.plataforma_danos_back.folios.infrastructure.reference.ReferenceCalculationCatalog;

@ExtendWith(MockitoExtension.class)
class CalculateQuoteUseCaseTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-04-21T00:00:00Z");

    @Mock
    private CotizacionRepository cotizacionRepository;

    @Mock
    private CoverageOptionsRepository coverageOptionsRepository;

    @Mock
    private UbicacionCotizacionRepository ubicacionCotizacionRepository;

    @Mock
    private PrimaPorUbicacionRepository primaPorUbicacionRepository;

    private CalculateQuoteUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CalculateQuoteUseCase(
                cotizacionRepository,
                coverageOptionsRepository,
                ubicacionCotizacionRepository,
                primaPorUbicacionRepository,
                new QuoteCalculationEngine(new ReferenceCalculationCatalog(), Clock.fixed(FIXED_NOW, ZoneOffset.UTC)),
                Clock.fixed(FIXED_NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void handle_returnsCalculatedResponseWhenSnapshotIsValid() {
        Cotizacion cotizacion = cotizacionBase();
        CoverageOptions coverageOptions = new CoverageOptions(
            cotizacion.id(),
            List.of(new SelectedGuarantee("GAR-INC-ED", List.of("incendio"))),
            null
        );

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));
        when(coverageOptionsRepository.findByCotizacionId(13L)).thenReturn(Optional.of(coverageOptions));
        when(ubicacionCotizacionRepository.findAllByCotizacionId(13L)).thenReturn(List.of(validLocation(1)));
        when(primaPorUbicacionRepository.saveAll(eq(13L), anyList())).thenAnswer(invocation -> invocation.getArgument(1));
        when(cotizacionRepository.save(any(Cotizacion.class))).thenAnswer(invocation -> {
            Cotizacion saved = invocation.getArgument(0);
            return saved.withPersistence(saved.id(), saved.version() + 1, FIXED_NOW);
        });

        CalculateQuoteResponse response = useCase.handle("1000001", new CalculateQuoteRequest(4L));

        assertEquals("1000001", response.numeroFolio());
        assertEquals(EstadoCotizacion.CALCULADA, response.estadoCotizacion());
        assertEquals(EstadoCalculo.CALCULADO, response.estadoCalculo());
        assertEquals(new BigDecimal("24120.00"), response.primaNeta());
        assertEquals(new BigDecimal("28220.40"), response.primaComercial());
        assertEquals("1.0.0", response.calculationParameterVersion());
        assertEquals(5L, response.version());
        assertEquals(FIXED_NOW, response.calculatedAt());
        assertEquals(FIXED_NOW, response.fechaUltimaActualizacion());
        assertEquals(1, response.primasPorUbicacion().size());
        assertEquals(1, response.primasPorUbicacion().get(0).garantiasCalculadas().size());
        assertEquals(2, response.primasPorUbicacion().get(0).garantiasCalculadas().get(0).componentes().size());
        assertTrue(response.alertasVigentes().isEmpty());

        ArgumentCaptor<Cotizacion> cotizacionCaptor = ArgumentCaptor.forClass(Cotizacion.class);
        verify(primaPorUbicacionRepository).deleteByCotizacionId(13L);
        verify(primaPorUbicacionRepository).saveAll(eq(13L), anyList());
        verify(cotizacionRepository).save(cotizacionCaptor.capture());
        assertEquals(new BigDecimal("24120.00"), cotizacionCaptor.getValue().primaNeta());
        assertEquals(new BigDecimal("28220.40"), cotizacionCaptor.getValue().primaComercial());
        assertEquals(EstadoCalculo.CALCULADO, cotizacionCaptor.getValue().estadoCalculo());
        assertEquals("1.0.0", cotizacionCaptor.getValue().calculationParameterVersion());
    }

    @Test
    void handle_returnsPartialResponseWhenOneLocationIsIncomplete() {
        Cotizacion cotizacion = cotizacionBase();
        CoverageOptions coverageOptions = new CoverageOptions(
            cotizacion.id(),
            List.of(new SelectedGuarantee("GAR-INC-ED", List.of("incendio"))),
            null
        );

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));
        when(coverageOptionsRepository.findByCotizacionId(13L)).thenReturn(Optional.of(coverageOptions));
        when(ubicacionCotizacionRepository.findAllByCotizacionId(13L)).thenReturn(List.of(validLocation(1), invalidLocation(2)));
        when(primaPorUbicacionRepository.saveAll(eq(13L), anyList())).thenAnswer(invocation -> invocation.getArgument(1));
        when(cotizacionRepository.save(any(Cotizacion.class))).thenAnswer(invocation -> {
            Cotizacion saved = invocation.getArgument(0);
            return saved.withPersistence(saved.id(), saved.version() + 1, FIXED_NOW);
        });

        CalculateQuoteResponse response = useCase.handle("1000001", new CalculateQuoteRequest(4L));

        assertEquals(EstadoCalculo.PARCIAL, response.estadoCalculo());
        assertEquals(new BigDecimal("24120.00"), response.primaNeta());
        assertEquals(2, response.primasPorUbicacion().size());
        assertFalse(response.alertasVigentes().isEmpty());
        assertEquals("UBICACION_SIN_ZIP", response.alertasVigentes().get(0).codigo());
        assertEquals(1, response.primasPorUbicacion().stream().filter(CalculateQuoteResponse.PrimaPorUbicacionView::ubicacionCalculable).count());
        assertEquals(1, response.primasPorUbicacion().stream().filter(item -> !item.ubicacionCalculable()).count());

        verify(primaPorUbicacionRepository).deleteByCotizacionId(13L);
        verify(primaPorUbicacionRepository).saveAll(eq(13L), anyList());
        verify(cotizacionRepository).save(any(Cotizacion.class));
    }

    @Test
    void handle_throwsWhenFolioDoesNotExist() {
        when(cotizacionRepository.findByNumeroFolio("9999999")).thenReturn(Optional.empty());

        QuoteNotFoundException exception = assertThrows(
                QuoteNotFoundException.class,
                () -> useCase.handle("9999999", new CalculateQuoteRequest(4L))
        );

        assertEquals("No existe una cotizacion con numeroFolio 9999999", exception.getMessage());
        verify(cotizacionRepository).findByNumeroFolio("9999999");
        verifyNoInteractions(coverageOptionsRepository, ubicacionCotizacionRepository, primaPorUbicacionRepository);
    }

    @Test
    void handle_throwsWhenVersionIsOutdated() {
        Cotizacion cotizacion = cotizacionBase();
        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));

        QuoteCalculationVersionConflictException exception = assertThrows(
                QuoteCalculationVersionConflictException.class,
                () -> useCase.handle("1000001", new CalculateQuoteRequest(99L))
        );

        assertEquals("La version de calculo para el folio 1000001 no coincide. Solicitada: 99, vigente: 4", exception.getMessage());
        verify(cotizacionRepository).findByNumeroFolio("1000001");
        verifyNoInteractions(coverageOptionsRepository, ubicacionCotizacionRepository, primaPorUbicacionRepository);
    }

    @Test
    void handle_throwsWhenCoverageOptionsAreMissing() {
        Cotizacion cotizacion = cotizacionBase();
        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));
        when(coverageOptionsRepository.findByCotizacionId(13L)).thenReturn(Optional.empty());

        QuoteCalculationRejectedException exception = assertThrows(
                QuoteCalculationRejectedException.class,
                () -> useCase.handle("1000001", new CalculateQuoteRequest(4L))
        );

        assertEquals("La cotizacion no tiene garantias activas seleccionadas", exception.getMessage());
        verify(cotizacionRepository).findByNumeroFolio("1000001");
        verify(coverageOptionsRepository).findByCotizacionId(13L);
        verifyNoInteractions(ubicacionCotizacionRepository, primaPorUbicacionRepository);
    }

    @Test
    void handle_throwsWhenNoLocationIsCalculable() {
        Cotizacion cotizacion = cotizacionBase();
        CoverageOptions coverageOptions = new CoverageOptions(
                cotizacion.id(),
                List.of(new SelectedGuarantee("GAR-INC-ED", List.of("incendio"))),
                null
        );

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));
        when(coverageOptionsRepository.findByCotizacionId(13L)).thenReturn(Optional.of(coverageOptions));
        when(ubicacionCotizacionRepository.findAllByCotizacionId(13L)).thenReturn(List.of(invalidLocation(1)));

        QuoteCalculationRejectedException exception = assertThrows(
                QuoteCalculationRejectedException.class,
                () -> useCase.handle("1000001", new CalculateQuoteRequest(4L))
        );

        assertEquals("La cotizacion no tiene ubicaciones calculables", exception.getMessage());
        verify(cotizacionRepository).findByNumeroFolio("1000001");
        verify(coverageOptionsRepository).findByCotizacionId(13L);
        verify(ubicacionCotizacionRepository).findAllByCotizacionId(13L);
        verify(primaPorUbicacionRepository, never()).saveAll(anyLong(), anyList());
        verify(primaPorUbicacionRepository, never()).deleteByCotizacionId(anyLong());
        verify(cotizacionRepository, never()).save(any(Cotizacion.class));
    }

    private Cotizacion cotizacionBase() {
        return Cotizacion.nueva("1000001", FIXED_NOW).withPersistence(13L, 4L, FIXED_NOW);
    }

    private UbicacionCotizacion validLocation(int indice) {
        return new UbicacionCotizacion(
                21L,
                13L,
                new UbicacionDetalle(
                        indice,
                        "Planta principal",
                        "Calle 100 # 10-10",
                        "110111",
                        "Bogota D.C.",
                        "Bogota",
                        "Chapinero",
                        "Bogota",
                        "CONCRETO",
                        1,
                        2018,
                        new Giro("GIRO-001", "Manufactura ligera", "CI-001"),
                        new ZonaCatastrofica("ZTEV-1", "ZFHM-1")
                ),
                EstadoValidacion.CALCULABLE,
                List.of(),
                FIXED_NOW,
                FIXED_NOW
        );
    }

    private UbicacionCotizacion invalidLocation(int indice) {
        return new UbicacionCotizacion(
                22L,
                13L,
                new UbicacionDetalle(
                        indice,
                        "Bodega secundaria",
                        "Calle 101 # 11-11",
                        "000000",
                        "Bogota D.C.",
                        "Bogota",
                        "Chapinero",
                        "Bogota",
                        "CONCRETO",
                        1,
                        2018,
                        new Giro("GIRO-001", "Manufactura ligera", "CI-001"),
                        new ZonaCatastrofica("ZTEV-1", "ZFHM-1")
                ),
                EstadoValidacion.INVALID,
                List.of(),
                FIXED_NOW,
                FIXED_NOW
        );
    }
}