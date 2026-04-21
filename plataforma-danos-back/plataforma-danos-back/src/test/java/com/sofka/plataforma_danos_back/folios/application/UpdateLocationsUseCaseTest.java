package com.sofka.plataforma_danos_back.folios.application;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyCollection;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sofka.plataforma_danos_back.folios.application.dto.LocationsResponse;
import com.sofka.plataforma_danos_back.folios.application.dto.UpdateLocationsRequest;
import com.sofka.plataforma_danos_back.folios.application.exception.InvalidLocationsPayloadException;
import com.sofka.plataforma_danos_back.folios.application.exception.LocationsLayoutValidationException;
import com.sofka.plataforma_danos_back.folios.application.exception.LocationsLayoutVersionConflictException;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.AlertaBloqueante;
import com.sofka.plataforma_danos_back.folios.domain.ConfiguracionLayout;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.EstadoCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.EstadoValidacion;
import com.sofka.plataforma_danos_back.folios.domain.Giro;
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
class UpdateLocationsUseCaseTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-04-21T00:00:00Z");

    @Mock
    private CotizacionRepository cotizacionRepository;

    @Mock
    private ConfiguracionLayoutRepository configuracionLayoutRepository;

    @Mock
    private UbicacionCotizacionRepository ubicacionCotizacionRepository;

    @Mock
    private LocationEvaluationService locationEvaluationService;

    private UpdateLocationsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateLocationsUseCase(
                cotizacionRepository,
                configuracionLayoutRepository,
                ubicacionCotizacionRepository,
                locationEvaluationService,
                Clock.fixed(FIXED_NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void handle_updatesLocationsWhenVersionMatchesAndLayoutExists() {
        Cotizacion cotizacion = Cotizacion.nueva("1000001", FIXED_NOW).withPersistence(13L, 3L, FIXED_NOW);
        ConfiguracionLayout layout = layout();
        UpdateLocationsRequest request = validRequest();

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));
        when(configuracionLayoutRepository.findByCotizacionId(13L)).thenReturn(Optional.of(layout));
        when(ubicacionCotizacionRepository.findAllByCotizacionId(13L)).thenReturn(List.of(existingLocation(3)));
        when(locationEvaluationService.evaluate(any(UbicacionDetalle.class))).thenAnswer(invocation -> {
            UbicacionDetalle detalle = invocation.getArgument(0);
            if (detalle.indice() == 1) {
                return new UbicacionEvaluacion(
                        new UbicacionDetalle(
                                1,
                                detalle.nombreUbicacion(),
                                detalle.direccion(),
                                detalle.codigoPostal(),
                                "Bogota D.C.",
                                "Bogota",
                                "Chapinero",
                                "Bogota",
                                detalle.tipoConstructivo(),
                                detalle.nivel(),
                                detalle.anioConstruccion(),
                                detalle.giro(),
                                new ZonaCatastrofica("Z-TEV-01", "Z-FHM-01")
                        ),
                        EstadoValidacion.CALCULABLE,
                        List.of()
                );
            }
            return new UbicacionEvaluacion(
                    new UbicacionDetalle(
                            2,
                            detalle.nombreUbicacion(),
                            detalle.direccion(),
                            detalle.codigoPostal(),
                            "Antioquia",
                            "Medellin",
                            detalle.colonia(),
                            "Medellin",
                            detalle.tipoConstructivo(),
                            detalle.nivel(),
                            detalle.anioConstruccion(),
                            detalle.giro(),
                            new ZonaCatastrofica("Z-TEV-02", "Z-FHM-01")
                    ),
                    EstadoValidacion.INVALID,
                    List.of(new AlertaBloqueante("UBICACION_SIN_ZIP", "La ubicacion no tiene codigo postal valido.", "Warning"))
            );
        });
        when(ubicacionCotizacionRepository.saveAll(anyList())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            List<UbicacionCotizacion> ubicaciones = invocation.getArgument(0);
            return ubicaciones.stream()
                    .map(ubicacion -> ubicacion.withPersistence(100L + ubicacion.detalle().indice(), FIXED_NOW, FIXED_NOW))
                    .toList();
        });
        when(cotizacionRepository.save(any(Cotizacion.class))).thenAnswer(invocation -> {
            Cotizacion input = invocation.getArgument(0);
            return input.withPersistence(13L, 4L, FIXED_NOW);
        });

        LocationsResponse response = useCase.handle("1000001", request);

        ArgumentCaptor<List<UbicacionCotizacion>> locationsCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Cotizacion> cotizacionCaptor = ArgumentCaptor.forClass(Cotizacion.class);
        ArgumentCaptor<Collection<Integer>> indicesCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(ubicacionCotizacionRepository).deleteByCotizacionIdAndIndiceNotIn(eq(13L), indicesCaptor.capture());
        verify(ubicacionCotizacionRepository).saveAll(locationsCaptor.capture());
        verify(cotizacionRepository).save(cotizacionCaptor.capture());

        assertEquals(2, indicesCaptor.getValue().size());
        assertEquals(true, indicesCaptor.getValue().contains(1));
        assertEquals(true, indicesCaptor.getValue().contains(2));

        assertEquals(13L, cotizacionCaptor.getValue().id());
        assertEquals(EstadoCotizacion.EN_CAPTURA, cotizacionCaptor.getValue().estadoCotizacion());
        assertEquals(3L, cotizacionCaptor.getValue().version());
        assertEquals(FIXED_NOW, cotizacionCaptor.getValue().fechaUltimaActualizacion());

        assertEquals(2, locationsCaptor.getValue().size());
        assertEquals(1, locationsCaptor.getValue().get(0).detalle().indice());
        assertEquals(EstadoValidacion.CALCULABLE, locationsCaptor.getValue().get(0).estadoValidacion());
        assertEquals(2, locationsCaptor.getValue().get(1).detalle().indice());
        assertEquals(EstadoValidacion.INVALID, locationsCaptor.getValue().get(1).estadoValidacion());

        assertEquals("1000001", response.numeroFolio());
        assertEquals(4L, response.version());
        assertEquals(2, response.ubicaciones().size());
        assertEquals("CALCULABLE", response.ubicaciones().get(0).estadoValidacion().name());
        assertEquals("INVALID", response.ubicaciones().get(1).estadoValidacion().name());
    }

    @Test
    void handle_throwsVersionConflictWhenVersionDoesNotMatch() {
        Cotizacion cotizacion = Cotizacion.nueva("1000001", FIXED_NOW).withPersistence(13L, 2L, FIXED_NOW);

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));

        LocationsLayoutVersionConflictException exception = assertThrows(
                LocationsLayoutVersionConflictException.class,
                () -> useCase.handle("1000001", validRequest())
        );

        assertEquals("La cotizacion 1000001 tiene version 3 y no coincide con la version actual 2", exception.getMessage());
        verify(cotizacionRepository).findByNumeroFolio("1000001");
        verifyNoInteractions(configuracionLayoutRepository, ubicacionCotizacionRepository, locationEvaluationService);
    }

    @Test
    void handle_throwsWhenFolioDoesNotExist() {
        when(cotizacionRepository.findByNumeroFolio("9999999")).thenReturn(Optional.empty());

        QuoteNotFoundException exception = assertThrows(
                QuoteNotFoundException.class,
                () -> useCase.handle("9999999", validRequest())
        );

        assertEquals("No existe una cotizacion con numeroFolio 9999999", exception.getMessage());
        verify(cotizacionRepository).findByNumeroFolio("9999999");
        verifyNoInteractions(configuracionLayoutRepository, ubicacionCotizacionRepository, locationEvaluationService);
    }

    @Test
    void handle_throwsWhenLayoutDoesNotExist() {
        Cotizacion cotizacion = Cotizacion.nueva("1000001", FIXED_NOW).withPersistence(13L, 3L, FIXED_NOW);

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));
        when(configuracionLayoutRepository.findByCotizacionId(13L)).thenReturn(Optional.empty());

        LocationsLayoutValidationException exception = assertThrows(
                LocationsLayoutValidationException.class,
                () -> useCase.handle("1000001", validRequest())
        );

        assertEquals("La cotizacion no tiene configuracionLayout registrado", exception.getMessage());
        verify(cotizacionRepository).findByNumeroFolio("1000001");
        verify(configuracionLayoutRepository).findByCotizacionId(13L);
        verifyNoInteractions(ubicacionCotizacionRepository, locationEvaluationService);
    }

    @Test
    void handle_throwsWhenRequestContainsDuplicateIndices() {
        Cotizacion cotizacion = Cotizacion.nueva("1000001", FIXED_NOW).withPersistence(13L, 3L, FIXED_NOW);

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));
        when(configuracionLayoutRepository.findByCotizacionId(13L)).thenReturn(Optional.of(layout()));

        InvalidLocationsPayloadException exception = assertThrows(
                InvalidLocationsPayloadException.class,
                () -> useCase.handle("1000001", duplicateIndicesRequest())
        );

        assertEquals("No se permiten indices duplicados en la solicitud", exception.getMessage());
        verify(cotizacionRepository).findByNumeroFolio("1000001");
        verify(configuracionLayoutRepository).findByCotizacionId(13L);
        verifyNoInteractions(ubicacionCotizacionRepository, locationEvaluationService);
    }

    @Test
    void handle_throwsWhenRequestContainsIndexOutsideLayout() {
        Cotizacion cotizacion = Cotizacion.nueva("1000001", FIXED_NOW).withPersistence(13L, 3L, FIXED_NOW);

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));
        when(configuracionLayoutRepository.findByCotizacionId(13L)).thenReturn(Optional.of(layout()));

        LocationsLayoutValidationException exception = assertThrows(
                LocationsLayoutValidationException.class,
                () -> useCase.handle("1000001", outOfRangeRequest())
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

    private static UpdateLocationsRequest validRequest() {
        return new UpdateLocationsRequest(
                3L,
                List.of(
                        new UpdateLocationsRequest.LocationUpsertRequest(
                                1,
                                "Planta principal",
                                "Calle 100 # 10-10",
                                "110111",
                                null,
                                null,
                                null,
                                null,
                                "CONCRETO",
                                1,
                                2018,
                                new UpdateLocationsRequest.GiroRequest("GIRO-001", "Manufactura ligera", "CI-001"),
                                null
                        ),
                        new UpdateLocationsRequest.LocationUpsertRequest(
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
                        )
                )
        );
    }

    private static UpdateLocationsRequest duplicateIndicesRequest() {
        return new UpdateLocationsRequest(
                3L,
                List.of(
                        new UpdateLocationsRequest.LocationUpsertRequest(1, "Planta", null, "110111", null, null, null, null, null, null, null, null, null),
                        new UpdateLocationsRequest.LocationUpsertRequest(1, "Bodega", null, "050001", null, null, null, null, null, null, null, null, null)
                )
        );
    }

    private static UpdateLocationsRequest outOfRangeRequest() {
        return new UpdateLocationsRequest(
                3L,
                List.of(
                        new UpdateLocationsRequest.LocationUpsertRequest(4, "Fuera de rango", null, "110111", null, null, null, null, null, null, null, null, null)
                )
        );
    }

    private static UbicacionCotizacion existingLocation(int indice) {
        return new UbicacionCotizacion(
                (long) indice,
                13L,
                new UbicacionDetalle(
                        indice,
                        "Existente",
                        null,
                        "110111",
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
                EstadoValidacion.EMPTY,
                List.of(),
                FIXED_NOW,
                FIXED_NOW
        );
    }
}