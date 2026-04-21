package com.sofka.plataforma_danos_back.folios.domain.port;

import com.sofka.plataforma_danos_back.folios.domain.ConfiguracionLayout;

import java.util.Optional;

public interface ConfiguracionLayoutRepository {
    Optional<ConfiguracionLayout> findByCotizacionId(Long cotizacionId);

    boolean existsByCotizacionId(Long cotizacionId);

    ConfiguracionLayout save(ConfiguracionLayout configuracionLayout);
}