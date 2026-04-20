package com.sofka.plataforma_danos_back.folios.application;

import com.sofka.plataforma_danos_back.folios.application.dto.QuoteStateResponse;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.EstadoCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.port.CotizacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetQuoteStateUseCaseTest {

    @Mock
    private CotizacionRepository cotizacionRepository;

    @Test
    void handle_returnsQuoteStateWhenFolioExists() {
        GetQuoteStateUseCase useCase = new GetQuoteStateUseCase(cotizacionRepository);
        Instant now = Instant.parse("2026-04-20T00:00:00Z");
        Cotizacion cotizacion = Cotizacion.nueva("1000001", now).withPersistence(3L, 0L, now);

        when(cotizacionRepository.findByNumeroFolio("1000001")).thenReturn(Optional.of(cotizacion));

        QuoteStateResponse response = useCase.handle("1000001");

        assertEquals("1000001", response.numeroFolio());
        assertEquals(EstadoCotizacion.BORRADOR, response.estadoCotizacion());
        assertEquals(false, response.tieneAlertas());
        assertEquals(0, response.ubicacionesCalculables());
        assertEquals(0, response.ubicacionesIncompletas());
        assertEquals(0L, response.version());
        assertEquals(now, response.fechaUltimaActualizacion());
        verify(cotizacionRepository).findByNumeroFolio("1000001");
    }

    @Test
    void handle_throwsWhenFolioDoesNotExist() {
        GetQuoteStateUseCase useCase = new GetQuoteStateUseCase(cotizacionRepository);

        when(cotizacionRepository.findByNumeroFolio("9999999")).thenReturn(Optional.empty());

        QuoteNotFoundException exception = assertThrows(
                QuoteNotFoundException.class,
                () -> useCase.handle("9999999")
        );

        assertEquals("No existe una cotizacion con numeroFolio 9999999", exception.getMessage());
        verify(cotizacionRepository).findByNumeroFolio("9999999");
    }
}