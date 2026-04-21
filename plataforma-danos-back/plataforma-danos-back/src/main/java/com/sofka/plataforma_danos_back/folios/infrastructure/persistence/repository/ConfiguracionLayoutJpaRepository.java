package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.entity.ConfiguracionLayoutEntity;

public interface ConfiguracionLayoutJpaRepository extends JpaRepository<ConfiguracionLayoutEntity, Long> {
    Optional<ConfiguracionLayoutEntity> findByCotizacionId(Long cotizacionId);

    boolean existsByCotizacionId(Long cotizacionId);
}