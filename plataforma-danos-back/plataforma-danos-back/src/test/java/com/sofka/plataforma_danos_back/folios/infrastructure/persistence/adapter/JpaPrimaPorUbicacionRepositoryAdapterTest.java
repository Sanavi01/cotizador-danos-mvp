package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.sofka.plataforma_danos_back.folios.domain.AlertaBloqueante;
import com.sofka.plataforma_danos_back.folios.domain.ComponenteComercialCalculado;
import com.sofka.plataforma_danos_back.folios.domain.ComponenteTecnicoCalculado;
import com.sofka.plataforma_danos_back.folios.domain.GarantiaCalculada;
import com.sofka.plataforma_danos_back.folios.domain.PrimaPorUbicacion;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.entity.PrimaPorUbicacionEntity;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.repository.PrimaPorUbicacionJpaRepository;

@ExtendWith(MockitoExtension.class)
class JpaPrimaPorUbicacionRepositoryAdapterTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-04-21T00:00:00Z");

    @Mock
    private PrimaPorUbicacionJpaRepository repository;

    @Test
    void findAllByCotizacionId_mapsEntityToDomain() {
        JpaPrimaPorUbicacionRepositoryAdapter adapter = new JpaPrimaPorUbicacionRepositoryAdapter(repository);
        PrimaPorUbicacionEntity entity = PrimaPorUbicacionEntity.fromDomain(13L, samplePrima(1));
        ReflectionTestUtils.setField(entity, "id", 91L);

        when(repository.findAllByCotizacionIdOrderByIndiceUbicacionAsc(13L)).thenReturn(List.of(entity));

        List<PrimaPorUbicacion> result = adapter.findAllByCotizacionId(13L);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).indiceUbicacion());
        assertEquals(new BigDecimal("24120.00"), result.get(0).primaNetaUbicacion());
        assertEquals(1, result.get(0).garantiasCalculadas().size());
        assertEquals(1, result.get(0).alertas().size());
        assertEquals("UBICACION_SIN_ZIP", result.get(0).alertas().get(0).codigo());
        verify(repository).findAllByCotizacionIdOrderByIndiceUbicacionAsc(13L);
    }

    @Test
    void saveAll_mapsDomainToEntityAndBack() {
        JpaPrimaPorUbicacionRepositoryAdapter adapter = new JpaPrimaPorUbicacionRepositoryAdapter(repository);
        PrimaPorUbicacion prima = samplePrima(2);

        when(repository.saveAll(any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            List<PrimaPorUbicacionEntity> entities = invocation.getArgument(0);
            entities.forEach(entity -> ReflectionTestUtils.setField(entity, "id", 101L + entity.getIndiceUbicacion()));
            return entities;
        });

        List<PrimaPorUbicacion> result = adapter.saveAll(13L, List.of(prima));

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).indiceUbicacion());
        assertEquals(new BigDecimal("28220.40"), result.get(0).primaComercialUbicacion());
        verify(repository).saveAll(any());
    }

    @Test
    void deleteByCotizacionId_delegatesToJpaRepository() {
        JpaPrimaPorUbicacionRepositoryAdapter adapter = new JpaPrimaPorUbicacionRepositoryAdapter(repository);

        adapter.deleteByCotizacionId(13L);

        verify(repository).deleteByCotizacionId(13L);
    }

    private PrimaPorUbicacion samplePrima(int indice) {
        return new PrimaPorUbicacion(
                indice,
                true,
                new BigDecimal("24120.00"),
                new BigDecimal("28220.40"),
                List.of(new GarantiaCalculada(
                        "GAR-INC-ED",
                        new BigDecimal("24120.00"),
                        List.of(
                                new ComponenteTecnicoCalculado("base", "tariffs", "GIRO-001|ZTEV-1|GAR-INC-ED", new BigDecimal("0.015"), new BigDecimal("1.20"), new BigDecimal("18000.00")),
                                new ComponenteTecnicoCalculado("factor_zona", "catTariffs", "ZTEV-1|GAR-INC-ED", new BigDecimal("0.006"), new BigDecimal("1.02"), new BigDecimal("6120.00"))
                        )
                )),
                List.of(new ComponenteComercialCalculado("RECARGO_ADMINISTRACION", new BigDecimal("0.12"), new BigDecimal("24120.00"), new BigDecimal("2894.40"))),
                List.of(new AlertaBloqueante("UBICACION_SIN_ZIP", "La ubicacion no tiene codigo postal valido.", "Warning"))
        );
    }
}