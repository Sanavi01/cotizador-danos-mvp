package com.sofka.plataforma_danos_back.folios.entrypoints.controller;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sofka.plataforma_danos_back.common.error.ApiExceptionHandler;
import com.sofka.plataforma_danos_back.folios.application.GetLocationsSummaryUseCase;
import com.sofka.plataforma_danos_back.folios.application.GetLocationsUseCase;
import com.sofka.plataforma_danos_back.folios.application.UpdateLocationUseCase;
import com.sofka.plataforma_danos_back.folios.application.UpdateLocationsUseCase;
import com.sofka.plataforma_danos_back.folios.application.dto.LocationResponse;
import com.sofka.plataforma_danos_back.folios.application.dto.LocationsResponse;
import com.sofka.plataforma_danos_back.folios.application.dto.LocationsSummaryResponse;
import com.sofka.plataforma_danos_back.folios.application.dto.UpdateLocationRequest;
import com.sofka.plataforma_danos_back.folios.application.dto.UpdateLocationsRequest;
import com.sofka.plataforma_danos_back.folios.application.exception.InvalidLocationsPayloadException;
import com.sofka.plataforma_danos_back.folios.application.exception.LocationNotFoundException;
import com.sofka.plataforma_danos_back.folios.application.exception.LocationVersionConflictException;
import com.sofka.plataforma_danos_back.folios.application.exception.LocationsLayoutValidationException;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.AlertaBloqueante;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.EstadoCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.EstadoValidacion;
import com.sofka.plataforma_danos_back.folios.domain.Giro;
import com.sofka.plataforma_danos_back.folios.domain.LayoutUbicacionSlot;
import com.sofka.plataforma_danos_back.folios.domain.ModoCaptura;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionDetalle;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionEvaluacion;
import com.sofka.plataforma_danos_back.folios.domain.ZonaCatastrofica;

