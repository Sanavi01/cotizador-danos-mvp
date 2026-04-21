CREATE TABLE IF NOT EXISTS cotizacion_opciones_cobertura (
    id BIGSERIAL PRIMARY KEY,
    cotizacion_id BIGINT NOT NULL UNIQUE,
    version BIGINT NOT NULL DEFAULT 0,
    garantias_seleccionadas TEXT NOT NULL,
    observaciones VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_cotizacion_opciones_cobertura_cotizacion
        FOREIGN KEY (cotizacion_id)
        REFERENCES cotizaciones_danos (id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_cotizacion_opciones_cobertura_cotizacion_id
    ON cotizacion_opciones_cobertura (cotizacion_id);