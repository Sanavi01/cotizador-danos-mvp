package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.sofka.plataforma_danos_back.folios.domain.ConfiguracionLayout;
import com.sofka.plataforma_danos_back.folios.domain.port.ConfiguracionLayoutRepository;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.entity.ConfiguracionLayoutEntity;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.repository.ConfiguracionLayoutJpaRepository;

@Repository
public class JpaConfiguracionLayoutRepositoryAdapter implements ConfiguracionLayoutRepository {

    private final ConfiguracionLayoutJpaRepository repository;

    public JpaConfiguracionLayoutRepositoryAdapter(ConfiguracionLayoutJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ConfiguracionLayout> findByCotizacionId(Long cotizacionId) {
        return repository.findByCotizacionId(cotizacionId).map(ConfiguracionLayoutEntity::toDomain);
    }

    @Override
    public boolean existsByCotizacionId(Long cotizacionId) {
        return repository.existsByCotizacionId(cotizacionId);
    }

    @Override
    public ConfiguracionLayout save(ConfiguracionLayout configuracionLayout) {
        ConfiguracionLayoutEntity entity = repository.findByCotizacionId(configuracionLayout.cotizacionId())
                .orElseGet(ConfiguracionLayoutEntity::new);
        if (entity.getCreatedAt() == null) {
            entity = ConfiguracionLayoutEntity.fromDomain(configuracionLayout);
        } else {
            entity.setCotizacionId(configuracionLayout.cotizacionId());
            entity.setModoCaptura(configuracionLayout.modoCaptura());
            entity.setCantidadUbicaciones(configuracionLayout.cantidadUbicaciones());
            entity.setUbicaciones(configuracionLayout.ubicaciones().stream()
                    .map(ConfiguracionLayoutEntity.LayoutUbicacionSlotEmbeddable::fromDomain)
                    .toList());
            entity.setUpdatedAt(configuracionLayout.updatedAt());
        }
        ConfiguracionLayoutEntity saved = repository.save(entity);
        repository.flush();
        return saved.toDomain();
    }
}