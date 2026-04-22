package com.sofka.plataforma_danos_back.folios.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sofka.plataforma_danos_back.folios.application.dto.QuoteStateResponse;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.AlertaBloqueante;
import com.sofka.plataforma_danos_back.folios.domain.ConfiguracionLayout;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.CoverageOptions;
import com.sofka.plataforma_danos_back.folios.domain.EstadoCalculo;
import com.sofka.plataforma_danos_back.folios.domain.EstadoCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.EstadoValidacion;
import com.sofka.plataforma_danos_back.folios.domain.DatosGeneralesCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.Giro;
import com.sofka.plataforma_danos_back.folios.domain.LayoutUbicacionSlot;
import com.sofka.plataforma_danos_back.folios.domain.ModoCaptura;
import com.sofka.plataforma_danos_back.folios.domain.PrimaPorUbicacion;
import com.sofka.plataforma_danos_back.folios.domain.SelectedGuarantee;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionDetalle;
import com.sofka.plataforma_danos_back.folios.domain.ZonaCatastrofica;
import com.sofka.plataforma_danos_back.folios.domain.port.ConfiguracionLayoutRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.CotizacionRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.CoverageOptionsRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.DatosGeneralesCotizacionRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.PrimaPorUbicacionRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.UbicacionCotizacionRepository;

