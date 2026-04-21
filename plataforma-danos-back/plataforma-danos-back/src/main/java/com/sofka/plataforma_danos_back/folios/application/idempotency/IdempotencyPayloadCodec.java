package com.sofka.plataforma_danos_back.folios.application.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.plataforma_danos_back.folios.application.dto.CreateFolioRequest;
import com.sofka.plataforma_danos_back.folios.application.dto.CreateFolioResponse;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class IdempotencyPayloadCodec {

    private final ObjectMapper objectMapper;

    public IdempotencyPayloadCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String hashRequest(CreateFolioRequest request) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(serialize(request).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("No se pudo calcular el hash de la solicitud", exception);
        }
    }

    public String serializeCreateFolioResponse(CreateFolioResponse response) {
        return serialize(response);
    }

    public CreateFolioResponse deserializeCreateFolioResponse(String payload) {
        try {
            return objectMapper.readValue(payload, CreateFolioResponse.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("No se pudo reconstruir la respuesta idempotente", exception);
        }
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("No se pudo serializar el objeto", exception);
        }
    }
}