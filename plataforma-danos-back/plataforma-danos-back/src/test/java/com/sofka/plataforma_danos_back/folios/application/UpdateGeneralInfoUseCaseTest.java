package com.sofka.plataforma_danos_back.folios.application;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import com.sofka.plataforma_danos_back.folios.application.dto.GeneralInfoRequest;
import com.sofka.plataforma_danos_back.folios.application.dto.GeneralInfoResponse;
import com.sofka.plataforma_danos_back.folios.application.exception.GeneralInfoCatalogValidationException;
import com.sofka.plataforma_danos_back.folios.application.exception.GeneralInfoVersionConflictException;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.DatosGeneralesCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.port.CotizacionRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.DatosGeneralesCotizacionRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.GeneralInfoCatalogPort;

@ExtendWith(MockitoExtension.class)
class UpdateGeneralInfoUseCaseTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-04-21T00:00:00Z");

    @Mock
    private CotizacionRepository cotizacionRepository;

    @Mock
    private DatosGeneralesCotizacionRepository datosGeneralesCotizacionRepository;

    @Mock
    private GeneralInfoCatalogPort generalInfoCatalogPort;

    private UpdateGeneralInfoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateGeneralInfoUseCase(
                cotizacionRepository,
                datosGeneralesCotizacionRepository,
                generalInfoCatalogPort,
                Clock.fixed(FIXED_NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void handle_updatesGeneralInfoWhenVersionMatches() {
        Cotizacion cotizacion = Cotizacion.nueva("1000001", FIXED_NOW).withPersistence(13L, 1L, FIXED_NOW);
        GeneralInfoRequest request = validRequest(1L);
        DatosGeneralesCotizacion savedGeneralInfo = new DatosGeneralesCotizacion(
                13L,
                request.datosAsegurado().toDomain(),
                request.datosConduccion().toDomain(),
                FIXED_NOW,
                FIXED_NOW
        );
        Cotizacion savedCotizacion = cotizacion.withPersistence(13L, 2L, FIXED_NOW);

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));
        when(generalInfoCatalogPort.isActiveAgent("AG-102")).thenReturn(true);
        when(generalInfoCatalogPort.isActiveRiskClassification("RISK-A")).thenReturn(true);
        when(generalInfoCatalogPort.isActiveBusinessLine("GIRO-001")).thenReturn(true);
        when(datosGeneralesCotizacionRepository.save(any(DatosGeneralesCotizacion.class))).thenReturn(savedGeneralInfo);
        when(cotizacionRepository.save(any(Cotizacion.class))).thenReturn(savedCotizacion);

        GeneralInfoResponse response = useCase.handle("1000001", request);

        ArgumentCaptor<DatosGeneralesCotizacion> datosCaptor = ArgumentCaptor.forClass(DatosGeneralesCotizacion.class);
        ArgumentCaptor<Cotizacion> cotizacionCaptor = ArgumentCaptor.forClass(Cotizacion.class);
        verify(datosGeneralesCotizacionRepository).save(datosCaptor.capture());
        verify(cotizacionRepository).save(cotizacionCaptor.capture());

        assertEquals(13L, datosCaptor.getValue().cotizacionId());
        assertEquals("NIT", datosCaptor.getValue().datosAsegurado().tipoDocumento());
        assertEquals("900123456", datosCaptor.getValue().datosAsegurado().numeroDocumento());
        assertEquals("ACME SAS", datosCaptor.getValue().datosAsegurado().nombreORazonSocial());
        assertEquals("contacto@acme.com", datosCaptor.getValue().datosAsegurado().correoElectronico());
        assertEquals("6015550101", datosCaptor.getValue().datosAsegurado().telefono());
        assertEquals("AG-102", datosCaptor.getValue().datosConduccion().codigoAgente());
        assertEquals("RISK-A", datosCaptor.getValue().datosConduccion().clasificacionRiesgo());
        assertEquals("GIRO-001", datosCaptor.getValue().datosConduccion().tipoNegocio());
        assertEquals(FIXED_NOW, datosCaptor.getValue().createdAt());
        assertEquals(FIXED_NOW, datosCaptor.getValue().updatedAt());

        assertEquals(13L, cotizacionCaptor.getValue().id());
        assertEquals("1000001", cotizacionCaptor.getValue().numeroFolio());
        assertEquals(1L, cotizacionCaptor.getValue().version());
        assertEquals(FIXED_NOW, cotizacionCaptor.getValue().fechaUltimaActualizacion());

        assertEquals("1000001", response.numeroFolio());
        assertEquals(2L, response.version());
        assertEquals(FIXED_NOW, response.fechaUltimaActualizacion());
        assertEquals("ACME SAS", response.datosAsegurado().nombreORazonSocial());
        assertEquals("AG-102", response.datosConduccion().codigoAgente());
    }

    @Test
    void handle_throwsCatalogValidationWhenAgentIsInvalid() {
        Cotizacion cotizacion = Cotizacion.nueva("1000001", FIXED_NOW).withPersistence(13L, 1L, FIXED_NOW);
        GeneralInfoRequest request = validRequest(1L);

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));
        when(generalInfoCatalogPort.isActiveAgent("AG-999")).thenReturn(false);
        when(generalInfoCatalogPort.isActiveRiskClassification("RISK-A")).thenReturn(true);
        when(generalInfoCatalogPort.isActiveBusinessLine("GIRO-001")).thenReturn(true);

        GeneralInfoRequest invalidRequest = new GeneralInfoRequest(
                request.version(),
                request.datosAsegurado(),
                new GeneralInfoRequest.DatosConduccion("AG-999", "RISK-A", "GIRO-001")
        );

        GeneralInfoCatalogValidationException exception = assertThrows(
                GeneralInfoCatalogValidationException.class,
                () -> useCase.handle("1000001", invalidRequest)
        );

        assertEquals("Las referencias de catalogo no son validas: codigoAgente", exception.getMessage());
        verify(cotizacionRepository).findByNumeroFolio("1000001");
        verify(generalInfoCatalogPort).isActiveAgent("AG-999");
        verify(generalInfoCatalogPort).isActiveRiskClassification("RISK-A");
        verify(generalInfoCatalogPort).isActiveBusinessLine("GIRO-001");
        verify(datosGeneralesCotizacionRepository, never()).save(any(DatosGeneralesCotizacion.class));
        verify(cotizacionRepository, never()).save(any(Cotizacion.class));
    }

    @Test
    void handle_throwsVersionConflictWhenVersionDoesNotMatch() {
        Cotizacion cotizacion = Cotizacion.nueva("1000001", FIXED_NOW).withPersistence(13L, 2L, FIXED_NOW);
        GeneralInfoRequest request = validRequest(1L);

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));

        GeneralInfoVersionConflictException exception = assertThrows(
                GeneralInfoVersionConflictException.class,
                () -> useCase.handle("1000001", request)
        );

        assertEquals("La cotizacion 1000001 tiene version 1 y no coincide con la version actual 2", exception.getMessage());
        verify(cotizacionRepository).findByNumeroFolio("1000001");
        verifyNoInteractions(generalInfoCatalogPort, datosGeneralesCotizacionRepository);
    }

    @Test
    void handle_throwsWhenFolioDoesNotExist() {
        GeneralInfoRequest request = validRequest(1L);

        when(cotizacionRepository.findByNumeroFolio("9999999")).thenReturn(Optional.empty());

        QuoteNotFoundException exception = assertThrows(
                QuoteNotFoundException.class,
                () -> useCase.handle("9999999", request)
        );

        assertEquals("No existe una cotizacion con numeroFolio 9999999", exception.getMessage());
        verify(cotizacionRepository).findByNumeroFolio("9999999");
        verifyNoInteractions(generalInfoCatalogPort, datosGeneralesCotizacionRepository);
    }

    private static GeneralInfoRequest validRequest(Long version) {
        return new GeneralInfoRequest(
                version,
                new GeneralInfoRequest.DatosAsegurado(
                        "NIT",
                        "900123456",
                        "ACME SAS",
                        "contacto@acme.com",
                        "6015550101"
                ),
                new GeneralInfoRequest.DatosConduccion("AG-102", "RISK-A", "GIRO-001")
        );
    }
}