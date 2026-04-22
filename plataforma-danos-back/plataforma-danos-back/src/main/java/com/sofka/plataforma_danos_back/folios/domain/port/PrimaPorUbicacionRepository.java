package com.sofka.plataforma_danos_back.folios.domain.port;

import java.util.List;

import com.sofka.plataforma_danos_back.folios.domain.PrimaPorUbicacion;

public interface PrimaPorUbicacionRepository {
    List<PrimaPorUbicacion> findAllByCotizacionId(Long cotizacionId);

    List<PrimaPorUbicacion> saveAll(Long cotizacionId, List<PrimaPorUbicacion> primasPorUbicacion);

    void deleteByCotizacionId(Long cotizacionId);
}