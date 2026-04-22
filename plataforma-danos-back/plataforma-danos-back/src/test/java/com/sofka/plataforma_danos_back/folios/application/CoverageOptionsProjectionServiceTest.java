package com.sofka.plataforma_danos_back.folios.application;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sofka.plataforma_danos_back.folios.domain.CoverageGuaranteePreview;
import com.sofka.plataforma_danos_back.folios.domain.CoverageProjectionPerLocation;
import com.sofka.plataforma_danos_back.folios.domain.SelectedGuarantee;
import com.sofka.plataforma_danos_back.folios.domain.TechnicalPreviewSource;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionDetalle;
import com.sofka.plataforma_danos_back.folios.domain.port.CoverageGuaranteeCatalogPort;

@ExtendWith(MockitoExtension.class)
class CoverageOptionsProjectionServiceTest {

    @Mock
    private CoverageGuaranteeCatalogPort coverageGuaranteeCatalogPort;

    private CoverageOptionsProjectionService service;

    @BeforeEach
    void setUp() {
        service = new CoverageOptionsProjectionService(coverageGuaranteeCatalogPort);
    }

    @Test
    void buildProjection_returnsEmptyWhenNoLocationsOrGuarantees() {
        List<CoverageProjectionPerLocation> projection = service.buildProjection(List.of(), List.of());

        assertTrue(projection.isEmpty());
        verifyNoInteractions(coverageGuaranteeCatalogPort);
    }

    @Test
    void buildProjection_returnsCalculablePreviewWhenAnyGuaranteeIsTariffable() {
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
                null,
                null
        );
        List<SelectedGuarantee> garantiasSeleccionadas = List.of(
                new SelectedGuarantee("GAR-INC-ED", List.of()),
                new SelectedGuarantee("GAR-ROBO", List.of())
        );

        when(coverageGuaranteeCatalogPort.resolvePreview("GAR-INC-ED", ubicacion)).thenReturn(new CoverageGuaranteePreview(
                "GAR-INC-ED",
                true,
                TechnicalPreviewSource.CORE_TARIFF,
                "GIRO-001|ZTEV-1|GAR-INC-ED",
                List.of()
        ));
        when(coverageGuaranteeCatalogPort.resolvePreview("GAR-ROBO", ubicacion)).thenReturn(new CoverageGuaranteePreview(
                "GAR-ROBO",
                false,
                TechnicalPreviewSource.UNRESOLVED,
                null,
                List.of("No existe tarifa vigente para la combinacion actual de la ubicacion.")
        ));

        List<CoverageProjectionPerLocation> projection = service.buildProjection(List.of(ubicacion), garantiasSeleccionadas);

        assertEquals(1, projection.size());
        assertEquals(1, projection.get(0).indice());
        assertTrue(projection.get(0).calculablePreview());
        assertEquals(2, projection.get(0).garantiasDerivadas().size());
        assertTrue(projection.get(0).garantiasDerivadas().get(0).tariffablePreview());
        assertFalse(projection.get(0).garantiasDerivadas().get(1).tariffablePreview());
    }
}