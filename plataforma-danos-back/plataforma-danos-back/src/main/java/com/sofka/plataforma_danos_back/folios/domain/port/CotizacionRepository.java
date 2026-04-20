package com.sofka.plataforma_danos_back.folios.domain.port;

import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;

import java.util.Optional;

public interface CotizacionRepository {
    Cotizacion save(Cotizacion cotizacion);

    Optional<Cotizacion> findByNumeroFolio(String numeroFolio);
}
