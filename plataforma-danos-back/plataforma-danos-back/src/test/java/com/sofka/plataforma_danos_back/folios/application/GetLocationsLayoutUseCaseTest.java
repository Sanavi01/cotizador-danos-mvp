package com.sofka.plataforma_danos_back.folios.application;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sofka.plataforma_danos_back.folios.application.dto.LocationsLayoutResponse;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.ConfiguracionLayout;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.LayoutUbicacionSlot;
import com.sofka.plataforma_danos_back.folios.domain.ModoCaptura;
import com.sofka.plataforma_danos_back.folios.domain.port.ConfiguracionLayoutRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.CotizacionRepository;

@ExtendWith(MockitoExtension.class)
class GetLocationsLayoutUseCaseTest {

    @Mock
    private CotizacionRepository cotizacionRepository;

    @Mock
    private ConfiguracionLayoutRepository configuracionLayoutRepository;

    @Test
    void handle_returnsPersistedLayoutWhenSectionExists() {
        GetLocationsLayoutUseCase useCase = new GetLocationsLayoutUseCase(
                cotizacionRepository,
                configuracionLayoutRepository
        );
        Instant now = Instant.parse("2026-04-21T00:00:00Z");
        Cotizacion cotizacion = Cotizacion.nueva("1000001", now).withPersistence(13L, 3L, now);
        ConfiguracionLayout configuracionLayout = new ConfiguracionLayout(
                21L,
                13L,
                ModoCaptura.MULTIPLE,
                3,
                List.of(
                        new LayoutUbicacionSlot(1, 1),
                        new LayoutUbicacionSlot(2, 2),
                        new LayoutUbicacionSlot(3, 3)
                ),
                now,
                now
        );

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));
        when(configuracionLayoutRepository.findByCotizacionId(13L)).thenReturn(Optional.of(configuracionLayout));

        LocationsLayoutResponse response = useCase.handle("1000001");

        assertEquals("1000001", response.numeroFolio());
        assertEquals(3L, response.version());
        assertEquals(now, response.fechaUltimaActualizacion());
        assertEquals(ModoCaptura.MULTIPLE, response.configuracionLayout().modoCaptura());
        assertEquals(3, response.configuracionLayout().cantidadUbicaciones());
        assertEquals(3, response.configuracionLayout().ubicaciones().size());
        assertEquals(1, response.configuracionLayout().ubicaciones().get(0).indice());
        assertEquals(1, response.configuracionLayout().ubicaciones().get(0).ordenCaptura());

        verify(cotizacionRepository).findByNumeroFolio("1000001");
        verify(configuracionLayoutRepository).findByCotizacionId(13L);
    }

    @Test
    void handle_returnsEmptyLayoutWhenSectionDoesNotExist() {
        GetLocationsLayoutUseCase useCase = new GetLocationsLayoutUseCase(
                cotizacionRepository,
                configuracionLayoutRepository
        );
        Instant now = Instant.parse("2026-04-21T00:00:00Z");
        Cotizacion cotizacion = Cotizacion.nueva("1000001", now).withPersistence(13L, 3L, now);

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));
        when(configuracionLayoutRepository.findByCotizacionId(13L)).thenReturn(Optional.empty());

        LocationsLayoutResponse response = useCase.handle("1000001");

        assertEquals("1000001", response.numeroFolio());
        assertEquals(3L, response.version());
        assertEquals(now, response.fechaUltimaActualizacion());
        assertNull(response.configuracionLayout().modoCaptura());
        assertNull(response.configuracionLayout().cantidadUbicaciones());
        assertEquals(List.of(), response.configuracionLayout().ubicaciones());

        verify(cotizacionRepository).findByNumeroFolio("1000001");
        verify(configuracionLayoutRepository).findByCotizacionId(13L);
    }

    @Test
    void handle_throwsWhenFolioDoesNotExist() {
        GetLocationsLayoutUseCase useCase = new GetLocationsLayoutUseCase(
                cotizacionRepository,
                configuracionLayoutRepository
        );

        when(cotizacionRepository.findByNumeroFolio("9999999")).thenReturn(Optional.empty());

        QuoteNotFoundException exception = assertThrows(
                QuoteNotFoundException.class,
                () -> useCase.handle("9999999")
        );

        assertEquals("No existe una cotizacion con numeroFolio 9999999", exception.getMessage());
        verify(cotizacionRepository).findByNumeroFolio("9999999");
        verifyNoInteractions(configuracionLayoutRepository);
    }
}