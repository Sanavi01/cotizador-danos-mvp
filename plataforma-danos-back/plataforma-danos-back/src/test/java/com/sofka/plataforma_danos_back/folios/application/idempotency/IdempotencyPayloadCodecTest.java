package com.sofka.plataforma_danos_back.folios.application.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sofka.plataforma_danos_back.folios.application.dto.CreateFolioRequest;
import com.sofka.plataforma_danos_back.folios.application.dto.CreateFolioResponse;
import com.sofka.plataforma_danos_back.folios.domain.EstadoCotizacion;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IdempotencyPayloadCodecTest {

    private final IdempotencyPayloadCodec codec = new IdempotencyPayloadCodec(
            new ObjectMapper().registerModule(new JavaTimeModule())
    );

    @Test
    void hashRequest_isStableForEquivalentPayloads() {
        String firstHash = codec.hashRequest(CreateFolioRequest.defaultRequest());
        String secondHash = codec.hashRequest(new CreateFolioRequest("spa"));
        String differentHash = codec.hashRequest(new CreateFolioRequest("web"));

        assertEquals(firstHash, secondHash);
        assertNotEquals(firstHash, differentHash);
    }

    @Test
    void serializeAndDeserializeCreateFolioResponse_roundTripsPayload() {
        Instant now = Instant.parse("2026-04-20T00:00:00Z");
        CreateFolioResponse response = new CreateFolioResponse("1000001", EstadoCotizacion.BORRADOR, 0L, now);

        String payload = codec.serializeCreateFolioResponse(response);
        CreateFolioResponse rebuilt = codec.deserializeCreateFolioResponse(payload);

        assertEquals(response, rebuilt);
    }

    @Test
    void deserializeCreateFolioResponse_throwsWhenPayloadIsInvalid() {
        assertThrows(IllegalStateException.class, () -> codec.deserializeCreateFolioResponse("not-json"));
    }
}