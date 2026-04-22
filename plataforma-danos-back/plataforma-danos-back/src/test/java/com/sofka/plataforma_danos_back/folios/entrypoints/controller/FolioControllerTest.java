package com.sofka.plataforma_danos_back.folios.entrypoints.controller;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sofka.plataforma_danos_back.common.error.ApiExceptionHandler;
import com.sofka.plataforma_danos_back.folios.application.CalculateQuoteUseCase;
import com.sofka.plataforma_danos_back.folios.application.CreateFolioUseCase;
import com.sofka.plataforma_danos_back.folios.application.GetQuoteStateUseCase;
import com.sofka.plataforma_danos_back.folios.application.dto.CalculateQuoteRequest;
import com.sofka.plataforma_danos_back.folios.application.dto.CalculateQuoteResponse;
import com.sofka.plataforma_danos_back.folios.application.dto.CreateFolioRequest;
import com.sofka.plataforma_danos_back.folios.application.dto.CreateFolioResponse;
import com.sofka.plataforma_danos_back.folios.application.dto.FolioCreationResult;
import com.sofka.plataforma_danos_back.folios.application.dto.QuoteStateResponse;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteCalculationRejectedException;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteCalculationVersionConflictException;
import com.sofka.plataforma_danos_back.folios.application.exception.IdempotencyConflictException;
import com.sofka.plataforma_danos_back.folios.application.exception.MissingIdempotencyKeyException;
import com.sofka.plataforma_danos_back.folios.application.exception.QuoteNotFoundException;
import com.sofka.plataforma_danos_back.folios.domain.EstadoCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.EstadoCalculo;
import com.sofka.plataforma_danos_back.folios.domain.AlertaBloqueante;
import com.sofka.plataforma_danos_back.folios.domain.ComponenteComercialCalculado;
import com.sofka.plataforma_danos_back.folios.domain.ComponenteTecnicoCalculado;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.GarantiaCalculada;
import com.sofka.plataforma_danos_back.folios.domain.PrimaPorUbicacion;

