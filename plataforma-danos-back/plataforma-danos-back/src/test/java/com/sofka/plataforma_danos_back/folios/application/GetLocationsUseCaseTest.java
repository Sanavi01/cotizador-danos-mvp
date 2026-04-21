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

import com.sofka.plataforma_danos_back.folios.application.dto.LocationsResponse;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.AlertaBloqueante;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.EstadoValidacion;
import com.sofka.plataforma_danos_back.folios.domain.Giro;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionDetalle;
import com.sofka.plataforma_danos_back.folios.domain.ZonaCatastrofica;
import com.sofka.plataforma_danos_back.folios.domain.port.CotizacionRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.UbicacionCotizacionRepository;

@ExtendWith(MockitoExtension.class)
class GetLocationsUseCaseTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-04-21T00:00:00Z");

    @Mock
    private CotizacionRepository cotizacionRepository;

    @Mock
    private UbicacionCotizacionRepository ubicacionCotizacionRepository;

    @Test
    void handle_returnsLocationsWhenFolioExists() {
        GetLocationsUseCase useCase = new GetLocationsUseCase(cotizacionRepository, ubicacionCotizacionRepository);
        Cotizacion cotizacion = Cotizacion.nueva("1000001", FIXED_NOW).withPersistence(13L, 3L, FIXED_NOW);
        List<UbicacionCotizacion> ubicaciones = List.of(validLocation(1), invalidLocation(2));

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));
        when(ubicacionCotizacionRepository.findAllByCotizacionId(13L)).thenReturn(ubicaciones);

        LocationsResponse response = useCase.handle("1000001");

        assertEquals("1000001", response.numeroFolio());
        assertEquals(3L, response.version());
        assertEquals(FIXED_NOW, response.fechaUltimaActualizacion());
        assertEquals(2, response.ubicaciones().size());
        assertEquals(1, response.ubicaciones().get(0).indice());
        assertEquals(2, response.ubicaciones().get(1).indice());
        assertEquals("CALCULABLE", response.ubicaciones().get(0).estadoValidacion().name());
        assertEquals("INVALID", response.ubicaciones().get(1).estadoValidacion().name());

        verify(cotizacionRepository).findByNumeroFolio("1000001");
        verify(ubicacionCotizacionRepository).findAllByCotizacionId(13L);
    }

    @Test
    void handle_throwsWhenFolioDoesNotExist() {
        GetLocationsUseCase useCase = new GetLocationsUseCase(cotizacionRepository, ubicacionCotizacionRepository);

        when(cotizacionRepository.findByNumeroFolio("9999999")).thenReturn(Optional.empty());

        QuoteNotFoundException exception = assertThrows(
                QuoteNotFoundException.class,
                () -> useCase.handle("9999999")
        );

        assertEquals("No existe una cotizacion con numeroFolio 9999999", exception.getMessage());
        verify(cotizacionRepository).findByNumeroFolio("9999999");
        verifyNoInteractions(ubicacionCotizacionRepository);
    }

    private static UbicacionCotizacion validLocation(int indice) {
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
                        new ZonaCatastrofica("Z-TEV-01", "Z-FHM-01")
                ),
                EstadoValidacion.CALCULABLE,
                List.of(),
                FIXED_NOW,
                FIXED_NOW
        );
    }

    private static UbicacionCotizacion invalidLocation(int indice) {
        return new UbicacionCotizacion(
                20L,
                13L,
                new UbicacionDetalle(
                        indice,
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