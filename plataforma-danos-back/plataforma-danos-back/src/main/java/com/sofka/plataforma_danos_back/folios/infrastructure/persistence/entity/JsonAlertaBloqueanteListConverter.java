package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.entity;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.plataforma_danos_back.folios.domain.AlertaBloqueante;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class JsonAlertaBloqueanteListConverter implements AttributeConverter<List<AlertaBloqueante>, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();
    private static final TypeReference<List<AlertaBloqueante>> TYPE_REFERENCE = new TypeReference<>() {
    };

    @Override
    public String convertToDatabaseColumn(List<AlertaBloqueante> attribute) {
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute == null ? List.of() : attribute);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("No se pudo serializar alertasBloqueantes", exception);
        }
    }

    @Override
    public List<AlertaBloqueante> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return List.of();
        }
        try {
            return OBJECT_MAPPER.readValue(dbData, TYPE_REFERENCE);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("No se pudo deserializar alertasBloqueantes", exception);
        }
    }
}