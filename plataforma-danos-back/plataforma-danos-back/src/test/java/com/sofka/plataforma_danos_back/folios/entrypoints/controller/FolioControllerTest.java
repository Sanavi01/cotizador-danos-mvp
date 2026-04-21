package com.sofka.plataforma_danos_back.folios.entrypoints.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sofka.plataforma_danos_back.common.error.ApiExceptionHandler;
import com.sofka.plataforma_danos_back.folios.application.CreateFolioUseCase;
import com.sofka.plataforma_danos_back.folios.application.GetQuoteStateUseCase;
import com.sofka.plataforma_danos_back.folios.application.dto.CreateFolioRequest;
import com.sofka.plataforma_danos_back.folios.application.dto.CreateFolioResponse;
import com.sofka.plataforma_danos_back.folios.application.dto.FolioCreationResult;
import com.sofka.plataforma_danos_back.folios.application.dto.QuoteStateResponse;
import com.sofka.plataforma_danos_back.folios.application.exception.IdempotencyConflictException;
import com.sofka.plataforma_danos_back.folios.application.exception.MissingIdempotencyKeyException;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.EstadoCotizacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class FolioControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private CreateFolioUseCase createFolioUseCase;

    @Mock
    private GetQuoteStateUseCase getQuoteStateUseCase;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = standaloneSetup(new FolioController(createFolioUseCase, getQuoteStateUseCase))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void createFolio_returns201WithDataEnvelope() throws Exception {
        Instant now = Instant.parse("2026-04-20T00:00:00Z");
        CreateFolioResponse response = new CreateFolioResponse("1000001", EstadoCotizacion.BORRADOR, 0L, now);

        when(createFolioUseCase.handle(any(CreateFolioRequest.class), anyString()))
                .thenReturn(new FolioCreationResult(response, true));

        mockMvc.perform(post("/v1/folios")
                        .header("Idempotency-Key", "idem-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateFolioRequest.defaultRequest())))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.numeroFolio").value("1000001"))
                .andExpect(jsonPath("$.data.estadoCotizacion").value("BORRADOR"))
                .andExpect(jsonPath("$.data.version").value(0))
                .andExpect(jsonPath("$.data.fechaUltimaActualizacion").value("2026-04-20T00:00:00Z"));
    }

    @Test
    void createFolio_returns200WhenRequestIsReplayed() throws Exception {
        Instant now = Instant.parse("2026-04-20T00:00:00Z");
        CreateFolioResponse response = new CreateFolioResponse("1000001", EstadoCotizacion.BORRADOR, 0L, now);

        when(createFolioUseCase.handle(any(CreateFolioRequest.class), anyString()))
                .thenReturn(new FolioCreationResult(response, false));

        mockMvc.perform(post("/v1/folios")
                        .header("Idempotency-Key", "idem-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateFolioRequest.defaultRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.numeroFolio").value("1000001"))
                .andExpect(jsonPath("$.data.estadoCotizacion").value("BORRADOR"));
    }

    @Test
    void createFolio_returns400WhenHeaderIsMissing() throws Exception {
        when(createFolioUseCase.handle(any(CreateFolioRequest.class), isNull()))
                .thenThrow(new MissingIdempotencyKeyException());

        mockMvc.perform(post("/v1/folios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateFolioRequest.defaultRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Idempotency Key requerida"))
                .andExpect(jsonPath("$.detail").value("Idempotency-Key es obligatorio para crear folios"));
    }

    @Test
    void createFolio_returns409WhenIdempotencyConflictOccurs() throws Exception {
        when(createFolioUseCase.handle(any(CreateFolioRequest.class), anyString()))
                .thenThrow(new IdempotencyConflictException());

        mockMvc.perform(post("/v1/folios")
                        .header("Idempotency-Key", "idem-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateFolioRequest.defaultRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflicto de idempotencia"))
                .andExpect(jsonPath("$.detail").value("La llave de idempotencia ya fue utilizada con una solicitud diferente"));
    }

    @Test
    void createFolio_returns400WhenBodyFailsValidation() throws Exception {
        String longOrigin = "x".repeat(65);
        String invalidBody = "{\"origin\":\"" + longOrigin + "\"}";

        mockMvc.perform(post("/v1/folios")
                        .header("Idempotency-Key", "idem-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validacion invalida"))
                .andExpect(jsonPath("$.detail").value("La solicitud contiene campos invalidos"));
    }

    @Test
    void getQuoteState_returns200WithDataEnvelope() throws Exception {
        Instant now = Instant.parse("2026-04-20T00:00:00Z");
        QuoteStateResponse response = new QuoteStateResponse(
                "1000001",
                EstadoCotizacion.BORRADOR,
                false,
                List.of(),
                0,
                0,
                0L,
                now
        );

        when(getQuoteStateUseCase.handle("1000001")).thenReturn(response);

        mockMvc.perform(get("/v1/quotes/{folio}/state", "1000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.numeroFolio").value("1000001"))
                .andExpect(jsonPath("$.data.estadoCotizacion").value("BORRADOR"))
                .andExpect(jsonPath("$.data.tieneAlertas").value(false))
                .andExpect(jsonPath("$.data.ubicacionesCalculables").value(0))
                .andExpect(jsonPath("$.data.ubicacionesIncompletas").value(0))
                .andExpect(jsonPath("$.data.version").value(0))
                .andExpect(jsonPath("$.data.fechaUltimaActualizacion").value("2026-04-20T00:00:00Z"));
    }

    @Test
    void getQuoteState_returns404WhenFolioDoesNotExist() throws Exception {
        when(getQuoteStateUseCase.handle("9999999")).thenThrow(new QuoteNotFoundException("9999999"));

        mockMvc.perform(get("/v1/quotes/{folio}/state", "9999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Cotizacion no encontrada"))
                .andExpect(jsonPath("$.detail").value("No existe una cotizacion con numeroFolio 9999999"));
    }
}