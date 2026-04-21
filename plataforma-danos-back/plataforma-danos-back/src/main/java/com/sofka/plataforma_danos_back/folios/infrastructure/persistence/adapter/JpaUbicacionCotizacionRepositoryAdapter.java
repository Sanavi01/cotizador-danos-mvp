package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.adapter;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.sofka.plataforma_danos_back.folios.domain.UbicacionCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.port.UbicacionCotizacionRepository;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.entity.UbicacionCotizacionEntity;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.repository.UbicacionCotizacionJpaRepository;

@Repository
public class JpaUbicacionCotizacionRepositoryAdapter implements UbicacionCotizacionRepository {

    private final UbicacionCotizacionJpaRepository repository;

    public JpaUbicacionCotizacionRepositoryAdapter(UbicacionCotizacionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<UbicacionCotizacion> findAllByCotizacionId(Long cotizacionId) {
        return repository.findAllByCotizacionIdOrderByIndiceAsc(cotizacionId).stream()
                .map(UbicacionCotizacionEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<UbicacionCotizacion> findByCotizacionIdAndIndice(Long cotizacionId, Integer indice) {
        return repository.findByCotizacionIdAndIndice(cotizacionId, indice).map(UbicacionCotizacionEntity::toDomain);
    }

    @Override
    public List<UbicacionCotizacion> saveAll(List<UbicacionCotizacion> ubicaciones) {
        List<UbicacionCotizacionEntity> entities = ubicaciones.stream()
                .map(UbicacionCotizacionEntity::fromDomain)
                .toList();
        List<UbicacionCotizacionEntity> saved = repository.saveAll(entities);
        repository.flush();
        return saved.stream().map(UbicacionCotizacionEntity::toDomain).toList();
    }

    @Override
    public UbicacionCotizacion save(UbicacionCotizacion ubicacion) {
        UbicacionCotizacionEntity saved = repository.save(UbicacionCotizacionEntity.fromDomain(ubicacion));
        repository.flush();
        return saved.toDomain();
    }

    @Override
    public void deleteByCotizacionIdAndIndiceNotIn(Long cotizacionId, Collection<Integer> indices) {
        repository.deleteByCotizacionIdAndIndiceNotIn(cotizacionId, indices);
        repository.flush();
    }
}