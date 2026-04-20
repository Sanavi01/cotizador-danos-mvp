package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.adapter;

import com.sofka.plataforma_danos_back.folios.domain.IdempotencyRecord;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.entity.CotizacionEntity;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.entity.IdempotencyRecordEntity;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.repository.IdempotencyRecordJpaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaIdempotencyRepositoryAdapterTest {

    @Mock
    private IdempotencyRecordJpaRepository repository;

    @Mock
    private EntityManager entityManager;

    @Test
    void save_mapsRecordAndUsesCotizacionReference() {
        JpaIdempotencyRepositoryAdapter adapter = new JpaIdempotencyRepositoryAdapter(repository);
        ReflectionTestUtils.setField(adapter, "entityManager", entityManager);

        Instant now = Instant.parse("2026-04-20T00:00:00Z");
        CotizacionEntity cotizacionReference = new CotizacionEntity();
        ReflectionTestUtils.setField(cotizacionReference, "id", 99L);
        ReflectionTestUtils.setField(cotizacionReference, "numeroFolio", "1000001");

        when(entityManager.getReference(CotizacionEntity.class, 99L)).thenReturn(cotizacionReference);
        when(repository.save(any(IdempotencyRecordEntity.class))).thenAnswer(invocation -> {
            IdempotencyRecordEntity entity = invocation.getArgument(0);
            ReflectionTestUtils.setField(entity, "id", 123L);
            ReflectionTestUtils.setField(entity, "createdAt", now);
            return entity;
        });

        IdempotencyRecord saved = adapter.save(IdempotencyRecord.createNew(
                "idem-key",
                "request-hash",
                "{\"numeroFolio\":\"1000001\"}",
                99L,
                "1000001",
                now
        ));

        assertEquals(123L, saved.id());
        assertEquals("idem-key", saved.idempotencyKey());
        assertEquals("request-hash", saved.requestHash());
        assertEquals("{\"numeroFolio\":\"1000001\"}", saved.responsePayload());
        assertEquals(99L, saved.cotizacionId());
        assertEquals("1000001", saved.numeroFolio());
        assertEquals(now, saved.createdAt());
        verify(entityManager).getReference(CotizacionEntity.class, 99L);
    }

    @Test
    void findByKey_mapsEntityToDomain() {
        JpaIdempotencyRepositoryAdapter adapter = new JpaIdempotencyRepositoryAdapter(repository);
        ReflectionTestUtils.setField(adapter, "entityManager", entityManager);

        Instant now = Instant.parse("2026-04-20T00:00:00Z");
        CotizacionEntity cotizacionEntity = new CotizacionEntity();
        ReflectionTestUtils.setField(cotizacionEntity, "id", 88L);
        ReflectionTestUtils.setField(cotizacionEntity, "numeroFolio", "1000001");

        IdempotencyRecordEntity entity = new IdempotencyRecordEntity();
        ReflectionTestUtils.setField(entity, "id", 55L);
        ReflectionTestUtils.setField(entity, "idempotencyKey", "idem-key");
        ReflectionTestUtils.setField(entity, "requestHash", "request-hash");
        ReflectionTestUtils.setField(entity, "responsePayload", "{\"numeroFolio\":\"1000001\"}");
        ReflectionTestUtils.setField(entity, "cotizacion", cotizacionEntity);
        ReflectionTestUtils.setField(entity, "createdAt", now);

        when(repository.findByIdempotencyKey(eq("idem-key"))).thenReturn(Optional.of(entity));

        Optional<IdempotencyRecord> result = adapter.findByKey("idem-key");

        assertTrue(result.isPresent());
        assertEquals(55L, result.get().id());
        assertEquals("idem-key", result.get().idempotencyKey());
        assertEquals("request-hash", result.get().requestHash());
        assertEquals("{\"numeroFolio\":\"1000001\"}", result.get().responsePayload());
        assertEquals(88L, result.get().cotizacionId());
        assertEquals("1000001", result.get().numeroFolio());
        assertEquals(now, result.get().createdAt());
    }
}