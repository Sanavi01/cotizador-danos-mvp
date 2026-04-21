package com.sofka.plataforma_danos_back.folios.entrypoints.controller;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.sofka.plataforma_danos_back.folios.application.GetGeneralInfoUseCase;
import com.sofka.plataforma_danos_back.folios.application.UpdateGeneralInfoUseCase;
import com.sofka.plataforma_danos_back.folios.application.dto.GeneralInfoRequest;
import com.sofka.plataforma_danos_back.folios.application.dto.GeneralInfoResponse;
import com.sofka.plataforma_danos_back.folios.application.exception.GeneralInfoCatalogValidationException;
import com.sofka.plataforma_danos_back.folios.application.exception.GeneralInfoVersionConflictException;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;

@ExtendWith(MockitoExtension.class)
class GeneralInfoControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private GetGeneralInfoUseCase getGeneralInfoUseCase;

    @Mock
    private UpdateGeneralInfoUseCase updateGeneralInfoUseCase;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = standaloneSetup(new GeneralInfoController(getGeneralInfoUseCase, updateGeneralInfoUseCase))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void getGeneralInfo_returns200WithDataEnvelope() throws Exception {
        Instant now = Instant.parse("2026-04-21T00:00:00Z");
        GeneralInfoResponse response = new GeneralInfoResponse(
                "1000001",
                3L,
                now,
                new GeneralInfoResponse.DatosAsegurado(
                        "NIT",
                        "900123456",
                        "ACME SAS",
                        "contacto@acme.com",
                        "6015550101"
                ),
                new GeneralInfoResponse.DatosConduccion("AG-102", "RISK-A", "GIRO-001")
        );

        when(getGeneralInfoUseCase.handle("1000001")).thenReturn(response);

        mockMvc.perform(get("/v1/quotes/{folio}/general-info", "1000001"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.numeroFolio").value("1000001"))
                .andExpect(jsonPath("$.data.version").value(3))
                .andExpect(jsonPath("$.data.fechaUltimaActualizacion").value("2026-04-21T00:00:00Z"))
                .andExpect(jsonPath("$.data.datosAsegurado.tipoDocumento").value("NIT"))
                .andExpect(jsonPath("$.data.datosConduccion.codigoAgente").value("AG-102"));
    }

    @Test
    void getGeneralInfo_returns404WhenFolioDoesNotExist() throws Exception {
        when(getGeneralInfoUseCase.handle("9999999")).thenThrow(new QuoteNotFoundException("9999999"));

        mockMvc.perform(get("/v1/quotes/{folio}/general-info", "9999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Cotizacion no encontrada"))
                .andExpect(jsonPath("$.detail").value("No existe una cotizacion con numeroFolio 9999999"));
    }

    @Test
    void updateGeneralInfo_returns200WithDataEnvelope() throws Exception {
        Instant now = Instant.parse("2026-04-21T00:00:00Z");
        GeneralInfoResponse response = new GeneralInfoResponse(
                "1000001",
                2L,
                now,
                new GeneralInfoResponse.DatosAsegurado(
                        "NIT",
                        "900123456",
                        "ACME SAS",
                        "contacto@acme.com",
                        "6015550101"
                ),
                new GeneralInfoResponse.DatosConduccion("AG-102", "RISK-A", "GIRO-001")
        );
        GeneralInfoRequest request = new GeneralInfoRequest(
                1L,
                new GeneralInfoRequest.DatosAsegurado(
                        "NIT",
                        "900123456",
                        "ACME SAS",
                        "contacto@acme.com",
                        "6015550101"
                ),
                new GeneralInfoRequest.DatosConduccion("AG-102", "RISK-A", "GIRO-001")
        );

        when(updateGeneralInfoUseCase.handle(eq("1000001"), any(GeneralInfoRequest.class))).thenReturn(response);

        mockMvc.perform(put("/v1/quotes/{folio}/general-info", "1000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.numeroFolio").value("1000001"))
                .andExpect(jsonPath("$.data.version").value(2))
                .andExpect(jsonPath("$.data.fechaUltimaActualizacion").value("2026-04-21T00:00:00Z"))
                .andExpect(jsonPath("$.data.datosAsegurado.nombreORazonSocial").value("ACME SAS"))
                .andExpect(jsonPath("$.data.datosConduccion.tipoNegocio").value("GIRO-001"));
    }

    @Test
    void updateGeneralInfo_returns400WhenPayloadFailsValidation() throws Exception {
        String invalidBody = "{}";

        mockMvc.perform(put("/v1/quotes/{folio}/general-info", "1000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validacion invalida"))
                .andExpect(jsonPath("$.detail").value("La solicitud contiene campos invalidos"));
    }

    @Test
    void updateGeneralInfo_returns409WhenVersionConflictOccurs() throws Exception {
        GeneralInfoRequest request = new GeneralInfoRequest(
                1L,
                new GeneralInfoRequest.DatosAsegurado(
                        "NIT",
                        "900123456",
                        "ACME SAS",
                        "contacto@acme.com",
                        "6015550101"
                ),
                new GeneralInfoRequest.DatosConduccion("AG-102", "RISK-A", "GIRO-001")
        );

        when(updateGeneralInfoUseCase.handle(eq("1000001"), any(GeneralInfoRequest.class)))
                .thenThrow(new GeneralInfoVersionConflictException("1000001", 1L, 2L));

        mockMvc.perform(put("/v1/quotes/{folio}/general-info", "1000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflicto de concurrencia"))
                .andExpect(jsonPath("$.detail").value("La cotizacion 1000001 tiene version 1 y no coincide con la version actual 2"));
    }

    @Test
    void updateGeneralInfo_returns422WhenCatalogValidationFails() throws Exception {
        GeneralInfoRequest request = new GeneralInfoRequest(
                1L,
                new GeneralInfoRequest.DatosAsegurado(
                        "NIT",
                        "900123456",
                        "ACME SAS",
                        "contacto@acme.com",
                        "6015550101"
                ),
                new GeneralInfoRequest.DatosConduccion("AG-999", "RISK-A", "GIRO-001")
        );

        when(updateGeneralInfoUseCase.handle(eq("1000001"), any(GeneralInfoRequest.class)))
                .thenThrow(new GeneralInfoCatalogValidationException(java.util.List.of("codigoAgente")));

        mockMvc.perform(put("/v1/quotes/{folio}/general-info", "1000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Referencia de catalogo invalida"))
                .andExpect(jsonPath("$.detail").value("Las referencias de catalogo no son validas: codigoAgente"));
    }
}