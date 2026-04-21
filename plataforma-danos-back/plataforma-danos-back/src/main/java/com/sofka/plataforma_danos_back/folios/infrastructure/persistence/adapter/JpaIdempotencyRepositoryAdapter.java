package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.adapter;

import com.sofka.plataforma_danos_back.folios.domain.IdempotencyRecord;
import com.sofka.plataforma_danos_back.folios.domain.port.IdempotencyRepository;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.entity.CotizacionEntity;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.entity.IdempotencyRecordEntity;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.repository.IdempotencyRecordJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaIdempotencyRepositoryAdapter implements IdempotencyRepository {

    private final IdempotencyRecordJpaRepository repository;
    @PersistenceContext
    private EntityManager entityManager;

    public JpaIdempotencyRepositoryAdapter(IdempotencyRecordJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<IdempotencyRecord> findByKey(String idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey).map(this::toDomain);
    }

    @Override
    public IdempotencyRecord save(IdempotencyRecord record) {
        IdempotencyRecordEntity entity = new IdempotencyRecordEntity();
        entity.setIdempotencyKey(record.idempotencyKey());
        entity.setRequestHash(record.requestHash());
        entity.setResponsePayload(record.responsePayload());
        entity.setCotizacion(entityManager.getReference(CotizacionEntity.class, record.cotizacionId()));
        IdempotencyRecordEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    private IdempotencyRecord toDomain(IdempotencyRecordEntity entity) {
        return new IdempotencyRecord(
                entity.getId(),
                entity.getIdempotencyKey(),
                entity.getRequestHash(),
                entity.getResponsePayload(),
                entity.getCotizacion().getId(),
                entity.getCotizacion().getNumeroFolio(),
                entity.getCreatedAt()
        );
    }
}
