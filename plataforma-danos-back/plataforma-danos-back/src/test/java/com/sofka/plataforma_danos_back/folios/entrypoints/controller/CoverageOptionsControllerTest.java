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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sofka.plataforma_danos_back.common.error.ApiExceptionHandler;
import com.sofka.plataforma_danos_back.folios.application.GetCoverageOptionsUseCase;
import com.sofka.plataforma_danos_back.folios.application.UpdateCoverageOptionsUseCase;
import com.sofka.plataforma_danos_back.folios.application.dto.CoverageOptionsRequest;
import com.sofka.plataforma_danos_back.folios.application.dto.CoverageOptionsResponse;
import com.sofka.plataforma_danos_back.folios.application.exception.CoverageOptionsCatalogValidationException;
import com.sofka.plataforma_danos_back.folios.application.exception.CoverageOptionsVersionConflictException;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.TechnicalPreviewSource;

@ExtendWith(MockitoExtension.class)
class CoverageOptionsControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private GetCoverageOptionsUseCase getCoverageOptionsUseCase;

    @Mock
    private UpdateCoverageOptionsUseCase updateCoverageOptionsUseCase;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = standaloneSetup(new CoverageOptionsController(getCoverageOptionsUseCase, updateCoverageOptionsUseCase))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void getCoverageOptions_returns200WithDataEnvelope() throws Exception {
        Instant now = Instant.parse("2026-04-21T00:00:00Z");
        CoverageOptionsResponse response = new CoverageOptionsResponse(
                "1000001",
                3L,
                now,
                new CoverageOptionsResponse.CoverageOptionsView(
                        List.of(new CoverageOptionsResponse.SelectedGuaranteeView("GAR-INC-ED", List.of())),
                        "Cobertura base para el analisis inicial"
                ),
                List.of(new CoverageOptionsResponse.ProjectionPerLocationView(
                        1,
                        List.of(new CoverageOptionsResponse.DerivedGuaranteeView(
                                "GAR-INC-ED",
                                true,
                                TechnicalPreviewSource.CORE_TARIFF,
                                "GIRO-001|ZTEV-1|GAR-INC-ED",
                                List.of()
                        )),
                        true
                ))
        );

        when(getCoverageOptionsUseCase.handle("1000001")).thenReturn(response);

        mockMvc.perform(get("/v1/quotes/{folio}/coverage-options", "1000001"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.numeroFolio").value("1000001"))
                .andExpect(jsonPath("$.data.version").value(3))
                .andExpect(jsonPath("$.data.fechaUltimaActualizacion").value("2026-04-21T00:00:00Z"))
                .andExpect(jsonPath("$.data.opcionesCobertura.garantiasSeleccionadas[0].garantiaCode").value("GAR-INC-ED"))
                .andExpect(jsonPath("$.data.projectionPerLocation[0].indice").value(1))
                .andExpect(jsonPath("$.data.projectionPerLocation[0].calculablePreview").value(true))
                .andExpect(jsonPath("$.data.projectionPerLocation[0].garantiasDerivadas[0].fuenteTecnicaPreview").value("CORE_TARIFF"));
    }

    @Test
    void getCoverageOptions_returns404WhenFolioDoesNotExist() throws Exception {
        when(getCoverageOptionsUseCase.handle("9999999")).thenThrow(new QuoteNotFoundException("9999999"));

        mockMvc.perform(get("/v1/quotes/{folio}/coverage-options", "9999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Cotizacion no encontrada"))
                .andExpect(jsonPath("$.detail").value("No existe una cotizacion con numeroFolio 9999999"));
    }

    @Test
    void updateCoverageOptions_returns200WithDataEnvelope() throws Exception {
        Instant now = Instant.parse("2026-04-21T00:00:00Z");
        CoverageOptionsRequest request = new CoverageOptionsRequest(
                1L,
                new CoverageOptionsRequest.CoverageOptionsView(
                        List.of(
                                new CoverageOptionsRequest.SelectedGuaranteeRequest("GAR-INC-ED", List.of(" base ")),
                                new CoverageOptionsRequest.SelectedGuaranteeRequest("GAR-ROBO", List.of())
                        ),
                        "Cobertura base para el analisis inicial"
                )
        );
        CoverageOptionsResponse response = new CoverageOptionsResponse(
                "1000001",
                2L,
                now,
                new CoverageOptionsResponse.CoverageOptionsView(
                        List.of(
                                new CoverageOptionsResponse.SelectedGuaranteeView("GAR-INC-ED", List.of("base")),
                                new CoverageOptionsResponse.SelectedGuaranteeView("GAR-ROBO", List.of())
                        ),
                        "Cobertura base para el analisis inicial"
                ),
                List.of(new CoverageOptionsResponse.ProjectionPerLocationView(
                        1,
                        List.of(new CoverageOptionsResponse.DerivedGuaranteeView(
                                "GAR-INC-ED",
                                true,
                                TechnicalPreviewSource.CORE_TARIFF,
                                "GIRO-001|ZTEV-1|GAR-INC-ED",
                                List.of()
                        )),
                        true
                ))
        );

        when(updateCoverageOptionsUseCase.handle(eq("1000001"), any(CoverageOptionsRequest.class))).thenReturn(response);

        mockMvc.perform(put("/v1/quotes/{folio}/coverage-options", "1000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.numeroFolio").value("1000001"))
                .andExpect(jsonPath("$.data.version").value(2))
                .andExpect(jsonPath("$.data.opcionesCobertura.garantiasSeleccionadas[0].garantiaCode").value("GAR-INC-ED"))
                .andExpect(jsonPath("$.data.opcionesCobertura.garantiasSeleccionadas[0].terminos[0]").value("base"))
                .andExpect(jsonPath("$.data.projectionPerLocation[0].calculablePreview").value(true));
    }

    @Test
    void updateCoverageOptions_returns400WhenPayloadFailsValidation() throws Exception {
        mockMvc.perform(put("/v1/quotes/{folio}/coverage-options", "1000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validacion invalida"))
                .andExpect(jsonPath("$.detail").value("La solicitud contiene campos invalidos"));
    }

    @Test
    void updateCoverageOptions_returns409WhenVersionConflictOccurs() throws Exception {
        CoverageOptionsRequest request = new CoverageOptionsRequest(
                1L,
                new CoverageOptionsRequest.CoverageOptionsView(
                        List.of(new CoverageOptionsRequest.SelectedGuaranteeRequest("GAR-INC-ED", List.of())),
                        null
                )
        );

        when(updateCoverageOptionsUseCase.handle(eq("1000001"), any(CoverageOptionsRequest.class)))
                .thenThrow(new CoverageOptionsVersionConflictException("1000001", 1L, 2L));

        mockMvc.perform(put("/v1/quotes/{folio}/coverage-options", "1000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflicto de concurrencia"))
                .andExpect(jsonPath("$.detail").value("La version de opcionesCobertura para el folio 1000001 no coincide. Solicitada: 1, vigente: 2"));
    }

    @Test
    void updateCoverageOptions_returns422WhenCatalogValidationFails() throws Exception {
        CoverageOptionsRequest request = new CoverageOptionsRequest(
                1L,
                new CoverageOptionsRequest.CoverageOptionsView(
                        List.of(new CoverageOptionsRequest.SelectedGuaranteeRequest("GAR-INACTIVA", List.of())),
                        null
                )
        );

        when(updateCoverageOptionsUseCase.handle(eq("1000001"), any(CoverageOptionsRequest.class)))
                .thenThrow(new CoverageOptionsCatalogValidationException(List.of("GAR-INACTIVA")));

        mockMvc.perform(put("/v1/quotes/{folio}/coverage-options", "1000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Referencia de catalogo invalida"))
                .andExpect(jsonPath("$.detail").value("Las garantias no estan activas o no existen en el catalogo aprobado: [GAR-INACTIVA]"));
    }
}