package com.sofka.plataforma_danos_back.folios.application;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sofka.plataforma_danos_back.folios.application.dto.QuoteStateResponse;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.AlertaBloqueante;
import com.sofka.plataforma_danos_back.folios.domain.ConfiguracionLayout;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.DatosGeneralesCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.EstadoCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.EstadoValidacion;
import com.sofka.plataforma_danos_back.folios.domain.LayoutUbicacionSlot;
import com.sofka.plataforma_danos_back.folios.domain.PrimaPorUbicacion;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.port.ConfiguracionLayoutRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.CoverageOptionsRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.CotizacionRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.DatosGeneralesCotizacionRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.PrimaPorUbicacionRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.UbicacionCotizacionRepository;

@Service
public class GetQuoteStateUseCase {

    private final CotizacionRepository cotizacionRepository;
    private final DatosGeneralesCotizacionRepository datosGeneralesCotizacionRepository;
    private final ConfiguracionLayoutRepository configuracionLayoutRepository;
    private final UbicacionCotizacionRepository ubicacionCotizacionRepository;
    private final CoverageOptionsRepository coverageOptionsRepository;
    private final PrimaPorUbicacionRepository primaPorUbicacionRepository;

    public GetQuoteStateUseCase(CotizacionRepository cotizacionRepository) {
        this(cotizacionRepository, null, null, null, null, null);
    }

    @Autowired
    public GetQuoteStateUseCase(
            CotizacionRepository cotizacionRepository,
            DatosGeneralesCotizacionRepository datosGeneralesCotizacionRepository,
            ConfiguracionLayoutRepository configuracionLayoutRepository,
            UbicacionCotizacionRepository ubicacionCotizacionRepository,
            CoverageOptionsRepository coverageOptionsRepository,
            PrimaPorUbicacionRepository primaPorUbicacionRepository
    ) {
        this.cotizacionRepository = Objects.requireNonNull(cotizacionRepository, "cotizacionRepository es obligatorio");
        this.datosGeneralesCotizacionRepository = datosGeneralesCotizacionRepository;
        this.configuracionLayoutRepository = configuracionLayoutRepository;
        this.ubicacionCotizacionRepository = ubicacionCotizacionRepository;
        this.coverageOptionsRepository = coverageOptionsRepository;
        this.primaPorUbicacionRepository = primaPorUbicacionRepository;
    }

