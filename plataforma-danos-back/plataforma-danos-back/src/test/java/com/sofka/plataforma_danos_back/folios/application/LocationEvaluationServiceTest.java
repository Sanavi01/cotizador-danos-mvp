package com.sofka.plataforma_danos_back.folios.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.sofka.plataforma_danos_back.folios.domain.EstadoValidacion;
import com.sofka.plataforma_danos_back.folios.domain.Giro;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionDetalle;

class LocationEvaluationServiceTest {

    private final LocationEvaluationService service = new LocationEvaluationService();

    @Test
    void evaluate_returnsEmptyWhenLocationHasNoData() {
        var result = service.evaluate(new UbicacionDetalle(1, null, null, null, null, null, null, null, null, null, null, null, null));

        assertEquals(EstadoValidacion.EMPTY, result.estadoValidacion());
        assertEquals(0, result.alertasBloqueantes().size());
    }

    @Test
    void evaluate_returnsIncompleteWhenDraftDataIsPartial() {
        var result = service.evaluate(new UbicacionDetalle(1, "Planta principal", null, null, null, null, null, null, null, null, null, null, null));

        assertEquals(EstadoValidacion.INCOMPLETE, result.estadoValidacion());
        assertEquals("Planta principal", result.detalle().nombreUbicacion());
    }

    @Test
    void evaluate_returnsInvalidWhenPostalCodeIsInvalid() {
        var result = service.evaluate(new UbicacionDetalle(2, "Bodega secundaria", null, "000000", null, null, null, null, null, null, null, null, null));

        assertEquals(EstadoValidacion.INVALID, result.estadoValidacion());
        assertEquals(1, result.alertasBloqueantes().size());
        assertEquals("UBICACION_SIN_ZIP", result.alertasBloqueantes().get(0).codigo());
    }

    @Test
    void evaluate_returnsIncompleteWhenTechnicalFieldsAreMissingEvenWithValidPostalCode() {
        var result = service.evaluate(new UbicacionDetalle(
                1,
                "Planta principal",
                "Calle 100 # 10-10",
                "110111",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        ));

        assertEquals(EstadoValidacion.INCOMPLETE, result.estadoValidacion());
        assertEquals("Bogota D.C.", result.detalle().estado());
        assertEquals(0, result.alertasBloqueantes().size());
    }

    @Test
    void evaluate_returnsValidWhenStructureIsCompleteButGiroIsMissing() {
        var result = service.evaluate(new UbicacionDetalle(
                1,
                "Planta principal",
                "Calle 100 # 10-10",
                "110111",
                null,
                null,
                null,
                null,
                "CONCRETO",
                1,
                2018,
                null,
                null
        ));

        assertEquals(EstadoValidacion.VALID, result.estadoValidacion());
        assertEquals("Bogota D.C.", result.detalle().estado());
        assertEquals(0, result.alertasBloqueantes().size());
    }

    @Test
    void evaluate_returnsCalculableWhenTechnicalDataIsComplete() {
        var result = service.evaluate(new UbicacionDetalle(
                1,
                "Planta principal",
                "Calle 100 # 10-10",
                "110111",
                null,
                null,
                null,
                null,
                "CONCRETO",
                1,
                2018,
                new Giro("GIRO-001", "Manufactura ligera", "CI-001"),
                null
        ));

        assertEquals(EstadoValidacion.CALCULABLE, result.estadoValidacion());
        assertEquals("Bogota D.C.", result.detalle().estado());
        assertEquals("Z-TEV-01", result.detalle().zonaCatastrofica().zonaTev());
        assertEquals(0, result.alertasBloqueantes().size());
    }
}