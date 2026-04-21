package com.sofka.plataforma_danos_back.folios.domain.port;

import com.sofka.plataforma_danos_back.folios.domain.IdempotencyRecord;

import java.util.Optional;

public interface IdempotencyRepository {
    Optional<IdempotencyRecord> findByKey(String idempotencyKey);

    IdempotencyRecord save(IdempotencyRecord record);
}
