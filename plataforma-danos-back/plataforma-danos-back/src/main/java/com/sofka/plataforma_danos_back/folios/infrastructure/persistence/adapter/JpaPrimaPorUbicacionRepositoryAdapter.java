package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.adapter;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.sofka.plataforma_danos_back.folios.domain.PrimaPorUbicacion;
import com.sofka.plataforma_danos_back.folios.domain.port.PrimaPorUbicacionRepository;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.entity.PrimaPorUbicacionEntity;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.repository.PrimaPorUbicacionJpaRepository;

@Repository
public class JpaPrimaPorUbicacionRepositoryAdapter implements PrimaPorUbicacionRepository {

    private final PrimaPorUbicacionJpaRepository repository;

    public JpaPrimaPorUbicacionRepositoryAdapter(PrimaPorUbicacionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<PrimaPorUbicacion> findAllByCotizacionId(Long cotizacionId) {
        return repository.findAllByCotizacionIdOrderByIndiceUbicacionAsc(cotizacionId).stream()
                .map(PrimaPorUbicacionEntity::toDomain)
                .toList();
    }

    @Override
    public List<PrimaPorUbicacion> saveAll(Long cotizacionId, List<PrimaPorUbicacion> primasPorUbicacion) {
        List<PrimaPorUbicacionEntity> entities = primasPorUbicacion.stream()
                .map(prima -> PrimaPorUbicacionEntity.fromDomain(cotizacionId, prima))
                .toList();
        return repository.saveAll(entities).stream()
                .map(PrimaPorUbicacionEntity::toDomain)
                .toList();
    }

    @Override
    public void deleteByCotizacionId(Long cotizacionId) {
        repository.deleteByCotizacionId(cotizacionId);
    }
}