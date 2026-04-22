package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.entity.PrimaPorUbicacionEntity;

public interface PrimaPorUbicacionJpaRepository extends JpaRepository<PrimaPorUbicacionEntity, Long> {
    List<PrimaPorUbicacionEntity> findAllByCotizacionIdOrderByIndiceUbicacionAsc(Long cotizacionId);

    void deleteByCotizacionId(Long cotizacionId);
}