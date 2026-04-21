package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.repository;

import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.entity.DatosGeneralesCotizacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DatosGeneralesCotizacionJpaRepository extends JpaRepository<DatosGeneralesCotizacionEntity, Long> {
    Optional<DatosGeneralesCotizacionEntity> findByCotizacionId(Long cotizacionId);

    boolean existsByCotizacionId(Long cotizacionId);
}