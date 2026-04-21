package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.entity.CoverageOptionsEntity;

public interface CoverageOptionsJpaRepository extends JpaRepository<CoverageOptionsEntity, Long> {
    Optional<CoverageOptionsEntity> findByCotizacionId(Long cotizacionId);

    boolean existsByCotizacionId(Long cotizacionId);
}