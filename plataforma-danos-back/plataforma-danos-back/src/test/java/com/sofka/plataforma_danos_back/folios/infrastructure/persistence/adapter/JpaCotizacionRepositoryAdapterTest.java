package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.adapter;

import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.EstadoCotizacion;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.entity.CotizacionEntity;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.repository.CotizacionJpaRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaCotizacionRepositoryAdapterTest {

    @Mock
    private CotizacionJpaRepository repository;

    @Test
    void save_mapsDomainEntityAndBack() {
        JpaCotizacionRepositoryAdapter adapter = new JpaCotizacionRepositoryAdapter(repository);
        Instant now = Instant.parse("2026-04-20T00:00:00Z");
        Cotizacion cotizacion = Cotizacion.nueva("1000001", now);

        when(repository.save(any(CotizacionEntity.class))).thenAnswer(invocation -> {
            CotizacionEntity entity = invocation.getArgument(0);
            ReflectionTestUtils.setField(entity, "id", 11L);
            return entity;
        });

        Cotizacion saved = adapter.save(cotizacion);

        assertEquals(11L, saved.id());
        assertEquals("1000001", saved.numeroFolio());
        assertEquals(EstadoCotizacion.BORRADOR, saved.estadoCotizacion());
        assertEquals(0L, saved.version());
        assertEquals(now, saved.fechaUltimaActualizacion());
    }

    @Test
    void findByNumeroFolio_mapsEntityToDomain() {
        JpaCotizacionRepositoryAdapter adapter = new JpaCotizacionRepositoryAdapter(repository);
        Instant now = Instant.parse("2026-04-20T00:00:00Z");
        CotizacionEntity entity = new CotizacionEntity();
        ReflectionTestUtils.setField(entity, "id", 15L);
        ReflectionTestUtils.setField(entity, "numeroFolio", "1000001");
        ReflectionTestUtils.setField(entity, "estadoCotizacion", EstadoCotizacion.BORRADOR);
        ReflectionTestUtils.setField(entity, "version", 0L);
        ReflectionTestUtils.setField(entity, "fechaUltimaActualizacion", now);

        when(repository.findByNumeroFolio("1000001")).thenReturn(Optional.of(entity));

        Optional<Cotizacion> result = adapter.findByNumeroFolio("1000001");

        assertTrue(result.isPresent());
        assertEquals(15L, result.get().id());
        assertEquals("1000001", result.get().numeroFolio());
        assertEquals(EstadoCotizacion.BORRADOR, result.get().estadoCotizacion());
        assertEquals(0L, result.get().version());
        assertEquals(now, result.get().fechaUltimaActualizacion());
    }
}