@ExtendWith(MockitoExtension.class)
class LocationsControllerTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-04-21T00:00:00Z");

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private GetLocationsUseCase getLocationsUseCase;

    @Mock
    private UpdateLocationsUseCase updateLocationsUseCase;

    @Mock
    private UpdateLocationUseCase updateLocationUseCase;

    @Mock
    private GetLocationsSummaryUseCase getLocationsSummaryUseCase;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = standaloneSetup(new LocationsController(
                getLocationsUseCase,
                updateLocationsUseCase,
                updateLocationUseCase,
                getLocationsSummaryUseCase
        )).setControllerAdvice(new ApiExceptionHandler()).build();
    }

    @Test
    void getLocations_returns200WithDataEnvelope() throws Exception {
        LocationsResponse response = locationsResponse();
        when(getLocationsUseCase.handle("1000001")).thenReturn(response);

        mockMvc.perform(get("/v1/quotes/{folio}/locations", "1000001"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.numeroFolio").value("1000001"))
                .andExpect(jsonPath("$.data.version").value(3))
                .andExpect(jsonPath("$.data.ubicaciones[0].indice").value(1))
                .andExpect(jsonPath("$.data.ubicaciones[0].estadoValidacion").value("CALCULABLE"))
                .andExpect(jsonPath("$.data.ubicaciones[0].garantias[0].garantiaCode").value("GAR-INC-ED"))
                .andExpect(jsonPath("$.data.ubicaciones[1].indice").value(2))
                .andExpect(jsonPath("$.data.ubicaciones[1].estadoValidacion").value("INVALID"));
    }

    @Test
    void getLocations_returns404WhenFolioDoesNotExist() throws Exception {
        when(getLocationsUseCase.handle("9999999")).thenThrow(new QuoteNotFoundException("9999999"));

        mockMvc.perform(get("/v1/quotes/{folio}/locations", "9999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Cotizacion no encontrada"))
                .andExpect(jsonPath("$.detail").value("No existe una cotizacion con numeroFolio 9999999"));
    }

    @Test
    void updateLocations_returns200WithDataEnvelope() throws Exception {
        UpdateLocationsRequest request = validUpdateLocationsRequest();
        LocationsResponse response = locationsResponse();
        when(updateLocationsUseCase.handle(eq("1000001"), any(UpdateLocationsRequest.class))).thenReturn(response);

        mockMvc.perform(put("/v1/quotes/{folio}/locations", "1000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.numeroFolio").value("1000001"))
                .andExpect(jsonPath("$.data.version").value(3))
                .andExpect(jsonPath("$.data.ubicaciones[0].indice").value(1))
                .andExpect(jsonPath("$.data.ubicaciones[1].indice").value(2));
    }

    @Test
    void updateLocations_returns400WhenBodyFailsValidation() throws Exception {
        mockMvc.perform(put("/v1/quotes/{folio}/locations", "1000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validacion invalida"))
                .andExpect(jsonPath("$.detail").value("La solicitud contiene campos invalidos"));
    }

    @Test
    void updateLocations_returns400WhenRequestContainsDuplicateIndices() throws Exception {
        UpdateLocationsRequest request = new UpdateLocationsRequest(
                1L,
                List.of(
                        new UpdateLocationsRequest.LocationUpsertRequest(1, "Planta", null, "110111", null, null, null, null, null, null, null, null, null),
                        new UpdateLocationsRequest.LocationUpsertRequest(1, "Bodega", null, "050001", null, null, null, null, null, null, null, null, null)
                )
        );

        when(updateLocationsUseCase.handle(eq("1000001"), any(UpdateLocationsRequest.class)))
                .thenThrow(new InvalidLocationsPayloadException("No se permiten indices duplicados en la solicitud"));

        mockMvc.perform(put("/v1/quotes/{folio}/locations", "1000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Solicitud invalida"))
                .andExpect(jsonPath("$.detail").value("No se permiten indices duplicados en la solicitud"));
    }

    @Test
    void updateLocations_returns409WhenVersionConflictOccurs() throws Exception {
        UpdateLocationsRequest request = validUpdateLocationsRequest();

        when(updateLocationsUseCase.handle(eq("1000001"), any(UpdateLocationsRequest.class)))
                .thenThrow(new LocationVersionConflictException("1000001", 1L, 2L));

        mockMvc.perform(put("/v1/quotes/{folio}/locations", "1000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflicto de concurrencia"))
                .andExpect(jsonPath("$.detail").value("La cotizacion 1000001 tiene version 1 y no coincide con la version actual 2"));
    }

    @Test
    void updateLocations_returns422WhenLayoutRuleFails() throws Exception {
        UpdateLocationsRequest request = validUpdateLocationsRequest();

        when(updateLocationsUseCase.handle(eq("1000001"), any(UpdateLocationsRequest.class)))
                .thenThrow(new LocationsLayoutValidationException("El indice de una ubicacion excede el rango permitido por el layout"));

        mockMvc.perform(put("/v1/quotes/{folio}/locations", "1000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Layout de ubicaciones invalido"))
                .andExpect(jsonPath("$.detail").value("El indice de una ubicacion excede el rango permitido por el layout"));
    }

    @Test
    void updateLocation_returns200WithDataEnvelope() throws Exception {
        UpdateLocationRequest request = validUpdateLocationRequest();
        LocationResponse response = locationResponse();
        when(updateLocationUseCase.handle(eq("1000001"), eq(2), any(UpdateLocationRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/v1/quotes/{folio}/locations/{indice}", "1000001", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.numeroFolio").value("1000001"))
                .andExpect(jsonPath("$.data.version").value(4))
                .andExpect(jsonPath("$.data.ubicacion.indice").value(2))
                .andExpect(jsonPath("$.data.ubicacion.estadoValidacion").value("VALID"))
                .andExpect(jsonPath("$.data.ubicacion.zonaCatastrofica.zonaTev").value("Z-TEV-02"));
    }

    @Test
    void updateLocation_returns404WhenLocationDoesNotExist() throws Exception {
        UpdateLocationRequest request = validUpdateLocationRequest();

        when(updateLocationUseCase.handle(eq("1000001"), eq(2), any(UpdateLocationRequest.class)))
                .thenThrow(new LocationNotFoundException("1000001", 2));

        mockMvc.perform(patch("/v1/quotes/{folio}/locations/{indice}", "1000001", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Ubicacion no encontrada"))
                .andExpect(jsonPath("$.detail").value("No existe una ubicacion con indice 2 en la cotizacion 1000001"));
    }

    @Test
    void updateLocation_returns409WhenVersionConflictOccurs() throws Exception {
        UpdateLocationRequest request = validUpdateLocationRequest();

        when(updateLocationUseCase.handle(eq("1000001"), eq(2), any(UpdateLocationRequest.class)))
                .thenThrow(new LocationVersionConflictException("1000001", 3L, 4L));

        mockMvc.perform(patch("/v1/quotes/{folio}/locations/{indice}", "1000001", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflicto de concurrencia"))
                .andExpect(jsonPath("$.detail").value("La cotizacion 1000001 tiene version 3 y no coincide con la version actual 4"));
    }

    @Test
    void updateLocation_returns422WhenLayoutRuleFails() throws Exception {
        UpdateLocationRequest request = validUpdateLocationRequest();

        when(updateLocationUseCase.handle(eq("1000001"), eq(2), any(UpdateLocationRequest.class)))
                .thenThrow(new LocationsLayoutValidationException("El indice de una ubicacion excede el rango permitido por el layout"));

        mockMvc.perform(patch("/v1/quotes/{folio}/locations/{indice}", "1000001", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Layout de ubicaciones invalido"))
                .andExpect(jsonPath("$.detail").value("El indice de una ubicacion excede el rango permitido por el layout"));
    }

    @Test
    void getLocationsSummary_returns200WithDataEnvelope() throws Exception {
        LocationsSummaryResponse response = summaryResponse();
        when(getLocationsSummaryUseCase.handle("1000001")).thenReturn(response);

        mockMvc.perform(get("/v1/quotes/{folio}/locations/summary", "1000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.numeroFolio").value("1000001"))
                .andExpect(jsonPath("$.data.totalEsperado").value(3))
                .andExpect(jsonPath("$.data.totalActual").value(2))
                .andExpect(jsonPath("$.data.calculables").value(1))
                .andExpect(jsonPath("$.data.invalidas").value(1))
                .andExpect(jsonPath("$.data.resumenPorIndice[2].estadoValidacion").value("EMPTY"));
    }

    @Test
    void getLocationsSummary_returns404WhenFolioDoesNotExist() throws Exception {
        when(getLocationsSummaryUseCase.handle("9999999")).thenThrow(new QuoteNotFoundException("9999999"));

        mockMvc.perform(get("/v1/quotes/{folio}/locations/summary", "9999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Cotizacion no encontrada"))
                .andExpect(jsonPath("$.detail").value("No existe una cotizacion con numeroFolio 9999999"));
    }

    private static LocationsResponse locationsResponse() {
        Cotizacion cotizacion = Cotizacion.nueva("1000001", FIXED_NOW).withPersistence(13L, 3L, FIXED_NOW);
        return LocationsResponse.from(cotizacion, List.of(
                validLocation(1),
                invalidLocation(2)
        ));
    }

    private static LocationResponse locationResponse() {
        Cotizacion cotizacion = Cotizacion.nueva("1000001", FIXED_NOW).withPersistence(13L, 4L, FIXED_NOW);
        return LocationResponse.from(cotizacion, validUpdatedLocation());
    }

    private static LocationsSummaryResponse summaryResponse() {
        List<LayoutUbicacionSlot> slots = List.of(
                new LayoutUbicacionSlot(1, 1),
                new LayoutUbicacionSlot(2, 2),
                new LayoutUbicacionSlot(3, 3)
        );
        return LocationsSummaryResponse.from("1000001", slots, List.of(
                validLocation(1),
                invalidLocation(2)
        ));
    }

    private static UbicacionCotizacion validLocation(int indice) {
        return new UbicacionCotizacion(
                (long) indice,
                13L,
                new UbicacionDetalle(
                        indice,
                        indice == 1 ? "Planta principal" : "Bodega secundaria",
                        indice == 1 ? "Calle 100 # 10-10" : null,
                        indice == 1 ? "110111" : "000000",
                        indice == 1 ? "Bogota D.C." : null,
                        indice == 1 ? "Bogota" : null,
                        indice == 1 ? "Chapinero" : null,
                        indice == 1 ? "Bogota" : null,
                        indice == 1 ? "CONCRETO" : null,
                        indice == 1 ? 1 : null,
                        indice == 1 ? 2018 : null,
                        indice == 1 ? new Giro("GIRO-001", "Manufactura ligera", "CI-001") : null,
                        indice == 1 ? new ZonaCatastrofica("Z-TEV-01", "Z-FHM-01") : null
                ),
                EstadoValidacion.CALCULABLE,
                List.of(),
                FIXED_NOW,
                FIXED_NOW
        );
    }

    private static UbicacionCotizacion invalidLocation(int indice) {
        return new UbicacionCotizacion(
                20L,
                13L,
                new UbicacionDetalle(
                        indice,
                        "Bodega secundaria",
                        null,
                        "000000",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                EstadoValidacion.INVALID,
                List.of(new AlertaBloqueante(
                        "UBICACION_SIN_ZIP",
                        "La ubicacion no tiene codigo postal valido.",
                        "Warning"
                )),
                FIXED_NOW,
                FIXED_NOW
        );
    }

    private static UbicacionCotizacion validUpdatedLocation() {
        return new UbicacionCotizacion(
                20L,
                13L,
                new UbicacionDetalle(
                        2,
                        "Bodega secundaria",
                        null,
                        "050001",
                        "Antioquia",
                        "Medellin",
                        null,
                        "Medellin",
                        null,
                        null,
                        null,
                        new Giro("GIRO-002", "Bodega", "CI-010"),
                        new ZonaCatastrofica("Z-TEV-02", "Z-FHM-01")
                ),
                EstadoValidacion.VALID,
                List.of(),
                FIXED_NOW,
                FIXED_NOW
        );
    }

    private static UpdateLocationsRequest validUpdateLocationsRequest() {
        return new UpdateLocationsRequest(
                3L,
                List.of(
                        new UpdateLocationsRequest.LocationUpsertRequest(
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
                                new UpdateLocationsRequest.GiroRequest("GIRO-001", "Manufactura ligera", "CI-001"),
                                null
                        ),
                        new UpdateLocationsRequest.LocationUpsertRequest(
                                2,
                                "Bodega secundaria",
                                null,
                                "000000",
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null
                        )
                )
        );
    }

    private static UpdateLocationRequest validUpdateLocationRequest() {
        return new UpdateLocationRequest(
                3L,
                new UpdateLocationRequest.LocationChangesRequest(
                        "Bodega secundaria",
                        null,
                        "050001",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        new UpdateLocationRequest.GiroRequest("GIRO-002", "Bodega", "CI-010"),
                        null
                )
        );
    }
}