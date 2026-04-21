CREATE TABLE IF NOT EXISTS cotizacion_datos_generales (
    id BIGSERIAL PRIMARY KEY,
    cotizacion_id BIGINT NOT NULL UNIQUE,
    tipo_documento VARCHAR(32) NOT NULL,
    numero_documento VARCHAR(32) NOT NULL,
    nombre_o_razon_social VARCHAR(160) NOT NULL,
    correo_electronico VARCHAR(160),
    telefono VARCHAR(20),
    codigo_agente VARCHAR(32) NOT NULL,
    clasificacion_riesgo VARCHAR(32) NOT NULL,
    tipo_negocio VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_cotizacion_datos_generales_cotizacion
        FOREIGN KEY (cotizacion_id)
        REFERENCES cotizaciones_danos (id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_cotizacion_datos_generales_codigo_agente
    ON cotizacion_datos_generales (codigo_agente);

CREATE INDEX IF NOT EXISTS idx_cotizacion_datos_generales_clasificacion_riesgo
    ON cotizacion_datos_generales (clasificacion_riesgo);

CREATE INDEX IF NOT EXISTS idx_cotizacion_datos_generales_tipo_negocio
    ON cotizacion_datos_generales (tipo_negocio);