package com.sofka.plataforma_danos_back.folios.domain.port;

import java.util.Optional;

import com.sofka.plataforma_danos_back.folios.domain.CoverageGuaranteeDefinition;
import com.sofka.plataforma_danos_back.folios.domain.CoverageGuaranteePreview;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionCotizacion;

public interface CoverageGuaranteeCatalogPort {
    Optional<CoverageGuaranteeDefinition> findByCode(String garantiaCode);

    CoverageGuaranteePreview resolvePreview(String garantiaCode, UbicacionCotizacion ubicacion);
}