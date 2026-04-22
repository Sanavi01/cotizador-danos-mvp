package com.sofka.plataforma_danos_back.folios.entrypoints.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.sofka.plataforma_danos_back.folios.infrastructure.reference.ReferenceCalculationCatalog;

class CalculationParametersControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new CalculationParametersController(new ReferenceCalculationCatalog())).build();
    }

    @Test
    void getActiveCalculationParameters_returns200WithDataEnvelope() throws Exception {
        mockMvc.perform(get("/v1/calculation-parameters/active"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.codigo").value("CALC-2026-CORE"))
                .andExpect(jsonPath("$.data.activo").value(true))
                .andExpect(jsonPath("$.data.version").value("1.0.0"))
                .andExpect(jsonPath("$.data.recargoAdministracion").value(0.12))
                .andExpect(jsonPath("$.data.margenComercial").value(0.05))
                .andExpect(jsonPath("$.data.moneda").value("COP"))
                .andExpect(jsonPath("$.data.roundingMode").value("HALF_UP"));
    }
}