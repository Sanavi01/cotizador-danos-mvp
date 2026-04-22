ALTER TABLE cotizaciones_danos
    ADD COLUMN IF NOT EXISTS estado_calculo VARCHAR(20),
    ADD COLUMN IF NOT EXISTS calculated_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS calculation_parameter_version VARCHAR(32);

CREATE TABLE IF NOT EXISTS cotizacion_primas_por_ubicacion (
    id BIGSERIAL PRIMARY KEY,
    cotizacion_id BIGINT NOT NULL,
    indice_ubicacion INTEGER NOT NULL,
    ubicacion_calculable BOOLEAN NOT NULL,
    prima_neta_ubicacion NUMERIC(18, 2) NOT NULL,
    prima_comercial_ubicacion NUMERIC(18, 2) NOT NULL,
    garantias_calculadas TEXT NOT NULL,
    componentes_comerciales TEXT NOT NULL,
    alertas TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_cotizacion_primas_por_ubicacion_cotizacion
        FOREIGN KEY (cotizacion_id)
        REFERENCES cotizaciones_danos (id)
        ON DELETE CASCADE,
    CONSTRAINT uk_cotizacion_primas_por_ubicacion_indice
        UNIQUE (cotizacion_id, indice_ubicacion)
);

CREATE INDEX IF NOT EXISTS idx_cotizacion_primas_por_ubicacion_cotizacion_id
    ON cotizacion_primas_por_ubicacion (cotizacion_id);

CREATE INDEX IF NOT EXISTS idx_cotizacion_primas_por_ubicacion_estado_calculo
    ON cotizaciones_danos (estado_calculo);