package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.sofka.plataforma_danos_back.folios.domain.CoverageOptions;
import com.sofka.plataforma_danos_back.folios.domain.port.CoverageOptionsRepository;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.entity.CoverageOptionsEntity;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.repository.CoverageOptionsJpaRepository;

@Repository
public class JpaCoverageOptionsRepositoryAdapter implements CoverageOptionsRepository {

    private final CoverageOptionsJpaRepository repository;

    public JpaCoverageOptionsRepositoryAdapter(CoverageOptionsJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<CoverageOptions> findByCotizacionId(Long cotizacionId) {
        return repository.findByCotizacionId(cotizacionId).map(CoverageOptionsEntity::toDomain);
    }

    @Override
    public CoverageOptions save(CoverageOptions coverageOptions) {
        CoverageOptionsEntity entity = repository.findByCotizacionId(coverageOptions.cotizacionId())
                .orElseGet(CoverageOptionsEntity::new);
        entity.setCotizacionId(coverageOptions.cotizacionId());
        entity.setGarantiasSeleccionadas(coverageOptions.garantiasSeleccionadas());
        entity.setObservaciones(coverageOptions.observaciones());
        CoverageOptionsEntity saved = repository.save(entity);
        repository.flush();
        return saved.toDomain();
    }
}