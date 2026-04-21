package com.sofka.plataforma_danos_back.folios.application;

import java.time.Instant;
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

import com.sofka.plataforma_danos_back.folios.application.dto.GeneralInfoResponse;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.DatosGeneralesCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.port.CotizacionRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.DatosGeneralesCotizacionRepository;

@ExtendWith(MockitoExtension.class)
class GetGeneralInfoUseCaseTest {

    @Mock
    private CotizacionRepository cotizacionRepository;

    @Mock
    private DatosGeneralesCotizacionRepository datosGeneralesCotizacionRepository;

    @Test
    void handle_returnsPersistedGeneralInfoWhenSectionExists() {
        GetGeneralInfoUseCase useCase = new GetGeneralInfoUseCase(
                cotizacionRepository,
                datosGeneralesCotizacionRepository
        );
        Instant now = Instant.parse("2026-04-21T00:00:00Z");
        Cotizacion cotizacion = Cotizacion.nueva("1000001", now).withPersistence(13L, 3L, now);
        DatosGeneralesCotizacion datosGeneralesCotizacion = new DatosGeneralesCotizacion(
                13L,
                new DatosGeneralesCotizacion.DatosAsegurado(
                        "NIT",
                        "900123456",
                        "ACME SAS",
                        "contacto@acme.com",
                        "6015550101"
                ),
                new DatosGeneralesCotizacion.DatosConduccion("AG-102", "RISK-A", "GIRO-001"),
                now,
                now
        );

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));
        when(datosGeneralesCotizacionRepository.findByCotizacionId(13L)).thenReturn(Optional.of(datosGeneralesCotizacion));

        GeneralInfoResponse response = useCase.handle("1000001");

        assertEquals("1000001", response.numeroFolio());
        assertEquals(3L, response.version());
        assertEquals(now, response.fechaUltimaActualizacion());
        assertEquals("NIT", response.datosAsegurado().tipoDocumento());
        assertEquals("900123456", response.datosAsegurado().numeroDocumento());
        assertEquals("ACME SAS", response.datosAsegurado().nombreORazonSocial());
        assertEquals("contacto@acme.com", response.datosAsegurado().correoElectronico());
        assertEquals("6015550101", response.datosAsegurado().telefono());
        assertEquals("AG-102", response.datosConduccion().codigoAgente());
        assertEquals("RISK-A", response.datosConduccion().clasificacionRiesgo());
        assertEquals("GIRO-001", response.datosConduccion().tipoNegocio());

        verify(cotizacionRepository).findByNumeroFolio("1000001");
        verify(datosGeneralesCotizacionRepository).findByCotizacionId(13L);
    }

    @Test
    void handle_returnsEmptyGeneralInfoWhenSectionDoesNotExist() {
        GetGeneralInfoUseCase useCase = new GetGeneralInfoUseCase(
                cotizacionRepository,
                datosGeneralesCotizacionRepository
        );
        Instant now = Instant.parse("2026-04-21T00:00:00Z");
        Cotizacion cotizacion = Cotizacion.nueva("1000001", now).withPersistence(13L, 3L, now);

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));
        when(datosGeneralesCotizacionRepository.findByCotizacionId(13L)).thenReturn(Optional.empty());

        GeneralInfoResponse response = useCase.handle("1000001");

        assertEquals("1000001", response.numeroFolio());
        assertEquals(3L, response.version());
        assertEquals(now, response.fechaUltimaActualizacion());
        assertNull(response.datosAsegurado().tipoDocumento());
        assertNull(response.datosAsegurado().numeroDocumento());
        assertNull(response.datosAsegurado().nombreORazonSocial());
        assertNull(response.datosAsegurado().correoElectronico());
        assertNull(response.datosAsegurado().telefono());
        assertNull(response.datosConduccion().codigoAgente());
        assertNull(response.datosConduccion().clasificacionRiesgo());
        assertNull(response.datosConduccion().tipoNegocio());

        verify(cotizacionRepository).findByNumeroFolio("1000001");
        verify(datosGeneralesCotizacionRepository).findByCotizacionId(13L);
    }

    @Test
    void handle_throwsWhenFolioDoesNotExist() {
        GetGeneralInfoUseCase useCase = new GetGeneralInfoUseCase(
                cotizacionRepository,
                datosGeneralesCotizacionRepository
        );

        when(cotizacionRepository.findByNumeroFolio("9999999")).thenReturn(Optional.empty());

        QuoteNotFoundException exception = assertThrows(
                QuoteNotFoundException.class,
                () -> useCase.handle("9999999")
        );

        assertEquals("No existe una cotizacion con numeroFolio 9999999", exception.getMessage());
        verify(cotizacionRepository).findByNumeroFolio("9999999");
        verifyNoInteractions(datosGeneralesCotizacionRepository);
    }
}