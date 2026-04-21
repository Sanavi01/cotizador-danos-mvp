package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.adapter;

import com.sofka.plataforma_danos_back.folios.domain.DatosGeneralesCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.port.DatosGeneralesCotizacionRepository;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.entity.DatosGeneralesCotizacionEntity;
import com.sofka.plataforma_danos_back.folios.infrastructure.persistence.repository.DatosGeneralesCotizacionJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaDatosGeneralesCotizacionRepositoryAdapter implements DatosGeneralesCotizacionRepository {

    private final DatosGeneralesCotizacionJpaRepository repository;

    public JpaDatosGeneralesCotizacionRepositoryAdapter(DatosGeneralesCotizacionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<DatosGeneralesCotizacion> findByCotizacionId(Long cotizacionId) {
        return repository.findByCotizacionId(cotizacionId).map(DatosGeneralesCotizacionEntity::toDomain);
    }

    @Override
    public boolean existsByCotizacionId(Long cotizacionId) {
        return repository.existsByCotizacionId(cotizacionId);
    }

    @Override
    public DatosGeneralesCotizacion save(DatosGeneralesCotizacion datosGeneralesCotizacion) {
        DatosGeneralesCotizacionEntity entity = repository.findByCotizacionId(datosGeneralesCotizacion.cotizacionId())
                .orElseGet(DatosGeneralesCotizacionEntity::new);
        if (entity.getCreatedAt() == null) {
            entity = DatosGeneralesCotizacionEntity.fromDomain(datosGeneralesCotizacion);
        } else {
            entity.setCotizacionId(datosGeneralesCotizacion.cotizacionId());
            entity.setTipoDocumento(datosGeneralesCotizacion.datosAsegurado().tipoDocumento());
            entity.setNumeroDocumento(datosGeneralesCotizacion.datosAsegurado().numeroDocumento());
            entity.setNombreORazonSocial(datosGeneralesCotizacion.datosAsegurado().nombreORazonSocial());
            entity.setCorreoElectronico(datosGeneralesCotizacion.datosAsegurado().correoElectronico());
            entity.setTelefono(datosGeneralesCotizacion.datosAsegurado().telefono());
            entity.setCodigoAgente(datosGeneralesCotizacion.datosConduccion().codigoAgente());
            entity.setClasificacionRiesgo(datosGeneralesCotizacion.datosConduccion().clasificacionRiesgo());
            entity.setTipoNegocio(datosGeneralesCotizacion.datosConduccion().tipoNegocio());
        }
        DatosGeneralesCotizacionEntity saved = repository.save(entity);
        repository.flush();
        return saved.toDomain();
    }
}