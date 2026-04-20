package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.repository;

import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.entity.IdempotencyRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyRecordJpaRepository extends JpaRepository<IdempotencyRecordEntity, Long> {
    Optional<IdempotencyRecordEntity> findByIdempotencyKey(String idempotencyKey);
}
