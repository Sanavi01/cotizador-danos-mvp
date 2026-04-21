package com.sofka.plataforma_danos_back.folios.application;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sofka.plataforma_danos_back.folios.application.dto.CoverageOptionsResponse;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.CoverageGuaranteePreview;
import com.sofka.plataforma_danos_back.folios.domain.CoverageOptions;
import com.sofka.plataforma_danos_back.folios.domain.CoverageProjectionPerLocation;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.SelectedGuarantee;
import com.sofka.plataforma_danos_back.folios.domain.TechnicalPreviewSource;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionDetalle;
import com.sofka.plataforma_danos_back.folios.domain.port.CotizacionRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.CoverageOptionsRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.UbicacionCotizacionRepository;

@ExtendWith(MockitoExtension.class)
class GetCoverageOptionsUseCaseTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-04-21T00:00:00Z");

    @Mock
    private CotizacionRepository cotizacionRepository;

    @Mock
    private CoverageOptionsRepository coverageOptionsRepository;

    @Mock
    private UbicacionCotizacionRepository ubicacionCotizacionRepository;

    @Mock
    private CoverageOptionsProjectionService coverageOptionsProjectionService;

    private GetCoverageOptionsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetCoverageOptionsUseCase(
                cotizacionRepository,
                coverageOptionsRepository,
                ubicacionCotizacionRepository,
                coverageOptionsProjectionService
        );
    }

    @Test
    void handle_returnsCoverageOptionsWhenSectionExists() {
        Cotizacion cotizacion = Cotizacion.nueva("1000001", FIXED_NOW).withPersistence(13L, 3L, FIXED_NOW);
        CoverageOptions coverageOptions = new CoverageOptions(
                13L,
                List.of(
                        new SelectedGuarantee("GAR-INC-ED", List.of("base")),
                        new SelectedGuarantee("GAR-ROBO", List.of())
                ),
                "Cobertura base"
        );
        UbicacionCotizacion ubicacion = new UbicacionCotizacion(
                91L,
                13L,
                new UbicacionDetalle(1, "Planta", null, null, null, null, null, null, null, null, null, null, null),
                null,
                List.of(),
                FIXED_NOW,
                FIXED_NOW
        );
        List<CoverageProjectionPerLocation> projection = List.of(new CoverageProjectionPerLocation(
                1,
                List.of(new CoverageGuaranteePreview(
                        "GAR-INC-ED",
                        true,
                        TechnicalPreviewSource.CORE_TARIFF,
                        "GIRO-001|ZTEV-1|GAR-INC-ED",
                        List.of()
                )),
                true
        ));

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));
        when(coverageOptionsRepository.findByCotizacionId(13L)).thenReturn(Optional.of(coverageOptions));
        when(ubicacionCotizacionRepository.findAllByCotizacionId(13L)).thenReturn(List.of(ubicacion));
        when(coverageOptionsProjectionService.buildProjection(List.of(ubicacion), coverageOptions.garantiasSeleccionadas())).thenReturn(projection);

        CoverageOptionsResponse response = useCase.handle("1000001");

        assertEquals("1000001", response.numeroFolio());
        assertEquals(3L, response.version());
        assertEquals(FIXED_NOW, response.fechaUltimaActualizacion());
        assertEquals(2, response.opcionesCobertura().garantiasSeleccionadas().size());
        assertEquals("GAR-INC-ED", response.opcionesCobertura().garantiasSeleccionadas().get(0).garantiaCode());
        assertEquals("Cobertura base", response.opcionesCobertura().observaciones());
        assertEquals(1, response.projectionPerLocation().size());
        assertTrue(response.projectionPerLocation().get(0).calculablePreview());

        verify(cotizacionRepository).findByNumeroFolio("1000001");
        verify(coverageOptionsRepository).findByCotizacionId(13L);
        verify(ubicacionCotizacionRepository).findAllByCotizacionId(13L);
        verify(coverageOptionsProjectionService).buildProjection(List.of(ubicacion), coverageOptions.garantiasSeleccionadas());
    }

    @Test
    void handle_returnsEmptyCoverageOptionsWhenSectionDoesNotExist() {
        Cotizacion cotizacion = Cotizacion.nueva("1000001", FIXED_NOW).withPersistence(13L, 3L, FIXED_NOW);

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));
        when(coverageOptionsRepository.findByCotizacionId(13L)).thenReturn(Optional.empty());

        CoverageOptionsResponse response = useCase.handle("1000001");

        assertEquals("1000001", response.numeroFolio());
        assertEquals(3L, response.version());
        assertTrue(response.opcionesCobertura().garantiasSeleccionadas().isEmpty());
        assertTrue(response.projectionPerLocation().isEmpty());

        verify(cotizacionRepository).findByNumeroFolio("1000001");
        verify(coverageOptionsRepository).findByCotizacionId(13L);
        verify(ubicacionCotizacionRepository, never()).findAllByCotizacionId(13L);
        verify(coverageOptionsProjectionService, never()).buildProjection(List.of(), List.of());
    }

    @Test
    void handle_throwsWhenFolioDoesNotExist() {
        when(cotizacionRepository.findByNumeroFolio("9999999")).thenReturn(Optional.empty());

        QuoteNotFoundException exception = org.junit.jupiter.api.Assertions.assertThrows(
                QuoteNotFoundException.class,
                () -> useCase.handle("9999999")
        );

        assertEquals("No existe una cotizacion con numeroFolio 9999999", exception.getMessage());
        verify(cotizacionRepository).findByNumeroFolio("9999999");
        verifyNoInteractions(coverageOptionsRepository, ubicacionCotizacionRepository, coverageOptionsProjectionService);
    }
}