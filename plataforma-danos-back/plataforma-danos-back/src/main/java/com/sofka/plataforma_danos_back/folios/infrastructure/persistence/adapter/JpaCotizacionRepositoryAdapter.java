package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.adapter;

import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.port.CotizacionRepository;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.entity.CotizacionEntity;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.repository.CotizacionJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaCotizacionRepositoryAdapter implements CotizacionRepository {

    private final CotizacionJpaRepository repository;

    public JpaCotizacionRepositoryAdapter(CotizacionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Cotizacion save(Cotizacion cotizacion) {
        CotizacionEntity saved = repository.save(CotizacionEntity.fromDomain(cotizacion));
        return saved.toDomain();
    }

    @Override
    public Optional<Cotizacion> findByNumeroFolio(String numeroFolio) {
        return repository.findByNumeroFolio(numeroFolio).map(CotizacionEntity::toDomain);
    }
}
