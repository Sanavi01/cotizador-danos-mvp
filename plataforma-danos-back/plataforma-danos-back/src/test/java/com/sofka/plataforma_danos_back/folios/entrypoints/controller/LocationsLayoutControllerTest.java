package com.sofka.plataforma_danos_back.folios.entrypoints.controller;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sofka.plataforma_danos_back.common.error.ApiExceptionHandler;
import com.sofka.plataforma_danos_back.folios.application.GetLocationsLayoutUseCase;
import com.sofka.plataforma_danos_back.folios.application.UpdateLocationsLayoutUseCase;
import com.sofka.plataforma_danos_back.folios.application.dto.LocationsLayoutResponse;
import com.sofka.plataforma_danos_back.folios.application.dto.UpdateLocationsLayoutRequest;
import com.sofka.plataforma_danos_back.folios.application.exception.LocationsLayoutValidationException;
import com.sofka.plataforma_danos_back.folios.application.exception.LocationsLayoutVersionConflictException;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.ModoCaptura;

@ExtendWith(MockitoExtension.class)
class LocationsLayoutControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private GetLocationsLayoutUseCase getLocationsLayoutUseCase;

    @Mock
    private UpdateLocationsLayoutUseCase updateLocationsLayoutUseCase;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = standaloneSetup(new LocationsLayoutController(getLocationsLayoutUseCase, updateLocationsLayoutUseCase))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void getLocationsLayout_returns200WithDataEnvelope() throws Exception {
        Instant now = Instant.parse("2026-04-21T00:00:00Z");
        LocationsLayoutResponse response = new LocationsLayoutResponse(
                "1000001",
                3L,
                now,
                new LocationsLayoutResponse.ConfiguracionLayoutView(
                        ModoCaptura.MULTIPLE,
                        3,
                        List.of(
                                new LocationsLayoutResponse.LayoutUbicacionSlotView(1, 1),
                                new LocationsLayoutResponse.LayoutUbicacionSlotView(2, 2),
                                new LocationsLayoutResponse.LayoutUbicacionSlotView(3, 3)
                        )
                )
        );

        when(getLocationsLayoutUseCase.handle("1000001")).thenReturn(response);

        mockMvc.perform(get("/v1/quotes/{folio}/locations/layout", "1000001"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.numeroFolio").value("1000001"))
                .andExpect(jsonPath("$.data.version").value(3))
                .andExpect(jsonPath("$.data.fechaUltimaActualizacion").value("2026-04-21T00:00:00Z"))
                .andExpect(jsonPath("$.data.configuracionLayout.modoCaptura").value("MULTIPLE"))
                .andExpect(jsonPath("$.data.configuracionLayout.cantidadUbicaciones").value(3))
                .andExpect(jsonPath("$.data.configuracionLayout.ubicaciones[0].indice").value(1))
                .andExpect(jsonPath("$.data.configuracionLayout.ubicaciones[0].ordenCaptura").value(1));
    }

    @Test
    void getLocationsLayout_returns404WhenFolioDoesNotExist() throws Exception {
        when(getLocationsLayoutUseCase.handle("9999999")).thenThrow(new QuoteNotFoundException("9999999"));

        mockMvc.perform(get("/v1/quotes/{folio}/locations/layout", "9999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Cotizacion no encontrada"))
                .andExpect(jsonPath("$.detail").value("No existe una cotizacion con numeroFolio 9999999"));
    }

    @Test
    void updateLocationsLayout_returns200WithDataEnvelope() throws Exception {
        Instant now = Instant.parse("2026-04-21T00:00:00Z");
        LocationsLayoutResponse response = new LocationsLayoutResponse(
                "1000001",
                2L,
                now,
                new LocationsLayoutResponse.ConfiguracionLayoutView(
                        ModoCaptura.MULTIPLE,
                        3,
                        List.of(
                                new LocationsLayoutResponse.LayoutUbicacionSlotView(1, 1),
                                new LocationsLayoutResponse.LayoutUbicacionSlotView(2, 2),
                                new LocationsLayoutResponse.LayoutUbicacionSlotView(3, 3)
                        )
                )
        );
        UpdateLocationsLayoutRequest request = new UpdateLocationsLayoutRequest(
                1L,
                new UpdateLocationsLayoutRequest.ConfiguracionLayoutRequest(
                        ModoCaptura.MULTIPLE,
                        3,
                        List.of(
                                new UpdateLocationsLayoutRequest.LayoutUbicacionSlotRequest(1, 1),
                                new UpdateLocationsLayoutRequest.LayoutUbicacionSlotRequest(2, 2),
                                new UpdateLocationsLayoutRequest.LayoutUbicacionSlotRequest(3, 3)
                        )
                )
        );

        when(updateLocationsLayoutUseCase.handle(any(), any(UpdateLocationsLayoutRequest.class))).thenReturn(response);

        mockMvc.perform(put("/v1/quotes/{folio}/locations/layout", "1000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.numeroFolio").value("1000001"))
                .andExpect(jsonPath("$.data.version").value(2))
                .andExpect(jsonPath("$.data.configuracionLayout.modoCaptura").value("MULTIPLE"))
                .andExpect(jsonPath("$.data.configuracionLayout.cantidadUbicaciones").value(3));
    }

    @Test
    void updateLocationsLayout_returns400WhenBodyFailsValidation() throws Exception {
        mockMvc.perform(put("/v1/quotes/{folio}/locations/layout", "1000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validacion invalida"))
                .andExpect(jsonPath("$.detail").value("La solicitud contiene campos invalidos"));
    }

    @Test
    void updateLocationsLayout_returns409WhenVersionConflictOccurs() throws Exception {
        UpdateLocationsLayoutRequest request = new UpdateLocationsLayoutRequest(
                1L,
                new UpdateLocationsLayoutRequest.ConfiguracionLayoutRequest(
                        ModoCaptura.MULTIPLE,
                        3,
                        List.of(
                                new UpdateLocationsLayoutRequest.LayoutUbicacionSlotRequest(1, 1),
                                new UpdateLocationsLayoutRequest.LayoutUbicacionSlotRequest(2, 2),
                                new UpdateLocationsLayoutRequest.LayoutUbicacionSlotRequest(3, 3)
                        )
                )
        );

        when(updateLocationsLayoutUseCase.handle(any(), any(UpdateLocationsLayoutRequest.class)))
                .thenThrow(new LocationsLayoutVersionConflictException("1000001", 1L, 2L));

        mockMvc.perform(put("/v1/quotes/{folio}/locations/layout", "1000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflicto de concurrencia"))
                .andExpect(jsonPath("$.detail").value("La cotizacion 1000001 tiene version 1 y no coincide con la version actual 2"));
    }

    @Test
    void updateLocationsLayout_returns422WhenBusinessRulesAreBroken() throws Exception {
        UpdateLocationsLayoutRequest request = new UpdateLocationsLayoutRequest(
                1L,
                new UpdateLocationsLayoutRequest.ConfiguracionLayoutRequest(
                        ModoCaptura.UNICA,
                        2,
                        List.of(
                                new UpdateLocationsLayoutRequest.LayoutUbicacionSlotRequest(1, 1),
                                new UpdateLocationsLayoutRequest.LayoutUbicacionSlotRequest(2, 2)
                        )
                )
        );

        when(updateLocationsLayoutUseCase.handle(any(), any(UpdateLocationsLayoutRequest.class)))
                .thenThrow(new LocationsLayoutValidationException("El modo de captura UNICA requiere exactamente una ubicacion"));

        mockMvc.perform(put("/v1/quotes/{folio}/locations/layout", "1000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Layout de ubicaciones invalido"))
                .andExpect(jsonPath("$.detail").value("El modo de captura UNICA requiere exactamente una ubicacion"));
    }
}