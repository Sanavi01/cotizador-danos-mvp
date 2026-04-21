package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.adapter;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.sofka.plataforma_danos_back.folios.domain.DatosGeneralesCotizacion;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.entity.DatosGeneralesCotizacionEntity;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.repository.DatosGeneralesCotizacionJpaRepository;

@ExtendWith(MockitoExtension.class)
class JpaDatosGeneralesCotizacionRepositoryAdapterTest {

    @Mock
    private DatosGeneralesCotizacionJpaRepository repository;

    @Test
    void save_createsEntityAndMapsBackWhenRecordDoesNotExist() {
        JpaDatosGeneralesCotizacionRepositoryAdapter adapter = new JpaDatosGeneralesCotizacionRepositoryAdapter(repository);
        Instant now = Instant.parse("2026-04-21T00:00:00Z");
        DatosGeneralesCotizacion datosGeneralesCotizacion = new DatosGeneralesCotizacion(
                13L,
                new DatosGeneralesCotizacion.DatosAsegurado(
                        "NIT",
                        "900123456",
                        "ACME SAS",
                        "contacto@acme.com",
                        "6015550101"
                ),
                new DatosGeneralesCotizacion.DatosConduccion("AG-102", "RISK-A", "GIRO-001"),
                now,
                now
        );

        when(repository.findByCotizacionId(13L)).thenReturn(Optional.empty());
        when(repository.save(any(DatosGeneralesCotizacionEntity.class))).thenAnswer(invocation -> {
            DatosGeneralesCotizacionEntity entity = invocation.getArgument(0);
            ReflectionTestUtils.setField(entity, "id", 51L);
            return entity;
        });

        DatosGeneralesCotizacion saved = adapter.save(datosGeneralesCotizacion);

        assertEquals(13L, saved.cotizacionId());
        assertEquals("NIT", saved.datosAsegurado().tipoDocumento());
        assertEquals("900123456", saved.datosAsegurado().numeroDocumento());
        assertEquals("ACME SAS", saved.datosAsegurado().nombreORazonSocial());
        assertEquals("AG-102", saved.datosConduccion().codigoAgente());
        assertEquals(now, saved.createdAt());
        assertEquals(now, saved.updatedAt());
        verify(repository).findByCotizacionId(13L);
        verify(repository).save(any(DatosGeneralesCotizacionEntity.class));
    }

    @Test
    void save_updatesExistingEntityWithoutLosingCreatedAt() {
        JpaDatosGeneralesCotizacionRepositoryAdapter adapter = new JpaDatosGeneralesCotizacionRepositoryAdapter(repository);
        Instant createdAt = Instant.parse("2026-04-20T00:00:00Z");
        Instant updatedAt = Instant.parse("2026-04-20T01:00:00Z");
        Instant now = Instant.parse("2026-04-21T00:00:00Z");
        DatosGeneralesCotizacionEntity existingEntity = new DatosGeneralesCotizacionEntity();
        ReflectionTestUtils.setField(existingEntity, "id", 51L);
        ReflectionTestUtils.setField(existingEntity, "cotizacionId", 13L);
        ReflectionTestUtils.setField(existingEntity, "tipoDocumento", "CC");
        ReflectionTestUtils.setField(existingEntity, "numeroDocumento", "123");
        ReflectionTestUtils.setField(existingEntity, "nombreORazonSocial", "Previo");
        ReflectionTestUtils.setField(existingEntity, "codigoAgente", "AG-101");
        ReflectionTestUtils.setField(existingEntity, "clasificacionRiesgo", "RISK-B");
        ReflectionTestUtils.setField(existingEntity, "tipoNegocio", "GIRO-002");
        ReflectionTestUtils.setField(existingEntity, "createdAt", createdAt);
        ReflectionTestUtils.setField(existingEntity, "updatedAt", updatedAt);

        DatosGeneralesCotizacion datosGeneralesCotizacion = new DatosGeneralesCotizacion(
                13L,
                new DatosGeneralesCotizacion.DatosAsegurado(
                        "NIT",
                        "900123456",
                        "ACME SAS",
                        "contacto@acme.com",
                        "6015550101"
                ),
                new DatosGeneralesCotizacion.DatosConduccion("AG-102", "RISK-A", "GIRO-001"),
                now,
                now
        );

        when(repository.findByCotizacionId(13L)).thenReturn(Optional.of(existingEntity));
        when(repository.save(any(DatosGeneralesCotizacionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DatosGeneralesCotizacion saved = adapter.save(datosGeneralesCotizacion);

        assertEquals(13L, saved.cotizacionId());
        assertEquals("NIT", saved.datosAsegurado().tipoDocumento());
        assertEquals("900123456", saved.datosAsegurado().numeroDocumento());
        assertEquals("ACME SAS", saved.datosAsegurado().nombreORazonSocial());
        assertEquals("AG-102", saved.datosConduccion().codigoAgente());
        assertEquals(createdAt, saved.createdAt());
        assertEquals(updatedAt, saved.updatedAt());
        verify(repository).findByCotizacionId(13L);
        verify(repository).save(existingEntity);
    }

    @Test
    void findByCotizacionId_mapsEntityToDomain() {
        JpaDatosGeneralesCotizacionRepositoryAdapter adapter = new JpaDatosGeneralesCotizacionRepositoryAdapter(repository);
        Instant now = Instant.parse("2026-04-21T00:00:00Z");
        DatosGeneralesCotizacionEntity entity = new DatosGeneralesCotizacionEntity();
        ReflectionTestUtils.setField(entity, "id", 51L);
        ReflectionTestUtils.setField(entity, "cotizacionId", 13L);
        ReflectionTestUtils.setField(entity, "tipoDocumento", "NIT");
        ReflectionTestUtils.setField(entity, "numeroDocumento", "900123456");
        ReflectionTestUtils.setField(entity, "nombreORazonSocial", "ACME SAS");
        ReflectionTestUtils.setField(entity, "correoElectronico", "contacto@acme.com");
        ReflectionTestUtils.setField(entity, "telefono", "6015550101");
        ReflectionTestUtils.setField(entity, "codigoAgente", "AG-102");
        ReflectionTestUtils.setField(entity, "clasificacionRiesgo", "RISK-A");
        ReflectionTestUtils.setField(entity, "tipoNegocio", "GIRO-001");
        ReflectionTestUtils.setField(entity, "createdAt", now);
        ReflectionTestUtils.setField(entity, "updatedAt", now);

        when(repository.findByCotizacionId(13L)).thenReturn(Optional.of(entity));

        Optional<DatosGeneralesCotizacion> result = adapter.findByCotizacionId(13L);

        assertTrue(result.isPresent());
        assertEquals(13L, result.get().cotizacionId());
        assertEquals("NIT", result.get().datosAsegurado().tipoDocumento());
        assertEquals("AG-102", result.get().datosConduccion().codigoAgente());
    }

    @Test
    void existsByCotizacionId_delegatesToRepository() {
        JpaDatosGeneralesCotizacionRepositoryAdapter adapter = new JpaDatosGeneralesCotizacionRepositoryAdapter(repository);

        when(repository.existsByCotizacionId(13L)).thenReturn(true);

        boolean exists = adapter.existsByCotizacionId(13L);

        assertTrue(exists);
        verify(repository).existsByCotizacionId(13L);
    }
}