package com.sofka.plataforma_danos_back.folios.application;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sofka.plataforma_danos_back.folios.domain.CoverageGuaranteePreview;
import com.sofka.plataforma_danos_back.folios.domain.CoverageProjectionPerLocation;
import com.sofka.plataforma_danos_back.folios.domain.SelectedGuarantee;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.port.CoverageGuaranteeCatalogPort;

@Service
public class CoverageOptionsProjectionService {

    private final CoverageGuaranteeCatalogPort coverageGuaranteeCatalogPort;

    public CoverageOptionsProjectionService(CoverageGuaranteeCatalogPort coverageGuaranteeCatalogPort) {
        this.coverageGuaranteeCatalogPort = coverageGuaranteeCatalogPort;
    }

    public List<CoverageProjectionPerLocation> buildProjection(List<UbicacionCotizacion> ubicaciones, List<SelectedGuarantee> garantiasSeleccionadas) {
        if (ubicaciones == null || ubicaciones.isEmpty() || garantiasSeleccionadas == null || garantiasSeleccionadas.isEmpty()) {
            return List.of();
        }

        return ubicaciones.stream()
                .sorted(Comparator.comparingInt(ubicacion -> ubicacion.detalle().indice()))
                .map(ubicacion -> buildProjection(ubicacion, garantiasSeleccionadas))
                .toList();
    }

    private CoverageProjectionPerLocation buildProjection(UbicacionCotizacion ubicacion, List<SelectedGuarantee> garantiasSeleccionadas) {
        List<CoverageGuaranteePreview> garantiasDerivadas = garantiasSeleccionadas.stream()
                .map(guarantee -> coverageGuaranteeCatalogPort.resolvePreview(guarantee.garantiaCode(), ubicacion))
                .toList();
        boolean calculablePreview = garantiasDerivadas.stream().anyMatch(CoverageGuaranteePreview::tariffablePreview);
        return new CoverageProjectionPerLocation(ubicacion.detalle().indice(), garantiasDerivadas, calculablePreview);
    }
}