package com.sofka.plataforma_danos_back.folios.domain.port;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.sofka.plataforma_danos_back.folios.domain.UbicacionCotizacion;

public interface UbicacionCotizacionRepository {
    List<UbicacionCotizacion> findAllByCotizacionId(Long cotizacionId);

    Optional<UbicacionCotizacion> findByCotizacionIdAndIndice(Long cotizacionId, Integer indice);

    List<UbicacionCotizacion> saveAll(List<UbicacionCotizacion> ubicaciones);

    UbicacionCotizacion save(UbicacionCotizacion ubicacion);

    void deleteByCotizacionIdAndIndiceNotIn(Long cotizacionId, Collection<Integer> indices);
}