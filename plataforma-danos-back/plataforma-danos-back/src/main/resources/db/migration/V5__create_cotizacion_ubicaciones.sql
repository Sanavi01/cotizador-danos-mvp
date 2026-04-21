CREATE TABLE IF NOT EXISTS cotizacion_ubicaciones (
    id BIGSERIAL PRIMARY KEY,
    cotizacion_id BIGINT NOT NULL,
    indice INTEGER NOT NULL,
    nombre_ubicacion VARCHAR(160),
    direccion VARCHAR(255),
    codigo_postal VARCHAR(16),
    estado VARCHAR(100),
    municipio VARCHAR(100),
    colonia VARCHAR(100),
    ciudad VARCHAR(100),
    tipo_constructivo VARCHAR(64),
    nivel INTEGER,
    anio_construccion INTEGER,
    giro_codigo VARCHAR(32),
    giro_nombre VARCHAR(160),
    giro_clave_incendio VARCHAR(32),
    zona_catastrofica_zona_tev VARCHAR(32),
    zona_catastrofica_zona_fhm VARCHAR(32),
    estado_validacion VARCHAR(20) NOT NULL,
    alertas_bloqueantes TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_cotizacion_ubicaciones_cotizacion
        FOREIGN KEY (cotizacion_id)
        REFERENCES cotizaciones_danos (id)
        ON DELETE CASCADE,
    CONSTRAINT uk_cotizacion_ubicaciones_cotizacion_indice
        UNIQUE (cotizacion_id, indice),
    CONSTRAINT ck_cotizacion_ubicaciones_indice
        CHECK (indice >= 1),
    CONSTRAINT ck_cotizacion_ubicaciones_nivel
        CHECK (nivel IS NULL OR nivel >= 1),
    CONSTRAINT ck_cotizacion_ubicaciones_anio_construccion
        CHECK (anio_construccion IS NULL OR (anio_construccion BETWEEN 1900 AND 2100))
);

CREATE INDEX IF NOT EXISTS idx_cotizacion_ubicaciones_cotizacion_id
    ON cotizacion_ubicaciones (cotizacion_id);

CREATE INDEX IF NOT EXISTS idx_cotizacion_ubicaciones_estado_validacion
    ON cotizacion_ubicaciones (estado_validacion);