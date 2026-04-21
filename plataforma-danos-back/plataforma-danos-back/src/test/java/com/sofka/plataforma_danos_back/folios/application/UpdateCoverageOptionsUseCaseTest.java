package com.sofka.plataforma_danos_back.folios.application;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sofka.plataforma_danos_back.folios.application.dto.CoverageOptionsRequest;
import com.sofka.plataforma_danos_back.folios.application.dto.CoverageOptionsResponse;
import com.sofka.plataforma_danos_back.folios.application.exception.CoverageOptionsCatalogValidationException;
import com.sofka.plataforma_danos_back.folios.application.exception.CoverageOptionsVersionConflictException;
import com.sofka.plataforma_danos_back.folios.application.exception.InvalidCoverageOptionsPayloadException;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.CoverageGuaranteeDefinition;
import com.sofka.plataforma_danos_back.folios.domain.CoverageGuaranteePreview;
import com.sofka.plataforma_danos_back.folios.domain.CoverageOptions;
import com.sofka.plataforma_danos_back.folios.domain.CoverageProjectionPerLocation;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.SelectedGuarantee;
import com.sofka.plataforma_danos_back.folios.domain.TechnicalPreviewSource;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionDetalle;
import com.sofka.plataforma_danos_back.folios.domain.port.CotizacionRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.CoverageGuaranteeCatalogPort;
import com.sofka.plataforma_danos_back.folios.domain.port.CoverageOptionsRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.UbicacionCotizacionRepository;

