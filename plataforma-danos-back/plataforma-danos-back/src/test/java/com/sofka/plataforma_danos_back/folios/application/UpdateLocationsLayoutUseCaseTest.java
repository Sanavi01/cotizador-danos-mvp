package com.sofka.plataforma_danos_back.folios.application;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sofka.plataforma_danos_back.folios.application.dto.UpdateLocationsLayoutRequest;
import com.sofka.plataforma_danos_back.folios.application.exception.LocationsLayoutValidationException;
import com.sofka.plataforma_danos_back.folios.application.exception.LocationsLayoutVersionConflictException;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.ConfiguracionLayout;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.EstadoCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.ModoCaptura;
import com.sofka.plataforma_danos_back.folios.domain.port.ConfiguracionLayoutRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.CotizacionRepository;

@ExtendWith(MockitoExtension.class)
class UpdateLocationsLayoutUseCaseTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-04-21T00:00:00Z");

    @Mock
    private CotizacionRepository cotizacionRepository;

    @Mock
    private ConfiguracionLayoutRepository configuracionLayoutRepository;

    private UpdateLocationsLayoutUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateLocationsLayoutUseCase(
                cotizacionRepository,
                configuracionLayoutRepository,
                Clock.fixed(FIXED_NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void handle_updatesLayoutWhenVersionMatches() {
        Cotizacion cotizacion = Cotizacion.nueva("1000001", FIXED_NOW).withPersistence(13L, 1L, FIXED_NOW);
        UpdateLocationsLayoutRequest request = validRequest(1L);

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));
        when(configuracionLayoutRepository.save(any(ConfiguracionLayout.class))).thenAnswer(invocation -> {
            ConfiguracionLayout layout = invocation.getArgument(0);
            return layout.withPersistence(21L, FIXED_NOW, FIXED_NOW);
        });
        when(cotizacionRepository.save(any(Cotizacion.class))).thenAnswer(invocation -> {
            Cotizacion input = invocation.getArgument(0);
            return input.withPersistence(13L, 2L, FIXED_NOW);
        });

        var response = useCase.handle("1000001", request);

        ArgumentCaptor<ConfiguracionLayout> layoutCaptor = ArgumentCaptor.forClass(ConfiguracionLayout.class);
        ArgumentCaptor<Cotizacion> cotizacionCaptor = ArgumentCaptor.forClass(Cotizacion.class);
        verify(configuracionLayoutRepository).save(layoutCaptor.capture());
        verify(cotizacionRepository).save(cotizacionCaptor.capture());

        assertEquals(13L, layoutCaptor.getValue().cotizacionId());
        assertEquals(ModoCaptura.MULTIPLE, layoutCaptor.getValue().modoCaptura());
        assertEquals(3, layoutCaptor.getValue().cantidadUbicaciones());
        assertEquals(3, layoutCaptor.getValue().ubicaciones().size());
        assertEquals(13L, cotizacionCaptor.getValue().id());
        assertEquals(EstadoCotizacion.EN_CAPTURA, cotizacionCaptor.getValue().estadoCotizacion());
        assertEquals(1L, cotizacionCaptor.getValue().version());
        assertEquals(FIXED_NOW, cotizacionCaptor.getValue().fechaUltimaActualizacion());

        assertEquals("1000001", response.numeroFolio());
        assertEquals(2L, response.version());
        assertEquals(FIXED_NOW, response.fechaUltimaActualizacion());
        assertEquals(ModoCaptura.MULTIPLE, response.configuracionLayout().modoCaptura());
        assertEquals(3, response.configuracionLayout().cantidadUbicaciones());
    }

    @Test
    void handle_throwsVersionConflictWhenVersionDoesNotMatch() {
        Cotizacion cotizacion = Cotizacion.nueva("1000001", FIXED_NOW).withPersistence(13L, 2L, FIXED_NOW);
        UpdateLocationsLayoutRequest request = validRequest(1L);

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));

        LocationsLayoutVersionConflictException exception = assertThrows(
                LocationsLayoutVersionConflictException.class,
                () -> useCase.handle("1000001", request)
        );

        assertEquals("La cotizacion 1000001 tiene version 1 y no coincide con la version actual 2", exception.getMessage());
        verify(cotizacionRepository).findByNumeroFolio("1000001");
        verifyNoInteractions(configuracionLayoutRepository);
    }

    @Test
    void handle_throwsValidationErrorWhenLayoutRulesAreBroken() {
        Cotizacion cotizacion = Cotizacion.nueva("1000001", FIXED_NOW).withPersistence(13L, 1L, FIXED_NOW);
        UpdateLocationsLayoutRequest request = new UpdateLocationsLayoutRequest(
                1L,
                new UpdateLocationsLayoutRequest.ConfiguracionLayoutRequest(
                        ModoCaptura.UNICA,
                        2,
                        List.of(
                                new UpdateLocationsLayoutRequest.LayoutUbicacionSlotRequest(1, 1),
                                new UpdateLocationsLayoutRequest.LayoutUbicacionSlotRequest(2, 2)
                        )
                )
        );

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));

        LocationsLayoutValidationException exception = assertThrows(
                LocationsLayoutValidationException.class,
                () -> useCase.handle("1000001", request)
        );

        assertEquals("El modo de captura UNICA requiere exactamente una ubicacion", exception.getMessage());
        verify(cotizacionRepository).findByNumeroFolio("1000001");
        verifyNoInteractions(configuracionLayoutRepository);
    }

    @Test
    void handle_throwsWhenFolioDoesNotExist() {
        UpdateLocationsLayoutRequest request = validRequest(1L);

        when(cotizacionRepository.findByNumeroFolio("9999999")).thenReturn(Optional.empty());

        QuoteNotFoundException exception = assertThrows(
                QuoteNotFoundException.class,
                () -> useCase.handle("9999999", request)
        );

        assertEquals("No existe una cotizacion con numeroFolio 9999999", exception.getMessage());
        verify(cotizacionRepository).findByNumeroFolio("9999999");
        verifyNoInteractions(configuracionLayoutRepository);
    }

    private static UpdateLocationsLayoutRequest validRequest(Long version) {
        return new UpdateLocationsLayoutRequest(
                version,
                new UpdateLocationsLayoutRequest.ConfiguracionLayoutRequest(
                        ModoCaptura.MULTIPLE,
                        3,
                        List.of(
                                new UpdateLocationsLayoutRequest.LayoutUbicacionSlotRequest(1, 1),
                                new UpdateLocationsLayoutRequest.LayoutUbicacionSlotRequest(2, 2),
                                new UpdateLocationsLayoutRequest.LayoutUbicacionSlotRequest(3, 3)
                        )
                )
        );
    }
}