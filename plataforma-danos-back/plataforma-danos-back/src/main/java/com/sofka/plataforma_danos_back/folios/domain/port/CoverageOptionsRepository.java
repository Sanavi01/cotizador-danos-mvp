package com.sofka.plataforma_danos_back.folios.domain.port;

import java.util.Optional;

import com.sofka.plataforma_danos_back.folios.domain.CoverageOptions;

public interface CoverageOptionsRepository {
    Optional<CoverageOptions> findByCotizacionId(Long cotizacionId);

    CoverageOptions save(CoverageOptions coverageOptions);
}