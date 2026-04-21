package com.sofka.plataforma_danos_back.folios.domain;

import java.time.Instant;

public record IdempotencyRecord(
        Long id,
        String idempotencyKey,
        String requestHash,
        String responsePayload,
        Long cotizacionId,
        String numeroFolio,
        Instant createdAt
) {
    public static IdempotencyRecord createNew(
            String idempotencyKey,
            String requestHash,
            String responsePayload,
            Long cotizacionId,
            String numeroFolio,
            Instant createdAt
    ) {
        return new IdempotencyRecord(null, idempotencyKey, requestHash, responsePayload, cotizacionId, numeroFolio, createdAt);
    }
}
