CREATE TABLE IF NOT EXISTS cotizacion_layout_ubicaciones (
    id BIGSERIAL PRIMARY KEY,
    cotizacion_id BIGINT NOT NULL UNIQUE,
    modo_captura VARCHAR(16) NOT NULL,
    cantidad_ubicaciones INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_cotizacion_layout_ubicaciones_cotizacion
        FOREIGN KEY (cotizacion_id)
        REFERENCES cotizaciones_danos (id)
        ON DELETE CASCADE,
    CONSTRAINT ck_cotizacion_layout_ubicaciones_cantidad
        CHECK (cantidad_ubicaciones >= 1),
    CONSTRAINT ck_cotizacion_layout_ubicaciones_modo
        CHECK (
            (modo_captura = 'UNICA' AND cantidad_ubicaciones = 1)
            OR
            (modo_captura = 'MULTIPLE' AND cantidad_ubicaciones > 1)
        )
);

CREATE TABLE IF NOT EXISTS cotizacion_layout_ubicacion_slots (
    cotizacion_layout_id BIGINT NOT NULL,
    indice INTEGER NOT NULL,
    orden_captura INTEGER NOT NULL,
    CONSTRAINT fk_cotizacion_layout_ubicacion_slots_layout
        FOREIGN KEY (cotizacion_layout_id)
        REFERENCES cotizacion_layout_ubicaciones (id)
        ON DELETE CASCADE,
    CONSTRAINT uk_cotizacion_layout_ubicacion_slots_indice
        UNIQUE (cotizacion_layout_id, indice),
    CONSTRAINT uk_cotizacion_layout_ubicacion_slots_orden
        UNIQUE (cotizacion_layout_id, orden_captura),
    CONSTRAINT ck_cotizacion_layout_ubicacion_slots_indice
        CHECK (indice >= 1),
    CONSTRAINT ck_cotizacion_layout_ubicacion_slots_orden
        CHECK (orden_captura >= 1)
);

CREATE INDEX IF NOT EXISTS idx_cotizacion_layout_ubicaciones_cotizacion_id
    ON cotizacion_layout_ubicaciones (cotizacion_id);

CREATE INDEX IF NOT EXISTS idx_cotizacion_layout_ubicacion_slots_layout_id
    ON cotizacion_layout_ubicacion_slots (cotizacion_layout_id);