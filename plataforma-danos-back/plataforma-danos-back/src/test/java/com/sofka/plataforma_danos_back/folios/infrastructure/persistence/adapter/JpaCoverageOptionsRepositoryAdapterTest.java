package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.adapter;

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

import com.sofka.plataforma_danos_back.folios.domain.CoverageOptions;
import com.sofka.plataforma_danos_back.folios.domain.SelectedGuarantee;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.entity.CoverageOptionsEntity;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.repository.CoverageOptionsJpaRepository;

@ExtendWith(MockitoExtension.class)
class JpaCoverageOptionsRepositoryAdapterTest {

    @Mock
    private CoverageOptionsJpaRepository repository;

    @Test
    void save_createsEntityAndMapsBackWhenRecordDoesNotExist() {
        JpaCoverageOptionsRepositoryAdapter adapter = new JpaCoverageOptionsRepositoryAdapter(repository);
        CoverageOptions coverageOptions = new CoverageOptions(
                13L,
                List.of(
                        new SelectedGuarantee("GAR-INC-ED", List.of("base")),
                        new SelectedGuarantee("GAR-ROBO", List.of())
                ),
                "Cobertura base"
        );

        when(repository.findByCotizacionId(13L)).thenReturn(Optional.empty());
        when(repository.save(any(CoverageOptionsEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CoverageOptions saved = adapter.save(coverageOptions);

        ArgumentCaptor<CoverageOptionsEntity> entityCaptor = ArgumentCaptor.forClass(CoverageOptionsEntity.class);
        verify(repository).findByCotizacionId(13L);
        verify(repository).save(entityCaptor.capture());
        assertEquals(13L, entityCaptor.getValue().getCotizacionId());
        assertEquals("Cobertura base", entityCaptor.getValue().getObservaciones());
        assertEquals(2, entityCaptor.getValue().getGarantiasSeleccionadas().size());
        assertEquals("GAR-INC-ED", entityCaptor.getValue().getGarantiasSeleccionadas().get(0).garantiaCode());

        assertEquals(13L, saved.cotizacionId());
        assertEquals("Cobertura base", saved.observaciones());
        assertEquals(2, saved.garantiasSeleccionadas().size());
        assertEquals(List.of("base"), saved.garantiasSeleccionadas().get(0).terminos());
    }

    @Test
    void save_updatesExistingEntityWithoutLosingPersistedGuarantees() {
        JpaCoverageOptionsRepositoryAdapter adapter = new JpaCoverageOptionsRepositoryAdapter(repository);
        CoverageOptionsEntity existingEntity = new CoverageOptionsEntity();
        existingEntity.setCotizacionId(13L);
        existingEntity.setGarantiasSeleccionadas(List.of(new SelectedGuarantee("GAR-OLD", List.of("legacy"))));
        existingEntity.setObservaciones("Anterior");
        CoverageOptions coverageOptions = new CoverageOptions(
                13L,
                List.of(new SelectedGuarantee("GAR-INC-ED", List.of("base"))),
                "Actualizada"
        );

        when(repository.findByCotizacionId(13L)).thenReturn(Optional.of(existingEntity));
        when(repository.save(any(CoverageOptionsEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CoverageOptions saved = adapter.save(coverageOptions);

        verify(repository).findByCotizacionId(13L);
        verify(repository).save(existingEntity);
        assertEquals(13L, saved.cotizacionId());
        assertEquals("Actualizada", saved.observaciones());
        assertEquals(1, saved.garantiasSeleccionadas().size());
        assertEquals("GAR-INC-ED", saved.garantiasSeleccionadas().get(0).garantiaCode());
    }

    @Test
    void findByCotizacionId_mapsEntityToDomain() {
        JpaCoverageOptionsRepositoryAdapter adapter = new JpaCoverageOptionsRepositoryAdapter(repository);
        CoverageOptionsEntity entity = new CoverageOptionsEntity();
        entity.setCotizacionId(13L);
        entity.setGarantiasSeleccionadas(List.of(
                new SelectedGuarantee("GAR-INC-ED", List.of("base")),
                new SelectedGuarantee("GAR-ROBO", List.of())
        ));
        entity.setObservaciones("Cobertura base");

        when(repository.findByCotizacionId(13L)).thenReturn(Optional.of(entity));

        Optional<CoverageOptions> result = adapter.findByCotizacionId(13L);

        assertTrue(result.isPresent());
        assertEquals(13L, result.get().cotizacionId());
        assertEquals("Cobertura base", result.get().observaciones());
        assertEquals(2, result.get().garantiasSeleccionadas().size());
        assertEquals(List.of("base"), result.get().garantiasSeleccionadas().get(0).terminos());
    }
}