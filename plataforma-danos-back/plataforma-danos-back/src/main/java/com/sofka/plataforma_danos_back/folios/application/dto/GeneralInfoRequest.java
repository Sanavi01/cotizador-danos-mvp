package com.sofka.plataforma_danos_back.folios.application.dto;

import com.sofka.plataforma_danos_back.folios.domain.DatosGeneralesCotizacion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record GeneralInfoRequest(
        @Schema(description = "Version actual del agregado para control optimista", example = "1")
        @NotNull(message = "La version es obligatoria")
        Long version,
        @Schema(description = "Datos de identificacion y contacto del asegurado")
        @Valid
        @NotNull(message = "Los datos del asegurado son obligatorios")
        DatosAsegurado datosAsegurado,
        @Schema(description = "Datos comerciales asociados a la cotizacion")
        @Valid
        @NotNull(message = "Los datos de conduccion son obligatorios")
        DatosConduccion datosConduccion
) {
    public record DatosAsegurado(
            @Schema(description = "Tipo de documento del asegurado", example = "NIT")
            @NotBlank(message = "El tipo de documento es obligatorio")
            @Size(max = 32, message = "El tipo de documento no puede superar 32 caracteres")
            String tipoDocumento,
            @Schema(description = "Numero de documento del asegurado", example = "900123456")
            @NotBlank(message = "El numero de documento es obligatorio")
            @Size(max = 32, message = "El numero de documento no puede superar 32 caracteres")
            String numeroDocumento,
            @Schema(description = "Nombre o razon social del asegurado", example = "ACME SAS")
            @NotBlank(message = "El nombre o razon social es obligatorio")
            @Size(max = 160, message = "El nombre o razon social no puede superar 160 caracteres")
            String nombreORazonSocial,
            @Schema(description = "Correo electronico de contacto", example = "contacto@acme.com")
            @Email(message = "El correo electronico no tiene un formato valido")
            @Size(max = 160, message = "El correo electronico no puede superar 160 caracteres")
            String correoElectronico,
            @Schema(description = "Telefono de contacto", example = "6015550101")
            @Pattern(regexp = "^[0-9+()\\-\\s]{7,20}$", message = "El telefono no tiene un formato valido")
            @Size(max = 20, message = "El telefono no puede superar 20 caracteres")
            String telefono
    ) {
        public DatosGeneralesCotizacion.DatosAsegurado toDomain() {
            return new DatosGeneralesCotizacion.DatosAsegurado(
                    tipoDocumento,
                    numeroDocumento,
                    nombreORazonSocial,
                    correoElectronico,
                    telefono
            );
        }
    }

    public record DatosConduccion(
            @Schema(description = "Codigo de agente comercial", example = "AG-102")
            @NotBlank(message = "El codigo agente es obligatorio")
            @Size(max = 32, message = "El codigo agente no puede superar 32 caracteres")
            String codigoAgente,
            @Schema(description = "Clasificacion de riesgo", example = "RISK-A")
            @NotBlank(message = "La clasificacion de riesgo es obligatoria")
            @Size(max = 32, message = "La clasificacion de riesgo no puede superar 32 caracteres")
            String clasificacionRiesgo,
            @Schema(description = "Tipo de negocio", example = "GIRO-001")
            @NotBlank(message = "El tipo de negocio es obligatorio")
            @Size(max = 32, message = "El tipo de negocio no puede superar 32 caracteres")
            String tipoNegocio
    ) {
        public DatosGeneralesCotizacion.DatosConduccion toDomain() {
            return new DatosGeneralesCotizacion.DatosConduccion(
                    codigoAgente,
                    clasificacionRiesgo,
                    tipoNegocio
            );
        }
    }
}