package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.entity.UbicacionCotizacionEntity;

public interface UbicacionCotizacionJpaRepository extends JpaRepository<UbicacionCotizacionEntity, Long> {
    List<UbicacionCotizacionEntity> findAllByCotizacionIdOrderByIndiceAsc(Long cotizacionId);

    Optional<UbicacionCotizacionEntity> findByCotizacionIdAndIndice(Long cotizacionId, Integer indice);

    void deleteByCotizacionIdAndIndiceNotIn(Long cotizacionId, Collection<Integer> indices);
}