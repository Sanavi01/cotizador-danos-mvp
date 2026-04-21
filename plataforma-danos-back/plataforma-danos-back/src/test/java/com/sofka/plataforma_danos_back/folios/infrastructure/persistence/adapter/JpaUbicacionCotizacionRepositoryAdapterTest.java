package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.adapter;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.sofka.plataforma_danos_back.folios.domain.AlertaBloqueante;
import com.sofka.plataforma_danos_back.folios.domain.EstadoValidacion;
import com.sofka.plataforma_danos_back.folios.domain.Giro;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionDetalle;
import com.sofka.plataforma_danos_back.folios.domain.ZonaCatastrofica;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.entity.UbicacionCotizacionEntity;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.repository.UbicacionCotizacionJpaRepository;

@ExtendWith(MockitoExtension.class)
class JpaUbicacionCotizacionRepositoryAdapterTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-04-21T00:00:00Z");

    @Mock
    private UbicacionCotizacionJpaRepository repository;

    @Test
    void saveAll_mapsDomainToEntityAndBack() {
        JpaUbicacionCotizacionRepositoryAdapter adapter = new JpaUbicacionCotizacionRepositoryAdapter(repository);
        List<UbicacionCotizacion> ubicaciones = List.of(validLocation(1), invalidLocation(2));

        when(repository.saveAll(any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            List<UbicacionCotizacionEntity> entities = invocation.getArgument(0);
            ReflectionTestUtils.setField(entities.get(0), "id", 101L);
            ReflectionTestUtils.setField(entities.get(1), "id", 102L);
            return entities;
        });

        List<UbicacionCotizacion> result = adapter.saveAll(ubicaciones);

        ArgumentCaptor<List<UbicacionCotizacionEntity>> entityCaptor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(entityCaptor.capture());
        assertEquals(2, entityCaptor.getValue().size());
        assertEquals(1, entityCaptor.getValue().get(0).getIndice());
        assertEquals("110111", entityCaptor.getValue().get(0).getCodigoPostal());
        assertEquals(2, entityCaptor.getValue().get(1).getIndice());
        assertEquals("000000", entityCaptor.getValue().get(1).getCodigoPostal());

        assertEquals(101L, result.get(0).id());
        assertEquals(102L, result.get(1).id());
        assertEquals(EstadoValidacion.CALCULABLE, result.get(0).estadoValidacion());
        assertEquals(EstadoValidacion.INVALID, result.get(1).estadoValidacion());
    }

    @Test
    void findByCotizacionIdAndIndice_mapsEntityToDomain() {
        JpaUbicacionCotizacionRepositoryAdapter adapter = new JpaUbicacionCotizacionRepositoryAdapter(repository);
        UbicacionCotizacionEntity entity = UbicacionCotizacionEntity.fromDomain(validLocation(1));
        ReflectionTestUtils.setField(entity, "id", 77L);

        when(repository.findByCotizacionIdAndIndice(13L, 1)).thenReturn(Optional.of(entity));

        Optional<UbicacionCotizacion> result = adapter.findByCotizacionIdAndIndice(13L, 1);

        assertTrue(result.isPresent());
        assertEquals(77L, result.get().id());
        assertEquals("110111", result.get().detalle().codigoPostal());
        assertEquals(EstadoValidacion.CALCULABLE, result.get().estadoValidacion());
    }

    @Test
    void findAllByCotizacionId_mapsEntitiesToDomainInOrder() {
        JpaUbicacionCotizacionRepositoryAdapter adapter = new JpaUbicacionCotizacionRepositoryAdapter(repository);
        UbicacionCotizacionEntity first = UbicacionCotizacionEntity.fromDomain(validLocation(1));
        UbicacionCotizacionEntity second = UbicacionCotizacionEntity.fromDomain(invalidLocation(2));
        ReflectionTestUtils.setField(first, "id", 77L);
        ReflectionTestUtils.setField(second, "id", 78L);

        when(repository.findAllByCotizacionIdOrderByIndiceAsc(13L)).thenReturn(List.of(first, second));

        List<UbicacionCotizacion> result = adapter.findAllByCotizacionId(13L);

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).detalle().indice());
        assertEquals(2, result.get(1).detalle().indice());
        assertEquals(EstadoValidacion.CALCULABLE, result.get(0).estadoValidacion());
        assertEquals(EstadoValidacion.INVALID, result.get(1).estadoValidacion());
    }

    @Test
    void deleteByCotizacionIdAndIndiceNotIn_delegatesToRepository() {
        JpaUbicacionCotizacionRepositoryAdapter adapter = new JpaUbicacionCotizacionRepositoryAdapter(repository);

        adapter.deleteByCotizacionIdAndIndiceNotIn(13L, List.of(1, 2));

        verify(repository).deleteByCotizacionIdAndIndiceNotIn(13L, List.of(1, 2));
        verify(repository).flush();
    }

    private static UbicacionCotizacion validLocation(int indice) {
        return new UbicacionCotizacion(
                (long) indice,
                13L,
                new UbicacionDetalle(
                        indice,
                        "Planta principal",
                        "Calle 100 # 10-10",
                        "110111",
                        "Bogota D.C.",
                        "Bogota",
                        "Chapinero",
                        "Bogota",
                        "CONCRETO",
                        1,
                        2018,
                        new Giro("GIRO-001", "Manufactura ligera", "CI-001"),
                        new ZonaCatastrofica("Z-TEV-01", "Z-FHM-01")
                ),
                EstadoValidacion.CALCULABLE,
                List.of(),
                FIXED_NOW,
                FIXED_NOW
        );
    }

    private static UbicacionCotizacion invalidLocation(int indice) {
        return new UbicacionCotizacion(
                20L,
                13L,
                new UbicacionDetalle(
                        indice,
                        "Bodega secundaria",
                        null,
                        "000000",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                EstadoValidacion.INVALID,
                List.of(new AlertaBloqueante("UBICACION_SIN_ZIP", "La ubicacion no tiene codigo postal valido.", "Warning")),
                FIXED_NOW,
                FIXED_NOW
        );
    }
}