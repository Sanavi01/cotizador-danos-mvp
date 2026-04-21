package com.sofka.plataforma_danos_back.folios.application;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sofka.plataforma_danos_back.folios.application.dto.LocationsSummaryResponse;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.AlertaBloqueante;
import com.sofka.plataforma_danos_back.folios.domain.ConfiguracionLayout;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.EstadoValidacion;
import com.sofka.plataforma_danos_back.folios.domain.Giro;
import com.sofka.plataforma_danos_back.folios.domain.LayoutUbicacionSlot;
import com.sofka.plataforma_danos_back.folios.domain.ModoCaptura;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionDetalle;
import com.sofka.plataforma_danos_back.folios.domain.ZonaCatastrofica;
import com.sofka.plataforma_danos_back.folios.domain.port.ConfiguracionLayoutRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.CotizacionRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.UbicacionCotizacionRepository;

@ExtendWith(MockitoExtension.class)
class GetLocationsSummaryUseCaseTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-04-21T00:00:00Z");

    @Mock
    private CotizacionRepository cotizacionRepository;

    @Mock
    private ConfiguracionLayoutRepository configuracionLayoutRepository;

    @Mock
    private UbicacionCotizacionRepository ubicacionCotizacionRepository;

    @Test
    void handle_returnsSummaryWhenFolioExists() {
        GetLocationsSummaryUseCase useCase = new GetLocationsSummaryUseCase(
                cotizacionRepository,
                configuracionLayoutRepository,
                ubicacionCotizacionRepository
        );
        Cotizacion cotizacion = Cotizacion.nueva("1000001", FIXED_NOW).withPersistence(13L, 3L, FIXED_NOW);
        ConfiguracionLayout layout = ConfiguracionLayout.nueva(
                13L,
                ModoCaptura.MULTIPLE,
                3,
                List.of(
                        new LayoutUbicacionSlot(1, 1),
                        new LayoutUbicacionSlot(2, 2),
                        new LayoutUbicacionSlot(3, 3)
                ),
                FIXED_NOW
        ).withPersistence(21L, FIXED_NOW, FIXED_NOW);

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));
        when(configuracionLayoutRepository.findByCotizacionId(13L)).thenReturn(Optional.of(layout));
        when(ubicacionCotizacionRepository.findAllByCotizacionId(13L)).thenReturn(List.of(validLocation(), invalidLocation()));

        LocationsSummaryResponse response = useCase.handle("1000001");

        assertEquals("1000001", response.numeroFolio());
        assertEquals(3, response.totalEsperado());
        assertEquals(2, response.totalActual());
        assertEquals(1, response.calculables());
        assertEquals(0, response.incompletas());
        assertEquals(1, response.invalidas());
        assertEquals(1, response.conAlertas());
        assertEquals(3, response.resumenPorIndice().size());
        assertEquals(1, response.resumenPorIndice().get(0).indice());
        assertEquals("CALCULABLE", response.resumenPorIndice().get(0).estadoValidacion().name());
        assertEquals(2, response.resumenPorIndice().get(1).indice());
        assertEquals("INVALID", response.resumenPorIndice().get(1).estadoValidacion().name());
        assertEquals(3, response.resumenPorIndice().get(2).indice());
        assertEquals("EMPTY", response.resumenPorIndice().get(2).estadoValidacion().name());

        verify(cotizacionRepository).findByNumeroFolio("1000001");
        verify(configuracionLayoutRepository).findByCotizacionId(13L);
        verify(ubicacionCotizacionRepository).findAllByCotizacionId(13L);
    }

    @Test
    void handle_throwsWhenFolioDoesNotExist() {
        GetLocationsSummaryUseCase useCase = new GetLocationsSummaryUseCase(
                cotizacionRepository,
                configuracionLayoutRepository,
                ubicacionCotizacionRepository
        );

        when(cotizacionRepository.findByNumeroFolio("9999999")).thenReturn(Optional.empty());

        QuoteNotFoundException exception = assertThrows(
                QuoteNotFoundException.class,
                () -> useCase.handle("9999999")
        );

        assertEquals("No existe una cotizacion con numeroFolio 9999999", exception.getMessage());
        verify(cotizacionRepository).findByNumeroFolio("9999999");
        verifyNoInteractions(configuracionLayoutRepository, ubicacionCotizacionRepository);
    }

    private static UbicacionCotizacion validLocation() {
        return new UbicacionCotizacion(
                11L,
                13L,
                new UbicacionDetalle(
                        1,
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
                        new ZonaCatastrofica("Z-TEV-01", "Z-FHM-01")
                ),
                EstadoValidacion.CALCULABLE,
                List.of(),
                FIXED_NOW,
                FIXED_NOW
        );
    }

    private static UbicacionCotizacion invalidLocation() {
        return new UbicacionCotizacion(
                20L,
                13L,
                new UbicacionDetalle(
                        2,
                        "Bodega secundaria",
                        null,
                        "000000",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                EstadoValidacion.INVALID,
                List.of(new AlertaBloqueante("UBICACION_SIN_ZIP", "La ubicacion no tiene codigo postal valido.", "Warning")),
                FIXED_NOW,
                FIXED_NOW
        );
    }
}