    @Transactional(readOnly = true)
    public QuoteStateResponse handle(String numeroFolio) {
        Cotizacion cotizacion = cotizacionRepository.findByNumeroFolio(numeroFolio)
                .orElseThrow(() -> new QuoteNotFoundException(numeroFolio));

        DatosGeneralesCotizacion datosGenerales = findGeneralInfo(cotizacion.id());
        ConfiguracionLayout configuracionLayout = findLocationsLayout(cotizacion.id());
        List<UbicacionCotizacion> ubicaciones = findLocations(cotizacion.id());
        List<PrimaPorUbicacion> primasPorUbicacion = findFinancialDetails(cotizacion.id());
        boolean hasCoverageOptions = hasCoverageOptions(cotizacion.id());

        QuoteStateResponse.EstadoSeccion progresoDatosGenerales = datosGenerales == null
                ? QuoteStateResponse.EstadoSeccion.INCOMPLETE
                : QuoteStateResponse.EstadoSeccion.COMPLETED;

        List<Integer> indicesEsperados = resolveExpectedIndices(configuracionLayout);
        QuoteStateResponse.EstadoSeccion progresoLayout = indicesEsperados.isEmpty()
                ? QuoteStateResponse.EstadoSeccion.INCOMPLETE
                : QuoteStateResponse.EstadoSeccion.COMPLETED;

        Map<Integer, UbicacionCotizacion> ubicacionesPorIndice = ubicaciones.stream()
                .filter(ubicacion -> ubicacion.detalle() != null && ubicacion.detalle().indice() != null)
                .collect(Collectors.toMap(
                        ubicacion -> ubicacion.detalle().indice(),
                        ubicacion -> ubicacion,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        List<EstadoValidacion> estadosNormalizados = indicesEsperados.stream()
                .map(indice -> ubicacionesPorIndice.containsKey(indice)
                        ? ubicacionesPorIndice.get(indice).estadoValidacion()
                        : EstadoValidacion.EMPTY)
                .toList();

        QuoteStateResponse.EstadoSeccion progresoUbicaciones = isLocationsSectionCompleted(estadosNormalizados)
                ? QuoteStateResponse.EstadoSeccion.COMPLETED
                : QuoteStateResponse.EstadoSeccion.INCOMPLETE;

        QuoteStateResponse.EstadoSeccion progresoOpcionesCobertura = hasCoverageOptions
            ? QuoteStateResponse.EstadoSeccion.COMPLETED
            : QuoteStateResponse.EstadoSeccion.INCOMPLETE;

        QuoteStateResponse.ResumenUbicaciones resumenUbicaciones = buildLocationsSummary(indicesEsperados.size(), ubicaciones);
        List<QuoteStateResponse.AlertaVigente> alertasVigentes = aggregateAlerts(ubicaciones);
        boolean readyToCalculate = hasCoverageOptions && ubicaciones.stream().anyMatch(ubicacion -> ubicacion.estadoValidacion() == EstadoValidacion.CALCULABLE);
        EstadoCotizacion estadoCotizacion = resolveQuoteState(
                cotizacion,
                progresoDatosGenerales,
                progresoLayout,
                progresoOpcionesCobertura,
                resumenUbicaciones.totalActual(),
                readyToCalculate
        );

        return new QuoteStateResponse(
                cotizacion.numeroFolio(),
                estadoCotizacion,
                cotizacion.version(),
                cotizacion.fechaUltimaActualizacion(),
                new QuoteStateResponse.ProgresoCotizacion(
                        progresoDatosGenerales,
                        progresoLayout,
                        progresoUbicaciones,
                        progresoOpcionesCobertura
                ),
                resumenUbicaciones,
                !alertasVigentes.isEmpty(),
                alertasVigentes,
                readyToCalculate,
                QuoteStateResponse.ResultadoFinancieroResumen.from(cotizacion, primasPorUbicacion)
        );
    }

    private boolean hasCoverageOptions(Long cotizacionId) {
        if (coverageOptionsRepository == null) {
            return false;
        }
        return coverageOptionsRepository.findByCotizacionId(cotizacionId)
                .map(coverageOptions -> !coverageOptions.garantiasSeleccionadas().isEmpty())
                .orElse(false);
    }

    private List<PrimaPorUbicacion> findFinancialDetails(Long cotizacionId) {
        if (primaPorUbicacionRepository == null) {
            return List.of();
        }
        return primaPorUbicacionRepository.findAllByCotizacionId(cotizacionId);
    }

    private DatosGeneralesCotizacion findGeneralInfo(Long cotizacionId) {
        if (datosGeneralesCotizacionRepository == null) {
            return null;
        }
        return datosGeneralesCotizacionRepository.findByCotizacionId(cotizacionId).orElse(null);
    }

    private ConfiguracionLayout findLocationsLayout(Long cotizacionId) {
        if (configuracionLayoutRepository == null) {
            return null;
        }
        return configuracionLayoutRepository.findByCotizacionId(cotizacionId).orElse(null);
    }

    private List<UbicacionCotizacion> findLocations(Long cotizacionId) {
        if (ubicacionCotizacionRepository == null) {
            return List.of();
        }
        return ubicacionCotizacionRepository.findAllByCotizacionId(cotizacionId);
    }

    private List<Integer> resolveExpectedIndices(ConfiguracionLayout configuracionLayout) {
        if (configuracionLayout == null) {
            return List.of();
        }

        List<Integer> indicesDesdeSlots = configuracionLayout.ubicaciones().stream()
                .sorted(Comparator.comparingInt(slot -> slot.ordenCaptura() == null ? Integer.MAX_VALUE : slot.ordenCaptura()))
                .map(LayoutUbicacionSlot::indice)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (!indicesDesdeSlots.isEmpty()) {
            return indicesDesdeSlots;
        }

        Integer cantidadUbicaciones = configuracionLayout.cantidadUbicaciones();
        if (cantidadUbicaciones == null || cantidadUbicaciones <= 0) {
            return List.of();
        }

        return IntStream.rangeClosed(1, cantidadUbicaciones).boxed().toList();
    }

    private boolean isLocationsSectionCompleted(List<EstadoValidacion> estadosNormalizados) {
        return !estadosNormalizados.isEmpty() && estadosNormalizados.stream()
                .allMatch(estado -> estado == EstadoValidacion.VALID || estado == EstadoValidacion.CALCULABLE);
    }

    private QuoteStateResponse.ResumenUbicaciones buildLocationsSummary(int totalEsperado, List<UbicacionCotizacion> ubicaciones) {
        int calculables = countByState(ubicaciones, EstadoValidacion.CALCULABLE);
        int incompletas = countByState(ubicaciones, EstadoValidacion.INCOMPLETE);
        int invalidas = countByState(ubicaciones, EstadoValidacion.INVALID);
        int conAlertas = (int) ubicaciones.stream().filter(ubicacion -> !ubicacion.alertasBloqueantes().isEmpty()).count();

        return new QuoteStateResponse.ResumenUbicaciones(
                totalEsperado,
                ubicaciones.size(),
                calculables,
                incompletas,
                invalidas,
                conAlertas
        );
    }

    private int countByState(List<UbicacionCotizacion> ubicaciones, EstadoValidacion estado) {
        return (int) ubicaciones.stream().filter(ubicacion -> ubicacion.estadoValidacion() == estado).count();
    }

    private List<QuoteStateResponse.AlertaVigente> aggregateAlerts(List<UbicacionCotizacion> ubicaciones) {
        return ubicaciones.stream()
                .flatMap(ubicacion -> ubicacion.alertasBloqueantes().stream())
                .collect(Collectors.toMap(
                        this::alertKey,
                        alerta -> new QuoteStateResponse.AlertaVigente(alerta.codigo(), alerta.mensaje(), alerta.severidad()),
                        (left, right) -> left,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();
    }

    private String alertKey(AlertaBloqueante alerta) {
        return String.join("|",
                Objects.toString(alerta.codigo(), ""),
                Objects.toString(alerta.mensaje(), ""),
                Objects.toString(alerta.severidad(), "")
        );
    }

    private EstadoCotizacion resolveQuoteState(
            Cotizacion cotizacion,
            QuoteStateResponse.EstadoSeccion progresoDatosGenerales,
            QuoteStateResponse.EstadoSeccion progresoLayout,
            QuoteStateResponse.EstadoSeccion progresoOpcionesCobertura,
            int totalUbicacionesPersistidas,
            boolean readyToCalculate
    ) {
        if (hasPersistedFinancialTotals(cotizacion)) {
            return EstadoCotizacion.CALCULADA;
        }
        if (readyToCalculate) {
            return EstadoCotizacion.LISTA_PARA_CALCULO;
        }
        if (progresoDatosGenerales == QuoteStateResponse.EstadoSeccion.COMPLETED
                || progresoLayout == QuoteStateResponse.EstadoSeccion.COMPLETED
                || progresoOpcionesCobertura == QuoteStateResponse.EstadoSeccion.COMPLETED
                || totalUbicacionesPersistidas > 0
                || cotizacion.version() > 0) {
            return EstadoCotizacion.EN_CAPTURA;
        }
        return EstadoCotizacion.BORRADOR;
    }

    private boolean hasPersistedFinancialTotals(Cotizacion cotizacion) {
        return cotizacion.primaNeta() != null && cotizacion.primaComercial() != null;
    }
}
