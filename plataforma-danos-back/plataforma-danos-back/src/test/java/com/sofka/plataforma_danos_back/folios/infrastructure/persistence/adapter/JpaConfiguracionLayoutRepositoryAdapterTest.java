package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.adapter;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.sofka.plataforma_danos_back.folios.domain.ConfiguracionLayout;
import com.sofka.plataforma_danos_back.folios.domain.LayoutUbicacionSlot;
import com.sofka.plataforma_danos_back.folios.domain.ModoCaptura;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.entity.ConfiguracionLayoutEntity;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.repository.ConfiguracionLayoutJpaRepository;

@ExtendWith(MockitoExtension.class)
class JpaConfiguracionLayoutRepositoryAdapterTest {

    @Mock
    private ConfiguracionLayoutJpaRepository repository;

    @Test
    void save_mapsDomainEntityAndBack() {
        JpaConfiguracionLayoutRepositoryAdapter adapter = new JpaConfiguracionLayoutRepositoryAdapter(repository);
        Instant now = Instant.parse("2026-04-21T00:00:00Z");
        ConfiguracionLayout configuracionLayout = new ConfiguracionLayout(
                null,
                13L,
                ModoCaptura.MULTIPLE,
                3,
                List.of(
                        new LayoutUbicacionSlot(1, 1),
                        new LayoutUbicacionSlot(2, 2),
                        new LayoutUbicacionSlot(3, 3)
                ),
                now,
                now
        );

        when(repository.findByCotizacionId(13L)).thenReturn(Optional.empty());
        when(repository.save(any(ConfiguracionLayoutEntity.class))).thenAnswer(invocation -> {
            ConfiguracionLayoutEntity entity = invocation.getArgument(0);
            ReflectionTestUtils.setField(entity, "id", 22L);
            return entity;
        });

        ConfiguracionLayout saved = adapter.save(configuracionLayout);

        assertEquals(22L, saved.id());
        assertEquals(13L, saved.cotizacionId());
        assertEquals(ModoCaptura.MULTIPLE, saved.modoCaptura());
        assertEquals(3, saved.cantidadUbicaciones());
        assertEquals(3, saved.ubicaciones().size());
        assertEquals(now, saved.createdAt());
        assertEquals(now, saved.updatedAt());
    }

    @Test
    void save_updatesExistingEntityAndBack() {
        JpaConfiguracionLayoutRepositoryAdapter adapter = new JpaConfiguracionLayoutRepositoryAdapter(repository);
        Instant createdAt = Instant.parse("2026-04-20T00:00:00Z");
        Instant updatedAt = Instant.parse("2026-04-20T12:00:00Z");
        Instant now = Instant.parse("2026-04-21T00:00:00Z");
        ConfiguracionLayoutEntity existing = new ConfiguracionLayoutEntity();
        ReflectionTestUtils.setField(existing, "id", 22L);
        existing.setCotizacionId(13L);
        existing.setModoCaptura(ModoCaptura.UNICA);
        existing.setCantidadUbicaciones(1);
        existing.setUbicaciones(List.of(
                ConfiguracionLayoutEntity.LayoutUbicacionSlotEmbeddable.fromDomain(new LayoutUbicacionSlot(1, 1))
        ));
        existing.setCreatedAt(createdAt);
        existing.setUpdatedAt(updatedAt);

        ConfiguracionLayout configuracionLayout = new ConfiguracionLayout(
                22L,
                13L,
                ModoCaptura.MULTIPLE,
                3,
                List.of(
                        new LayoutUbicacionSlot(1, 1),
                        new LayoutUbicacionSlot(2, 2),
                        new LayoutUbicacionSlot(3, 3)
                ),
                createdAt,
                now
        );

        when(repository.findByCotizacionId(13L)).thenReturn(Optional.of(existing));
        when(repository.save(any(ConfiguracionLayoutEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConfiguracionLayout saved = adapter.save(configuracionLayout);

        assertEquals(22L, saved.id());
        assertEquals(13L, saved.cotizacionId());
        assertEquals(ModoCaptura.MULTIPLE, saved.modoCaptura());
        assertEquals(3, saved.cantidadUbicaciones());
        assertEquals(3, saved.ubicaciones().size());
        assertEquals(createdAt, saved.createdAt());
        assertEquals(now, saved.updatedAt());
    }

    @Test
    void findByCotizacionId_mapsEntityToDomain() {
        JpaConfiguracionLayoutRepositoryAdapter adapter = new JpaConfiguracionLayoutRepositoryAdapter(repository);
        Instant now = Instant.parse("2026-04-20T00:00:00Z");
        ConfiguracionLayoutEntity entity = new ConfiguracionLayoutEntity();
        ReflectionTestUtils.setField(entity, "id", 25L);
        entity.setCotizacionId(13L);
        entity.setModoCaptura(ModoCaptura.MULTIPLE);
        entity.setCantidadUbicaciones(3);
        entity.setUbicaciones(List.of(
                ConfiguracionLayoutEntity.LayoutUbicacionSlotEmbeddable.fromDomain(new LayoutUbicacionSlot(1, 1)),
                ConfiguracionLayoutEntity.LayoutUbicacionSlotEmbeddable.fromDomain(new LayoutUbicacionSlot(2, 2)),
                ConfiguracionLayoutEntity.LayoutUbicacionSlotEmbeddable.fromDomain(new LayoutUbicacionSlot(3, 3))
        ));
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        when(repository.findByCotizacionId(13L)).thenReturn(Optional.of(entity));

        Optional<ConfiguracionLayout> result = adapter.findByCotizacionId(13L);

        assertTrue(result.isPresent());
        assertEquals(25L, result.get().id());
        assertEquals(13L, result.get().cotizacionId());
        assertEquals(ModoCaptura.MULTIPLE, result.get().modoCaptura());
        assertEquals(3, result.get().cantidadUbicaciones());
        assertEquals(3, result.get().ubicaciones().size());
        assertEquals(now, result.get().createdAt());
        assertEquals(now, result.get().updatedAt());
    }
}