@ExtendWith(MockitoExtension.class)
class GetQuoteStateUseCaseTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-04-21T00:00:00Z");

    @Mock
    private CotizacionRepository cotizacionRepository;

    @Mock
    private DatosGeneralesCotizacionRepository datosGeneralesCotizacionRepository;

    @Mock
    private ConfiguracionLayoutRepository configuracionLayoutRepository;

    @Mock
    private UbicacionCotizacionRepository ubicacionCotizacionRepository;

    @Mock
    private CoverageOptionsRepository coverageOptionsRepository;

    @Mock
    private PrimaPorUbicacionRepository primaPorUbicacionRepository;

    @Test
    void handle_returnsBorradorWhenFolioExistsWithoutSections() {
        GetQuoteStateUseCase useCase = new GetQuoteStateUseCase(cotizacionRepository);
        Cotizacion cotizacion = Cotizacion.nueva("1000001", FIXED_NOW).withPersistence(3L, 0L, FIXED_NOW);

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));

        QuoteStateResponse response = useCase.handle("1000001");

        assertEquals("1000001", response.numeroFolio());
        assertEquals(EstadoCotizacion.BORRADOR, response.estadoCotizacion());
        assertEquals(0L, response.version());
        assertEquals(FIXED_NOW, response.fechaUltimaActualizacion());
        assertEquals(QuoteStateResponse.EstadoSeccion.INCOMPLETE, response.progreso().datosGenerales());
        assertEquals(QuoteStateResponse.EstadoSeccion.INCOMPLETE, response.progreso().layoutUbicaciones());
        assertEquals(QuoteStateResponse.EstadoSeccion.INCOMPLETE, response.progreso().ubicaciones());
        assertEquals(QuoteStateResponse.EstadoSeccion.INCOMPLETE, response.progreso().opcionesCobertura());
        assertEquals(0, response.resumenUbicaciones().totalEsperado());
        assertEquals(0, response.resumenUbicaciones().totalActual());
        assertEquals(0, response.resumenUbicaciones().calculables());
        assertEquals(0, response.resumenUbicaciones().incompletas());
        assertEquals(0, response.resumenUbicaciones().invalidas());
        assertEquals(0, response.resumenUbicaciones().conAlertas());
        assertFalse(response.tieneAlertas());
        assertTrue(response.alertasVigentes().isEmpty());
        assertFalse(response.readyToCalculate());
        assertNull(response.resultadoFinanciero());
        verify(cotizacionRepository).findByNumeroFolio("1000001");
    }

    @Test
    void handle_returnsConsolidatedStateWhenLayoutOptionsAndLocationsExist() {
        GetQuoteStateUseCase useCase = new GetQuoteStateUseCase(
                cotizacionRepository,
                datosGeneralesCotizacionRepository,
                configuracionLayoutRepository,
                ubicacionCotizacionRepository,
                coverageOptionsRepository,
                primaPorUbicacionRepository
        );
        Cotizacion cotizacion = Cotizacion.nueva("1000001", FIXED_NOW).withPersistence(13L, 4L, FIXED_NOW);
        ConfiguracionLayout layout = ConfiguracionLayout.nueva(
                13L,
                ModoCaptura.MULTIPLE,
                3,
                List.of(new LayoutUbicacionSlot(1, 1), new LayoutUbicacionSlot(2, 2), new LayoutUbicacionSlot(3, 3)),
                FIXED_NOW
        ).withPersistence(21L, FIXED_NOW, FIXED_NOW);

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));
        when(datosGeneralesCotizacionRepository.findByCotizacionId(13L)).thenReturn(Optional.of(defaultGeneralInfo(13L)));
        when(configuracionLayoutRepository.findByCotizacionId(13L)).thenReturn(Optional.of(layout));
        when(coverageOptionsRepository.findByCotizacionId(13L)).thenReturn(Optional.of(new CoverageOptions(13L, List.of(new SelectedGuarantee("GAR-INC-ED", List.of("incendio"))), null)));
        when(ubicacionCotizacionRepository.findAllByCotizacionId(13L)).thenReturn(List.of(calculableLocation(1), invalidLocation(3)));
        when(primaPorUbicacionRepository.findAllByCotizacionId(13L)).thenReturn(List.of(
                calculablePrima(1),
                nonCalculablePrima(3)
        ));

        QuoteStateResponse response = useCase.handle("1000001");

        assertEquals(EstadoCotizacion.LISTA_PARA_CALCULO, response.estadoCotizacion());
        assertEquals(QuoteStateResponse.EstadoSeccion.COMPLETED, response.progreso().datosGenerales());
        assertEquals(QuoteStateResponse.EstadoSeccion.COMPLETED, response.progreso().layoutUbicaciones());
        assertEquals(QuoteStateResponse.EstadoSeccion.INCOMPLETE, response.progreso().ubicaciones());
        assertEquals(QuoteStateResponse.EstadoSeccion.COMPLETED, response.progreso().opcionesCobertura());
        assertEquals(3, response.resumenUbicaciones().totalEsperado());
        assertEquals(2, response.resumenUbicaciones().totalActual());
        assertEquals(1, response.resumenUbicaciones().calculables());
        assertEquals(0, response.resumenUbicaciones().incompletas());
        assertEquals(1, response.resumenUbicaciones().invalidas());
        assertEquals(1, response.resumenUbicaciones().conAlertas());
        assertTrue(response.tieneAlertas());
        assertEquals(1, response.alertasVigentes().size());
        assertEquals("UBICACION_SIN_ZIP", response.alertasVigentes().get(0).codigo());
        assertTrue(response.readyToCalculate());
        assertNull(response.resultadoFinanciero());
        verify(cotizacionRepository).findByNumeroFolio("1000001");
        verify(datosGeneralesCotizacionRepository).findByCotizacionId(13L);
        verify(configuracionLayoutRepository).findByCotizacionId(13L);
        verify(coverageOptionsRepository).findByCotizacionId(13L);
        verify(ubicacionCotizacionRepository).findAllByCotizacionId(13L);
        verify(primaPorUbicacionRepository).findAllByCotizacionId(13L);
    }

    @Test
    void handle_returnsFinancialSummaryWhenTotalsArePersisted() {
        GetQuoteStateUseCase useCase = new GetQuoteStateUseCase(
                cotizacionRepository,
                datosGeneralesCotizacionRepository,
                configuracionLayoutRepository,
                ubicacionCotizacionRepository,
                coverageOptionsRepository,
                primaPorUbicacionRepository
        );
        Cotizacion cotizacion = new Cotizacion(
                13L,
                "1000001",
                EstadoCotizacion.CALCULADA,
                5L,
                FIXED_NOW,
                new BigDecimal("24120.00"),
                new BigDecimal("28220.40"),
                EstadoCalculo.CALCULADO,
                FIXED_NOW,
                "1.0.0"
        );

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));
        when(primaPorUbicacionRepository.findAllByCotizacionId(13L)).thenReturn(List.of(calculablePrima(1), nonCalculablePrima(3)));

        QuoteStateResponse response = useCase.handle("1000001");

        assertEquals(EstadoCotizacion.CALCULADA, response.estadoCotizacion());
        assertEquals(5L, response.version());
        assertEquals(FIXED_NOW, response.fechaUltimaActualizacion());
        assertFalse(response.readyToCalculate());
        assertEquals(new BigDecimal("24120.00"), response.resultadoFinanciero().primaNeta());
        assertEquals(new BigDecimal("28220.40"), response.resultadoFinanciero().primaComercial());
        assertEquals(1, response.resultadoFinanciero().ubicacionesCalculadas());
        assertEquals(1, response.resultadoFinanciero().ubicacionesNoCalculables());
        assertEquals(QuoteStateResponse.EstadoCalculoResumen.CALCULADO, response.resultadoFinanciero().estadoCalculo());
        assertEquals(FIXED_NOW, response.resultadoFinanciero().calculatedAt());
        assertEquals("1.0.0", response.resultadoFinanciero().calculationParameterVersion());
        verify(cotizacionRepository).findByNumeroFolio("1000001");
                verify(datosGeneralesCotizacionRepository).findByCotizacionId(13L);
                verify(configuracionLayoutRepository).findByCotizacionId(13L);
                verify(coverageOptionsRepository).findByCotizacionId(13L);
                verify(ubicacionCotizacionRepository).findAllByCotizacionId(13L);
        verify(primaPorUbicacionRepository).findAllByCotizacionId(13L);
    }

    @Test
    void handle_throwsWhenFolioDoesNotExist() {
        GetQuoteStateUseCase useCase = new GetQuoteStateUseCase(cotizacionRepository);

        when(cotizacionRepository.findByNumeroFolio("9999999")).thenReturn(Optional.empty());

        QuoteNotFoundException exception = assertThrows(
                QuoteNotFoundException.class,
                () -> useCase.handle("9999999")
        );

        assertEquals("No existe una cotizacion con numeroFolio 9999999", exception.getMessage());
        verify(cotizacionRepository).findByNumeroFolio("9999999");
        verifyNoInteractions(datosGeneralesCotizacionRepository, configuracionLayoutRepository, ubicacionCotizacionRepository, coverageOptionsRepository, primaPorUbicacionRepository);
    }

    private static DatosGeneralesCotizacion defaultGeneralInfo(Long cotizacionId) {
        return new DatosGeneralesCotizacion(
                cotizacionId,
                new DatosGeneralesCotizacion.DatosAsegurado(
                        "NIT",
                        "900123456",
                        "ACME SAS",
                        "contacto@acme.com",
                        "6015550101"
                ),
                new DatosGeneralesCotizacion.DatosConduccion("AG-102", "RISK-A", "GIRO-001"),
                FIXED_NOW,
                FIXED_NOW
        );
    }

    private static UbicacionCotizacion calculableLocation(int indice) {
        return new UbicacionCotizacion(
                (long) indice,
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
                        new ZonaCatastrofica("ZTEV-1", "Z-FHM-01")
                ),
                EstadoValidacion.CALCULABLE,
                List.of(),
                FIXED_NOW,
                FIXED_NOW
        );
    }

    private static UbicacionCotizacion invalidLocation(int indice) {
        return new UbicacionCotizacion(
                (long) indice,
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
                        new ZonaCatastrofica("ZTEV-1", "Z-FHM-01")
                ),
                EstadoValidacion.INVALID,
                List.of(new AlertaBloqueante("UBICACION_SIN_ZIP", "La ubicacion no tiene codigo postal valido.", "Warning")),
                FIXED_NOW,
                FIXED_NOW
        );
    }

    private static PrimaPorUbicacion calculablePrima(int indice) {
        return new PrimaPorUbicacion(
                indice,
                true,
                new BigDecimal("24120.00"),
                new BigDecimal("28220.40"),
                List.of(),
                List.of(),
                List.of()
        );
    }

    private static PrimaPorUbicacion nonCalculablePrima(int indice) {
        return new PrimaPorUbicacion(
                indice,
                false,
                BigDecimal.ZERO.setScale(2),
                BigDecimal.ZERO.setScale(2),
                List.of(),
                List.of(),
                List.of(new AlertaBloqueante("UBICACION_SIN_ZIP", "La ubicacion no tiene codigo postal valido.", "Warning"))
        );
    }
}