@ExtendWith(MockitoExtension.class)
class FolioControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private CreateFolioUseCase createFolioUseCase;

    @Mock
    private GetQuoteStateUseCase getQuoteStateUseCase;

        @Mock
        private CalculateQuoteUseCase calculateQuoteUseCase;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
                mockMvc = standaloneSetup(new FolioController(createFolioUseCase, getQuoteStateUseCase, calculateQuoteUseCase))
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
                EstadoCotizacion.EN_CAPTURA,
                0L,
                now,
                new QuoteStateResponse.ProgresoCotizacion(
                        QuoteStateResponse.EstadoSeccion.COMPLETED,
                        QuoteStateResponse.EstadoSeccion.COMPLETED,
                        QuoteStateResponse.EstadoSeccion.INCOMPLETE,
                        QuoteStateResponse.EstadoSeccion.COMPLETED
                ),
                new QuoteStateResponse.ResumenUbicaciones(3, 2, 1, 0, 1, 1),
                true,
                List.of(new QuoteStateResponse.AlertaVigente(
                        "UBICACION_SIN_ZIP",
                        "La ubicacion no tiene codigo postal valido.",
                        "Warning"
                )),
                false,
                new QuoteStateResponse.ResultadoFinancieroResumen(
                        new java.math.BigDecimal("60000.00"),
                        new java.math.BigDecimal("70200.00"),
                        1,
                        2,
                        QuoteStateResponse.EstadoCalculoResumen.PARCIAL,
                        now,
                        "1.0.0"
                )
        );

        when(getQuoteStateUseCase.handle("1000001")).thenReturn(response);

        mockMvc.perform(get("/v1/quotes/{folio}/state", "1000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.numeroFolio").value("1000001"))
                .andExpect(jsonPath("$.data.estadoCotizacion").value("EN_CAPTURA"))
                .andExpect(jsonPath("$.data.version").value(0))
                .andExpect(jsonPath("$.data.fechaUltimaActualizacion").value("2026-04-20T00:00:00Z"))
                .andExpect(jsonPath("$.data.progreso.datosGenerales").value("COMPLETED"))
                .andExpect(jsonPath("$.data.progreso.layoutUbicaciones").value("COMPLETED"))
                .andExpect(jsonPath("$.data.progreso.ubicaciones").value("INCOMPLETE"))
                .andExpect(jsonPath("$.data.progreso.opcionesCobertura").value("COMPLETED"))
                .andExpect(jsonPath("$.data.resumenUbicaciones.totalEsperado").value(3))
                .andExpect(jsonPath("$.data.resumenUbicaciones.totalActual").value(2))
                .andExpect(jsonPath("$.data.resumenUbicaciones.calculables").value(1))
                .andExpect(jsonPath("$.data.tieneAlertas").value(true))
                .andExpect(jsonPath("$.data.alertasVigentes[0].codigo").value("UBICACION_SIN_ZIP"))
                .andExpect(jsonPath("$.data.readyToCalculate").value(false))
                .andExpect(jsonPath("$.data.resultadoFinanciero.estadoCalculo").value("PARCIAL"))
                .andExpect(jsonPath("$.data.resultadoFinanciero.calculationParameterVersion").value("1.0.0"));
    }

    @Test
    void getQuoteState_returns404WhenFolioDoesNotExist() throws Exception {
        when(getQuoteStateUseCase.handle("9999999")).thenThrow(new QuoteNotFoundException("9999999"));

        mockMvc.perform(get("/v1/quotes/{folio}/state", "9999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Cotizacion no encontrada"))
                .andExpect(jsonPath("$.detail").value("No existe una cotizacion con numeroFolio 9999999"));
    }

    @Test
    void calculateQuote_returns200WithDataEnvelope() throws Exception {
        Instant now = Instant.parse("2026-04-21T00:00:00Z");
        CalculateQuoteResponse response = CalculateQuoteResponse.from(
                new Cotizacion(
                        13L,
                        "1000001",
                        EstadoCotizacion.CALCULADA,
                        5L,
                        now,
                        new java.math.BigDecimal("24120.00"),
                        new java.math.BigDecimal("28220.40"),
                        EstadoCalculo.CALCULADO,
                        now,
                        "1.0.0"
                ),
                List.of(new PrimaPorUbicacion(
                        1,
                        true,
                        new java.math.BigDecimal("24120.00"),
                        new java.math.BigDecimal("28220.40"),
                        List.of(new GarantiaCalculada(
                                "GAR-INC-ED",
                                new java.math.BigDecimal("24120.00"),
                                List.of(
                                        new ComponenteTecnicoCalculado("base", "tariffs", "GIRO-001|ZTEV-1|GAR-INC-ED", new java.math.BigDecimal("0.015"), new java.math.BigDecimal("1.20"), new java.math.BigDecimal("18000.00")),
                                        new ComponenteTecnicoCalculado("factor_zona", "catTariffs", "ZTEV-1|GAR-INC-ED", new java.math.BigDecimal("0.006"), new java.math.BigDecimal("1.02"), new java.math.BigDecimal("6120.00"))
                                )
                        )),
                        List.of(
                                new ComponenteComercialCalculado("RECARGO_ADMINISTRACION", new java.math.BigDecimal("0.12"), new java.math.BigDecimal("24120.00"), new java.math.BigDecimal("2894.40")),
                                new ComponenteComercialCalculado("MARGEN_COMERCIAL", new java.math.BigDecimal("0.05"), new java.math.BigDecimal("24120.00"), new java.math.BigDecimal("1206.00"))
                        ),
                        List.of()
                )),
                List.of(new AlertaBloqueante("UBICACION_SIN_ZIP", "La ubicacion no tiene codigo postal valido.", "Warning"))
        );

        when(calculateQuoteUseCase.handle("1000001", new CalculateQuoteRequest(4L))).thenReturn(response);

        mockMvc.perform(post("/v1/quotes/{folio}/calculate", "1000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CalculateQuoteRequest(4L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.numeroFolio").value("1000001"))
                .andExpect(jsonPath("$.data.estadoCotizacion").value("CALCULADA"))
                .andExpect(jsonPath("$.data.estadoCalculo").value("CALCULADO"))
                .andExpect(jsonPath("$.data.primaNeta").value(24120.00))
                .andExpect(jsonPath("$.data.primaComercial").value(28220.40))
                .andExpect(jsonPath("$.data.primasPorUbicacion[0].garantiasCalculadas[0].componentes[0].lookupKey").value("GIRO-001|ZTEV-1|GAR-INC-ED"))
                .andExpect(jsonPath("$.data.alertasVigentes[0].codigo").value("UBICACION_SIN_ZIP"));
    }

    @Test
    void calculateQuote_returns409WhenVersionDoesNotMatch() throws Exception {
        when(calculateQuoteUseCase.handle("1000001", new CalculateQuoteRequest(99L)))
                .thenThrow(new QuoteCalculationVersionConflictException("1000001", 99L, 4L));

        mockMvc.perform(post("/v1/quotes/{folio}/calculate", "1000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CalculateQuoteRequest(99L))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflicto de concurrencia"))
                .andExpect(jsonPath("$.detail").value("La version de calculo para el folio 1000001 no coincide. Solicitada: 99, vigente: 4"));
    }

    @Test
    void calculateQuote_returns422WhenCalculationIsRejected() throws Exception {
        when(calculateQuoteUseCase.handle("1000001", new CalculateQuoteRequest(4L)))
                .thenThrow(new QuoteCalculationRejectedException("La cotizacion no tiene ubicaciones calculables"));

        mockMvc.perform(post("/v1/quotes/{folio}/calculate", "1000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CalculateQuoteRequest(4L))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Calculo no disponible"))
                .andExpect(jsonPath("$.detail").value("La cotizacion no tiene ubicaciones calculables"));
    }
}