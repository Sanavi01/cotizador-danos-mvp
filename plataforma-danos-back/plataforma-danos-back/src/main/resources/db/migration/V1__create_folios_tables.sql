CREATE SEQUENCE IF NOT EXISTS public.folio_number_sequence
    START WITH 1000001
    INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS cotizaciones_danos (
    id BIGSERIAL PRIMARY KEY,
    numero_folio VARCHAR(32) NOT NULL UNIQUE,
    estado_cotizacion VARCHAR(50) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    fecha_ultima_actualizacion TIMESTAMP WITH TIME ZONE NOT NULL,
    prima_neta NUMERIC(18, 2),
    prima_comercial NUMERIC(18, 2),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS idempotency_keys (
    id BIGSERIAL PRIMARY KEY,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    request_hash VARCHAR(128) NOT NULL,
    response_payload TEXT NOT NULL,
    cotizacion_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_idempotency_cotizacion
        FOREIGN KEY (cotizacion_id)
        REFERENCES cotizaciones_danos (id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_cotizaciones_estado_cotizacion
    ON cotizaciones_danos (estado_cotizacion);