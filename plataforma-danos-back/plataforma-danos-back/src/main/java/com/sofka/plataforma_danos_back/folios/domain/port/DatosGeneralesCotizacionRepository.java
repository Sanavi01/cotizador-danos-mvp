package com.sofka.plataforma_danos_back.folios.domain.port;

import java.util.Optional;

import com.sofka.plataforma_danos_back.folios.domain.DatosGeneralesCotizacion;

public interface DatosGeneralesCotizacionRepository {
    Optional<DatosGeneralesCotizacion> findByCotizacionId(Long cotizacionId);

    boolean existsByCotizacionId(Long cotizacionId);

    DatosGeneralesCotizacion save(DatosGeneralesCotizacion datosGeneralesCotizacion);
}