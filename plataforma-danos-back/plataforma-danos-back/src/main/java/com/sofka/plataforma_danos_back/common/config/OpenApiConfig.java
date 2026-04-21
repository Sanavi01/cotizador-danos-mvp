package com.sofka.plataforma_danos_back.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Plataforma Danos Back API",
                version = "1.0",
                description = "Contrato OpenAPI para folios e idempotencia del cotizador de danos"
        ),
        servers = @Server(url = "http://localhost:8080")
)
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi foliosApi() {
        return GroupedOpenApi.builder()
                .group("folios")
                .packagesToScan("com.sofka.plataforma_danos_back.folios")
                .build();
    }
}