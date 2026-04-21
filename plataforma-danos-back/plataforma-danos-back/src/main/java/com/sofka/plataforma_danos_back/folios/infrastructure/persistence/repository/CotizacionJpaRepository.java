package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.repository;

import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.entity.CotizacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CotizacionJpaRepository extends JpaRepository<CotizacionEntity, Long> {
    Optional<CotizacionEntity> findByNumeroFolio(String numeroFolio);
}
