package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.port.CotizacionRepository;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.entity.CotizacionEntity;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.repository.CotizacionJpaRepository;

@Repository
public class JpaCotizacionRepositoryAdapter implements CotizacionRepository {

    private final CotizacionJpaRepository repository;

    public JpaCotizacionRepositoryAdapter(CotizacionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Cotizacion save(Cotizacion cotizacion) {
        CotizacionEntity entity = cotizacion.id() == null
                ? new CotizacionEntity()
                : repository.findById(cotizacion.id()).orElseGet(CotizacionEntity::new);
        entity.setNumeroFolio(cotizacion.numeroFolio());
        entity.setEstadoCotizacion(cotizacion.estadoCotizacion());
        entity.setVersion(cotizacion.version());
        entity.setFechaUltimaActualizacion(cotizacion.fechaUltimaActualizacion());
        entity.setPrimaNeta(cotizacion.primaNeta());
        entity.setPrimaComercial(cotizacion.primaComercial());
        CotizacionEntity saved = repository.save(entity);
        repository.flush();
        return saved.toDomain();
    }

    @Override
    public Optional<Cotizacion> findByNumeroFolio(String numeroFolio) {
        return repository.findByNumeroFolio(numeroFolio).map(CotizacionEntity::toDomain);
    }
}
