package com.sofka.plataforma_danos_back.folios.application;

import com.sofka.plataforma_danos_back.folios.application.dto.CreateFolioRequest;
import com.sofka.plataforma_danos_back.folios.application.dto.CreateFolioResponse;
import com.sofka.plataforma_danos_back.folios.application.dto.FolioCreationResult;
import com.sofka.plataforma_danos_back.folios.application.exception.IdempotencyConflictException;
import com.sofka.plataforma_danos_back.folios.application.exception.MissingIdempotencyKeyException;
import com.sofka.plataforma_danos_back.folios.application.idempotency.IdempotencyPayloadCodec;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.EstadoCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.IdempotencyRecord;
import com.sofka.plataforma_danos_back.folios.domain.port.CotizacionRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.FolioSequencePort;
import com.sofka.plataforma_danos_back.folios.domain.port.IdempotencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateFolioUseCaseTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-04-20T00:00:00Z");

    @Mock
    private CotizacionRepository cotizacionRepository;

    @Mock
    private IdempotencyRepository idempotencyRepository;

    @Mock
    private FolioSequencePort folioSequencePort;

    @Mock
    private IdempotencyPayloadCodec idempotencyPayloadCodec;

    private CreateFolioUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateFolioUseCase(
                cotizacionRepository,
                idempotencyRepository,
                folioSequencePort,
                idempotencyPayloadCodec,
                Clock.fixed(FIXED_NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void handle_createsFolioAndStoresIdempotencyRecord() {
        CreateFolioRequest request = CreateFolioRequest.defaultRequest();
        Cotizacion savedCotizacion = Cotizacion.nueva("1000001", FIXED_NOW).withPersistence(7L, 0L, FIXED_NOW);
        CreateFolioResponse response = new CreateFolioResponse("1000001", EstadoCotizacion.BORRADOR, 0L, FIXED_NOW);

        when(idempotencyRepository.findByKey("idem-key")).thenReturn(Optional.empty());
        when(idempotencyPayloadCodec.hashRequest(request)).thenReturn("request-hash");
        when(folioSequencePort.nextNumeroFolio()).thenReturn("1000001");
        when(cotizacionRepository.save(any(Cotizacion.class))).thenReturn(savedCotizacion);
        when(idempotencyPayloadCodec.serializeCreateFolioResponse(response)).thenReturn("{\"numeroFolio\":\"1000001\"}");
        when(idempotencyRepository.save(any(IdempotencyRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FolioCreationResult result = useCase.handle(request, "idem-key");

        assertTrue(result.created());
        assertEquals(response, result.response());

        ArgumentCaptor<IdempotencyRecord> recordCaptor = ArgumentCaptor.forClass(IdempotencyRecord.class);
        verify(idempotencyRepository).save(recordCaptor.capture());
        IdempotencyRecord storedRecord = recordCaptor.getValue();
        assertEquals("idem-key", storedRecord.idempotencyKey());
        assertEquals("request-hash", storedRecord.requestHash());
        assertEquals("{\"numeroFolio\":\"1000001\"}", storedRecord.responsePayload());
        assertEquals(7L, storedRecord.cotizacionId());
        assertEquals("1000001", storedRecord.numeroFolio());
        assertEquals(FIXED_NOW, storedRecord.createdAt());

        verify(cotizacionRepository).save(any(Cotizacion.class));
        verify(folioSequencePort).nextNumeroFolio();
    }

    @Test
    void handle_replaysExistingResponseWhenRequestMatches() {
        CreateFolioRequest request = CreateFolioRequest.defaultRequest();
        CreateFolioResponse response = new CreateFolioResponse("1000001", EstadoCotizacion.BORRADOR, 0L, FIXED_NOW);
        IdempotencyRecord storedRecord = new IdempotencyRecord(
                9L,
                "idem-key",
                "request-hash",
                "{\"numeroFolio\":\"1000001\"}",
                7L,
                "1000001",
                FIXED_NOW
        );

        when(idempotencyRepository.findByKey("idem-key")).thenReturn(Optional.of(storedRecord));
        when(idempotencyPayloadCodec.hashRequest(request)).thenReturn("request-hash");
        when(idempotencyPayloadCodec.deserializeCreateFolioResponse(storedRecord.responsePayload())).thenReturn(response);

        FolioCreationResult result = useCase.handle(request, "idem-key");

        assertFalse(result.created());
        assertEquals(response, result.response());
        verify(idempotencyRepository, never()).save(any(IdempotencyRecord.class));
        verifyNoInteractions(cotizacionRepository, folioSequencePort);
    }

    @Test
    void handle_rejectsDifferentPayloadForSameKey() {
        CreateFolioRequest request = CreateFolioRequest.defaultRequest();
        IdempotencyRecord storedRecord = new IdempotencyRecord(
                9L,
                "idem-key",
                "different-hash",
                "{\"numeroFolio\":\"1000001\"}",
                7L,
                "1000001",
                FIXED_NOW
        );

        when(idempotencyRepository.findByKey("idem-key")).thenReturn(Optional.of(storedRecord));
        when(idempotencyPayloadCodec.hashRequest(request)).thenReturn("request-hash");

        assertThrows(IdempotencyConflictException.class, () -> useCase.handle(request, "idem-key"));
        verify(idempotencyRepository, never()).save(any(IdempotencyRecord.class));
        verifyNoInteractions(cotizacionRepository, folioSequencePort);
    }

    @Test
    void handle_rejectsMissingIdempotencyKey() {
        MissingIdempotencyKeyException exception = assertThrows(
                MissingIdempotencyKeyException.class,
                () -> useCase.handle(CreateFolioRequest.defaultRequest(), null)
        );

        assertEquals("Idempotency-Key es obligatorio para crear folios", exception.getMessage());
        verifyNoInteractions(cotizacionRepository, idempotencyRepository, folioSequencePort, idempotencyPayloadCodec);
    }
}