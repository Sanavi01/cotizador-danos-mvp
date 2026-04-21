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
import static org.mockito.Mockito.doThrow;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sofka.plataforma_danos_back.folios.application.dto.LocationResponse;
import com.sofka.plataforma_danos_back.folios.application.dto.UpdateLocationRequest;
import com.sofka.plataforma_danos_back.folios.application.exception.InvalidLocationsPayloadException;
import com.sofka.plataforma_danos_back.folios.application.exception.LocationNotFoundException;
import com.sofka.plataforma_danos_back.folios.application.exception.LocationVersionConflictException;
import com.sofka.plataforma_danos_back.folios.application.exception.LocationsLayoutValidationException;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.ConfiguracionLayout;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.EstadoCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.EstadoValidacion;
import com.sofka.plataforma_danos_back.folios.domain.LayoutUbicacionSlot;
import com.sofka.plataforma_danos_back.folios.domain.ModoCaptura;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionDetalle;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionEvaluacion;
import com.sofka.plataforma_danos_back.folios.domain.ZonaCatastrofica;
import com.sofka.plataforma_danos_back.folios.domain.port.ConfiguracionLayoutRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.CotizacionRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.UbicacionCotizacionRepository;

@ExtendWith(MockitoExtension.class)
class UpdateLocationUseCaseTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-04-21T00:00:00Z");

    @Mock
    private CotizacionRepository cotizacionRepository;

    @Mock
    private ConfiguracionLayoutRepository configuracionLayoutRepository;

    @Mock
    private UbicacionCotizacionRepository ubicacionCotizacionRepository;

    @Mock
    private LocationEvaluationService locationEvaluationService;

    private UpdateLocationUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateLocationUseCase(
                cotizacionRepository,
                configuracionLayoutRepository,
                ubicacionCotizacionRepository,
                locationEvaluationService,
                Clock.fixed(FIXED_NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void handle_updatesLocationWhenVersionMatches() {
        Cotizacion cotizacion = Cotizacion.nueva("1000001", FIXED_NOW).withPersistence(13L, 3L, FIXED_NOW);
        ConfiguracionLayout layout = layout();
        UbicacionCotizacion existing = existingLocation();
        UpdateLocationRequest request = validRequest();

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));
        when(configuracionLayoutRepository.findByCotizacionId(13L)).thenReturn(Optional.of(layout));
        when(ubicacionCotizacionRepository.findByCotizacionIdAndIndice(13L, 2)).thenReturn(Optional.of(existing));
        when(locationEvaluationService.evaluate(any(UbicacionDetalle.class))).thenAnswer(invocation -> {
            UbicacionDetalle merged = invocation.getArgument(0);
            UbicacionDetalle normalized = new UbicacionDetalle(
                    merged.indice(),
                    merged.nombreUbicacion(),
                    merged.direccion(),
                    merged.codigoPostal(),
                    "Antioquia",
                    "Medellin",
                    merged.colonia(),
                    "Medellin",
                    merged.tipoConstructivo(),
                    merged.nivel(),
                    merged.anioConstruccion(),
                    merged.giro(),
                    new ZonaCatastrofica("Z-TEV-02", "Z-FHM-01")
            );
            return new UbicacionEvaluacion(normalized, EstadoValidacion.VALID, List.of());
        });
        when(ubicacionCotizacionRepository.save(any(UbicacionCotizacion.class))).thenAnswer(invocation -> {
            UbicacionCotizacion ubicacion = invocation.getArgument(0);
            return ubicacion.withPersistence(77L, FIXED_NOW, FIXED_NOW);
        });
        when(cotizacionRepository.save(any(Cotizacion.class))).thenAnswer(invocation -> {
            Cotizacion input = invocation.getArgument(0);
            return input.withPersistence(13L, 4L, FIXED_NOW);
        });

        LocationResponse response = useCase.handle("1000001", 2, request);

        ArgumentCaptor<UbicacionDetalle> detailCaptor = ArgumentCaptor.forClass(UbicacionDetalle.class);
        ArgumentCaptor<UbicacionCotizacion> locationCaptor = ArgumentCaptor.forClass(UbicacionCotizacion.class);
        ArgumentCaptor<Cotizacion> cotizacionCaptor = ArgumentCaptor.forClass(Cotizacion.class);

        verify(locationEvaluationService).evaluate(detailCaptor.capture());
        verify(ubicacionCotizacionRepository).save(locationCaptor.capture());
        verify(cotizacionRepository).save(cotizacionCaptor.capture());

        assertEquals(2, detailCaptor.getValue().indice());
        assertEquals("050001", detailCaptor.getValue().codigoPostal());
        assertEquals("Bodega secundaria", detailCaptor.getValue().nombreUbicacion());
        assertEquals("Bodega", detailCaptor.getValue().giro().nombre());

        assertEquals(77L, locationCaptor.getValue().id());
        assertEquals(EstadoValidacion.VALID, locationCaptor.getValue().estadoValidacion());
        assertEquals("050001", locationCaptor.getValue().detalle().codigoPostal());

        assertEquals(13L, cotizacionCaptor.getValue().id());
        assertEquals(EstadoCotizacion.EN_CAPTURA, cotizacionCaptor.getValue().estadoCotizacion());
        assertEquals(3L, cotizacionCaptor.getValue().version());
        assertEquals(FIXED_NOW, cotizacionCaptor.getValue().fechaUltimaActualizacion());

        assertEquals("1000001", response.numeroFolio());
        assertEquals(4L, response.version());
        assertEquals(2, response.ubicacion().indice());
        assertEquals("VALID", response.ubicacion().estadoValidacion().name());
        assertEquals("Antioquia", response.ubicacion().estado());
        assertEquals("Z-TEV-02", response.ubicacion().zonaCatastrofica().zonaTev());
        assertEquals(FIXED_NOW, response.fechaUltimaActualizacion());
    }

    @Test
    void handle_throwsVersionConflictWhenVersionDoesNotMatch() {
        Cotizacion cotizacion = Cotizacion.nueva("1000001", FIXED_NOW).withPersistence(13L, 4L, FIXED_NOW);

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));

        LocationVersionConflictException exception = assertThrows(
                LocationVersionConflictException.class,
                () -> useCase.handle("1000001", 2, validRequest())
        );

        assertEquals("La cotizacion 1000001 tiene version 3 y no coincide con la version actual 4", exception.getMessage());
        verify(cotizacionRepository).findByNumeroFolio("1000001");
        verifyNoInteractions(configuracionLayoutRepository, ubicacionCotizacionRepository, locationEvaluationService);
    }

    @Test
    void handle_throwsWhenFolioDoesNotExist() {
        when(cotizacionRepository.findByNumeroFolio("9999999")).thenReturn(Optional.empty());

        QuoteNotFoundException exception = assertThrows(
                QuoteNotFoundException.class,
                () -> useCase.handle("9999999", 2, validRequest())
        );

        assertEquals("No existe una cotizacion con numeroFolio 9999999", exception.getMessage());
        verify(cotizacionRepository).findByNumeroFolio("9999999");
        verifyNoInteractions(configuracionLayoutRepository, ubicacionCotizacionRepository, locationEvaluationService);
    }

    @Test
    void handle_throwsWhenLocationDoesNotExist() {
        Cotizacion cotizacion = Cotizacion.nueva("1000001", FIXED_NOW).withPersistence(13L, 3L, FIXED_NOW);

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));
        when(configuracionLayoutRepository.findByCotizacionId(13L)).thenReturn(Optional.of(layout()));
        when(ubicacionCotizacionRepository.findByCotizacionIdAndIndice(13L, 2)).thenReturn(Optional.empty());

        LocationNotFoundException exception = assertThrows(
                LocationNotFoundException.class,
                () -> useCase.handle("1000001", 2, validRequest())
        );

        assertEquals("No existe una ubicacion con indice 2 en la cotizacion 1000001", exception.getMessage());
        verify(cotizacionRepository).findByNumeroFolio("1000001");
        verify(configuracionLayoutRepository).findByCotizacionId(13L);
        verify(ubicacionCotizacionRepository).findByCotizacionIdAndIndice(13L, 2);
        verifyNoInteractions(locationEvaluationService);
    }

    @Test
    void handle_throwsWhenPatchHasNoChanges() {
        Cotizacion cotizacion = Cotizacion.nueva("1000001", FIXED_NOW).withPersistence(13L, 3L, FIXED_NOW);

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));
        when(configuracionLayoutRepository.findByCotizacionId(13L)).thenReturn(Optional.of(layout()));
        when(ubicacionCotizacionRepository.findByCotizacionIdAndIndice(13L, 2)).thenReturn(Optional.of(existingLocation()));
        doThrow(new InvalidLocationsPayloadException("La solicitud no contiene cambios para la ubicacion"))
            .when(locationEvaluationService).validatePatchPayload(false);

        UpdateLocationRequest request = new UpdateLocationRequest(3L, new UpdateLocationRequest.LocationChangesRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
            null
        ));

        InvalidLocationsPayloadException exception = assertThrows(
                InvalidLocationsPayloadException.class,
                () -> useCase.handle("1000001", 2, request)
        );

        assertEquals("La solicitud no contiene cambios para la ubicacion", exception.getMessage());
        verify(cotizacionRepository).findByNumeroFolio("1000001");
        verify(configuracionLayoutRepository).findByCotizacionId(13L);
        verify(ubicacionCotizacionRepository).findByCotizacionIdAndIndice(13L, 2);
        verify(locationEvaluationService).validatePatchPayload(false);
    }

    @Test
    void handle_throwsWhenIndexIsOutsideLayoutRange() {
        Cotizacion cotizacion = Cotizacion.nueva("1000001", FIXED_NOW).withPersistence(13L, 3L, FIXED_NOW);

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));
        when(configuracionLayoutRepository.findByCotizacionId(13L)).thenReturn(Optional.of(layout()));

        LocationsLayoutValidationException exception = assertThrows(
                LocationsLayoutValidationException.class,
                () -> useCase.handle("1000001", 4, validRequest())
        );

        assertEquals("El indice de una ubicacion excede el rango permitido por el layout", exception.getMessage());
        verify(cotizacionRepository).findByNumeroFolio("1000001");
        verify(configuracionLayoutRepository).findByCotizacionId(13L);
        verifyNoInteractions(ubicacionCotizacionRepository, locationEvaluationService);
    }

    private static ConfiguracionLayout layout() {
        return ConfiguracionLayout.nueva(
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
    }

    private static UpdateLocationRequest validRequest() {
        return new UpdateLocationRequest(
                3L,
                new UpdateLocationRequest.LocationChangesRequest(
                        "Bodega secundaria",
                        null,
                        "050001",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        new UpdateLocationRequest.GiroRequest("GIRO-002", "Bodega", "CI-010"),
                        null
                )
        );
    }

    private static UbicacionCotizacion existingLocation() {
        return new UbicacionCotizacion(
                77L,
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
                List.of(),
                FIXED_NOW,
                FIXED_NOW
        );
    }
}