@ExtendWith(MockitoExtension.class)
class UpdateCoverageOptionsUseCaseTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-04-21T00:00:00Z");

    @Mock
    private CotizacionRepository cotizacionRepository;

    @Mock
    private CoverageOptionsRepository coverageOptionsRepository;

    @Mock
    private UbicacionCotizacionRepository ubicacionCotizacionRepository;

    @Mock
    private CoverageGuaranteeCatalogPort coverageGuaranteeCatalogPort;

    @Mock
    private CoverageOptionsProjectionService coverageOptionsProjectionService;

    private UpdateCoverageOptionsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateCoverageOptionsUseCase(
                cotizacionRepository,
                coverageOptionsRepository,
                ubicacionCotizacionRepository,
                coverageGuaranteeCatalogPort,
                coverageOptionsProjectionService,
                Clock.fixed(FIXED_NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void handle_updatesCoverageOptionsWhenVersionMatches() {
        Cotizacion cotizacion = Cotizacion.nueva("1000001", FIXED_NOW).withPersistence(13L, 1L, FIXED_NOW);
        UbicacionCotizacion ubicacion = new UbicacionCotizacion(
                91L,
                13L,
                new UbicacionDetalle(
                        1,
                        "Planta principal",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "CONCRETO",
                        1,
                        null,
                        null,
                        null
                ),
                null,
                List.of(),
                FIXED_NOW,
                FIXED_NOW
        );
        CoverageOptionsRequest request = validRequest(1L);
        CoverageOptions expectedCoverageOptions = new CoverageOptions(
                13L,
                List.of(
                        new SelectedGuarantee("GAR-INC-ED", List.of("base")),
                        new SelectedGuarantee("GAR-ROBO", List.of())
                ),
                "Cobertura base para el analisis inicial"
        );
        List<CoverageProjectionPerLocation> expectedProjection = List.of(new CoverageProjectionPerLocation(
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
        when(coverageGuaranteeCatalogPort.findByCode("GAR-INC-ED")).thenReturn(Optional.of(new CoverageGuaranteeDefinition("GAR-INC-ED", true, TechnicalPreviewSource.CORE_TARIFF)));
        when(coverageGuaranteeCatalogPort.findByCode("GAR-ROBO")).thenReturn(Optional.of(new CoverageGuaranteeDefinition("GAR-ROBO", true, TechnicalPreviewSource.CAT_TARIFF)));
        when(coverageOptionsRepository.save(any(CoverageOptions.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cotizacionRepository.save(any(Cotizacion.class))).thenReturn(cotizacion.withPersistence(13L, 2L, FIXED_NOW));
        when(ubicacionCotizacionRepository.findAllByCotizacionId(13L)).thenReturn(List.of(ubicacion));
        when(coverageOptionsProjectionService.buildProjection(List.of(ubicacion), expectedCoverageOptions.garantiasSeleccionadas())).thenReturn(expectedProjection);

        CoverageOptionsResponse response = useCase.handle("1000001", request);

        ArgumentCaptor<Cotizacion> cotizacionCaptor = ArgumentCaptor.forClass(Cotizacion.class);
        ArgumentCaptor<CoverageOptions> coverageOptionsCaptor = ArgumentCaptor.forClass(CoverageOptions.class);
        verify(cotizacionRepository).save(cotizacionCaptor.capture());
        verify(coverageOptionsRepository).save(coverageOptionsCaptor.capture());

        assertEquals(13L, cotizacionCaptor.getValue().id());
        assertEquals(1L, cotizacionCaptor.getValue().version());
        assertEquals(FIXED_NOW, cotizacionCaptor.getValue().fechaUltimaActualizacion());
        assertEquals(13L, coverageOptionsCaptor.getValue().cotizacionId());
        assertEquals("GAR-INC-ED", coverageOptionsCaptor.getValue().garantiasSeleccionadas().get(0).garantiaCode());
        assertEquals(List.of("base"), coverageOptionsCaptor.getValue().garantiasSeleccionadas().get(0).terminos());
        assertEquals("GAR-ROBO", coverageOptionsCaptor.getValue().garantiasSeleccionadas().get(1).garantiaCode());

        assertEquals("1000001", response.numeroFolio());
        assertEquals(2L, response.version());
        assertEquals("GAR-INC-ED", response.opcionesCobertura().garantiasSeleccionadas().get(0).garantiaCode());
        assertEquals(List.of("base"), response.opcionesCobertura().garantiasSeleccionadas().get(0).terminos());
        assertEquals(1, response.projectionPerLocation().size());
        assertTrue(response.projectionPerLocation().get(0).calculablePreview());
    }

    @Test
    void handle_savesEmptyCoverageOptionsWhenNoGuaranteesAreSelected() {
        Cotizacion cotizacion = Cotizacion.nueva("1000001", FIXED_NOW).withPersistence(13L, 1L, FIXED_NOW);
        CoverageOptionsRequest request = new CoverageOptionsRequest(1L, new CoverageOptionsRequest.CoverageOptionsView(List.of(), null));

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));
        when(coverageOptionsRepository.save(any(CoverageOptions.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cotizacionRepository.save(any(Cotizacion.class))).thenReturn(cotizacion.withPersistence(13L, 2L, FIXED_NOW));

        CoverageOptionsResponse response = useCase.handle("1000001", request);

        ArgumentCaptor<CoverageOptions> coverageOptionsCaptor = ArgumentCaptor.forClass(CoverageOptions.class);
        verify(coverageOptionsRepository).save(coverageOptionsCaptor.capture());
        assertEquals(13L, coverageOptionsCaptor.getValue().cotizacionId());
        assertTrue(coverageOptionsCaptor.getValue().garantiasSeleccionadas().isEmpty());
        assertEquals(0, response.opcionesCobertura().garantiasSeleccionadas().size());
        assertEquals(0, response.projectionPerLocation().size());
        verifyNoInteractions(coverageGuaranteeCatalogPort, ubicacionCotizacionRepository, coverageOptionsProjectionService);
    }

    @Test
    void handle_throwsWhenFolioDoesNotExist() {
        CoverageOptionsRequest request = validRequest(1L);
        when(cotizacionRepository.findByNumeroFolio("9999999")).thenReturn(Optional.empty());

        QuoteNotFoundException exception = assertThrows(
                QuoteNotFoundException.class,
                () -> useCase.handle("9999999", request)
        );

        assertEquals("No existe una cotizacion con numeroFolio 9999999", exception.getMessage());
        verify(cotizacionRepository).findByNumeroFolio("9999999");
        verifyNoInteractions(coverageOptionsRepository, ubicacionCotizacionRepository, coverageGuaranteeCatalogPort, coverageOptionsProjectionService);
    }

    @Test
    void handle_throwsVersionConflictWhenVersionDoesNotMatch() {
        Cotizacion cotizacion = Cotizacion.nueva("1000001", FIXED_NOW).withPersistence(13L, 2L, FIXED_NOW);
        CoverageOptionsRequest request = validRequest(1L);

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));

        CoverageOptionsVersionConflictException exception = assertThrows(
                CoverageOptionsVersionConflictException.class,
                () -> useCase.handle("1000001", request)
        );

        assertEquals("La version de opcionesCobertura para el folio 1000001 no coincide. Solicitada: 1, vigente: 2", exception.getMessage());
        verify(cotizacionRepository).findByNumeroFolio("1000001");
        verifyNoInteractions(coverageOptionsRepository, ubicacionCotizacionRepository, coverageGuaranteeCatalogPort, coverageOptionsProjectionService);
    }

    @Test
    void handle_throwsWhenGuaranteesAreDuplicated() {
        Cotizacion cotizacion = Cotizacion.nueva("1000001", FIXED_NOW).withPersistence(13L, 1L, FIXED_NOW);
        CoverageOptionsRequest request = new CoverageOptionsRequest(
                1L,
                new CoverageOptionsRequest.CoverageOptionsView(
                        List.of(
                                new CoverageOptionsRequest.SelectedGuaranteeRequest("GAR-INC-ED", List.of()),
                                new CoverageOptionsRequest.SelectedGuaranteeRequest("GAR-INC-ED", List.of())
                        ),
                        null
                )
        );

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));

        InvalidCoverageOptionsPayloadException exception = assertThrows(
                InvalidCoverageOptionsPayloadException.class,
                () -> useCase.handle("1000001", request)
        );

        assertEquals("La solicitud contiene garantias duplicadas: [GAR-INC-ED]", exception.getMessage());
        verify(cotizacionRepository).findByNumeroFolio("1000001");
        verify(cotizacionRepository, never()).save(any(Cotizacion.class));
        verify(coverageOptionsRepository, never()).save(any(CoverageOptions.class));
        verifyNoInteractions(ubicacionCotizacionRepository, coverageGuaranteeCatalogPort, coverageOptionsProjectionService);
    }

    @Test
    void handle_throwsWhenGuaranteeIsNotInCatalog() {
        Cotizacion cotizacion = Cotizacion.nueva("1000001", FIXED_NOW).withPersistence(13L, 1L, FIXED_NOW);
        CoverageOptionsRequest request = new CoverageOptionsRequest(
                1L,
                new CoverageOptionsRequest.CoverageOptionsView(
                        List.of(
                                new CoverageOptionsRequest.SelectedGuaranteeRequest("GAR-INC-ED", List.of()),
                                new CoverageOptionsRequest.SelectedGuaranteeRequest("GAR-INACTIVA", List.of())
                        ),
                        null
                )
        );

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));
        when(coverageGuaranteeCatalogPort.findByCode("GAR-INC-ED")).thenReturn(Optional.of(new CoverageGuaranteeDefinition("GAR-INC-ED", true, TechnicalPreviewSource.CORE_TARIFF)));
        when(coverageGuaranteeCatalogPort.findByCode("GAR-INACTIVA")).thenReturn(Optional.of(new CoverageGuaranteeDefinition("GAR-INACTIVA", false, TechnicalPreviewSource.UNRESOLVED)));

        CoverageOptionsCatalogValidationException exception = assertThrows(
                CoverageOptionsCatalogValidationException.class,
                () -> useCase.handle("1000001", request)
        );

        assertEquals("Las garantias no estan activas o no existen en el catalogo aprobado: [GAR-INACTIVA]", exception.getMessage());
        verify(cotizacionRepository).findByNumeroFolio("1000001");
        verify(coverageGuaranteeCatalogPort).findByCode("GAR-INC-ED");
        verify(coverageGuaranteeCatalogPort).findByCode("GAR-INACTIVA");
        verify(cotizacionRepository, never()).save(any(Cotizacion.class));
        verify(coverageOptionsRepository, never()).save(any(CoverageOptions.class));
        verifyNoInteractions(ubicacionCotizacionRepository, coverageOptionsProjectionService);
    }

    private static CoverageOptionsRequest validRequest(Long version) {
        return new CoverageOptionsRequest(
                version,
                new CoverageOptionsRequest.CoverageOptionsView(
                        List.of(
                                new CoverageOptionsRequest.SelectedGuaranteeRequest("gar-inc-ed", List.of(" base ")),
                                new CoverageOptionsRequest.SelectedGuaranteeRequest("GAR-ROBO", List.of())
                        ),
                        "  Cobertura base para el analisis inicial  "
                )
        );
    }
}