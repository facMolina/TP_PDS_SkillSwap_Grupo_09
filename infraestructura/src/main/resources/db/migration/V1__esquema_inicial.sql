CREATE TABLE nodo_catalogo (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tipo VARCHAR(10) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    padre_id BIGINT NULL,
    PRIMARY KEY (id),
    CONSTRAINT ck_nodo_catalogo_tipo CHECK (tipo IN ('CATEGORIA', 'HABILIDAD')),
    CONSTRAINT fk_nodo_catalogo_padre FOREIGN KEY (padre_id) REFERENCES nodo_catalogo (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE perfil (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id VARCHAR(100) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    nombre_visible VARCHAR(100) NOT NULL,
    biografia VARCHAR(1000) NULL,
    zona_horaria VARCHAR(60) NOT NULL,
    email VARCHAR(150) NULL,
    canal_preferido VARCHAR(10) NOT NULL,
    suma_puntajes BIGINT NOT NULL DEFAULT 0,
    cantidad_calificaciones INT NOT NULL DEFAULT 0,
    estado VARCHAR(10) NOT NULL,
    fecha_baja DATETIME(6) NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uq_perfil_usuario UNIQUE (usuario_id),
    CONSTRAINT ck_perfil_tipo CHECK (tipo IN ('PERSONA', 'ORGANIZACION')),
    CONSTRAINT ck_perfil_canal CHECK (canal_preferido IN ('EMAIL', 'PUSH', 'INTERNA')),
    CONSTRAINT ck_perfil_estado CHECK (estado IN ('ACTIVO', 'BAJA'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE perfil_habilidad (
    perfil_id BIGINT NOT NULL,
    habilidad_id BIGINT NOT NULL,
    PRIMARY KEY (perfil_id, habilidad_id),
    CONSTRAINT fk_perfil_habilidad_perfil FOREIGN KEY (perfil_id) REFERENCES perfil (id),
    CONSTRAINT fk_perfil_habilidad_nodo FOREIGN KEY (habilidad_id) REFERENCES nodo_catalogo (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE perfil_interes (
    perfil_id BIGINT NOT NULL,
    habilidad_id BIGINT NOT NULL,
    PRIMARY KEY (perfil_id, habilidad_id),
    CONSTRAINT fk_perfil_interes_perfil FOREIGN KEY (perfil_id) REFERENCES perfil (id),
    CONSTRAINT fk_perfil_interes_nodo FOREIGN KEY (habilidad_id) REFERENCES nodo_catalogo (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE perfil_disponibilidad (
    perfil_id BIGINT NOT NULL,
    orden INT NOT NULL,
    inicio DATETIME(6) NOT NULL,
    fin DATETIME(6) NOT NULL,
    PRIMARY KEY (perfil_id, orden),
    CONSTRAINT fk_perfil_disponibilidad_perfil FOREIGN KEY (perfil_id) REFERENCES perfil (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE billetera (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id VARCHAR(100) NOT NULL,
    saldo BIGINT NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uq_billetera_usuario UNIQUE (usuario_id),
    CONSTRAINT ck_billetera_saldo CHECK (saldo >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE movimiento_credito (
    id BIGINT NOT NULL AUTO_INCREMENT,
    billetera_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    monto BIGINT NOT NULL,
    saldo_resultante BIGINT NOT NULL,
    referencia VARCHAR(200) NULL,
    fecha DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT ck_movimiento_tipo CHECK (tipo IN ('SALDO_INICIAL', 'PAGO', 'COBRO', 'REINTEGRO')),
    CONSTRAINT fk_movimiento_billetera FOREIGN KEY (billetera_id) REFERENCES billetera (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE publicacion (
    id BIGINT NOT NULL AUTO_INCREMENT,
    autor_id VARCHAR(100) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    titulo VARCHAR(150) NULL,
    descripcion VARCHAR(2000) NULL,
    categoria_id BIGINT NULL,
    modalidad VARCHAR(20) NULL,
    duracion_minutos INT NOT NULL DEFAULT 0,
    cupo INT NOT NULL DEFAULT 0,
    cupos_tomados INT NOT NULL DEFAULT 0,
    precio_base BIGINT NULL,
    politica_cancelacion VARCHAR(10) NOT NULL,
    estado VARCHAR(10) NOT NULL,
    fecha_creacion DATETIME(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT ck_publicacion_tipo CHECK (tipo IN ('MENTORIA', 'SERVICIO', 'DESAFIO')),
    CONSTRAINT ck_publicacion_estado CHECK (estado IN ('BORRADOR', 'PUBLICADA', 'PAUSADA', 'CERRADA', 'CANCELADA')),
    CONSTRAINT ck_publicacion_cupos CHECK (cupos_tomados >= 0),
    CONSTRAINT fk_publicacion_categoria FOREIGN KEY (categoria_id) REFERENCES nodo_catalogo (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX ix_publicacion_autor ON publicacion (autor_id, estado);
CREATE INDEX ix_publicacion_estado ON publicacion (estado, fecha_creacion);

CREATE TABLE publicacion_etiqueta (
    publicacion_id BIGINT NOT NULL,
    habilidad_id BIGINT NOT NULL,
    PRIMARY KEY (publicacion_id, habilidad_id),
    CONSTRAINT fk_etiqueta_publicacion FOREIGN KEY (publicacion_id) REFERENCES publicacion (id),
    CONSTRAINT fk_etiqueta_nodo FOREIGN KEY (habilidad_id) REFERENCES nodo_catalogo (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE franja_disponible (
    publicacion_id BIGINT NOT NULL,
    orden INT NOT NULL,
    inicio DATETIME(6) NOT NULL,
    fin DATETIME(6) NOT NULL,
    ocupada BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (publicacion_id, orden),
    CONSTRAINT uq_franja_publicacion UNIQUE (publicacion_id, inicio, fin),
    CONSTRAINT fk_franja_publicacion FOREIGN KEY (publicacion_id) REFERENCES publicacion (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE promocion (
    publicacion_id BIGINT NOT NULL,
    orden INT NOT NULL,
    tipo VARCHAR(10) NOT NULL,
    porcentaje INT NULL,
    monto BIGINT NULL,
    vigente_desde DATETIME(6) NOT NULL,
    vigente_hasta DATETIME(6) NOT NULL,
    PRIMARY KEY (publicacion_id, orden),
    CONSTRAINT ck_promocion_tipo CHECK (tipo IN ('PORCENTAJE', 'MONTO_FIJO')),
    CONSTRAINT fk_promocion_publicacion FOREIGN KEY (publicacion_id) REFERENCES publicacion (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE transicion_publicacion (
    publicacion_id BIGINT NOT NULL,
    orden INT NOT NULL,
    fecha DATETIME(6) NOT NULL,
    actor_id VARCHAR(100) NOT NULL,
    origen VARCHAR(20) NOT NULL,
    destino VARCHAR(20) NOT NULL,
    motivo VARCHAR(500) NULL,
    PRIMARY KEY (publicacion_id, orden),
    CONSTRAINT fk_transicion_publicacion FOREIGN KEY (publicacion_id) REFERENCES publicacion (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE contratacion (
    id BIGINT NOT NULL AUTO_INCREMENT,
    publicacion_id BIGINT NOT NULL,
    cliente_id VARCHAR(100) NOT NULL,
    proveedor_id VARCHAR(100) NOT NULL,
    mensaje VARCHAR(1000) NULL,
    objetivo VARCHAR(500) NULL,
    franja_inicio DATETIME(6) NOT NULL,
    franja_fin DATETIME(6) NOT NULL,
    precio_acordado BIGINT NOT NULL,
    monto_retenido BIGINT NOT NULL DEFAULT 0,
    politica_cancelacion VARCHAR(10) NOT NULL,
    fecha_solicitud DATETIME(6) NOT NULL,
    fecha_aceptacion DATETIME(6) NULL,
    fecha_completada DATETIME(6) NULL,
    estado VARCHAR(12) NOT NULL,
    propuesta_por VARCHAR(100) NULL,
    propuesta_inicio DATETIME(6) NULL,
    propuesta_fin DATETIME(6) NULL,
    propuesta_estado VARCHAR(10) NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT ck_contratacion_estado CHECK (estado IN
        ('PENDIENTE', 'ACEPTADA', 'CONFIRMADA', 'EN_CURSO', 'COMPLETADA', 'RECHAZADA', 'CANCELADA')),
    CONSTRAINT fk_contratacion_publicacion FOREIGN KEY (publicacion_id) REFERENCES publicacion (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX ix_contratacion_publicacion ON contratacion (publicacion_id, estado);
CREATE INDEX ix_contratacion_cliente ON contratacion (cliente_id, estado);
CREATE INDEX ix_contratacion_proveedor ON contratacion (proveedor_id, estado);

CREATE TABLE transicion_contratacion (
    contratacion_id BIGINT NOT NULL,
    orden INT NOT NULL,
    fecha DATETIME(6) NOT NULL,
    actor_id VARCHAR(100) NOT NULL,
    origen VARCHAR(20) NOT NULL,
    destino VARCHAR(20) NOT NULL,
    motivo VARCHAR(500) NULL,
    PRIMARY KEY (contratacion_id, orden),
    CONSTRAINT fk_transicion_contratacion FOREIGN KEY (contratacion_id) REFERENCES contratacion (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE calificacion (
    id BIGINT NOT NULL AUTO_INCREMENT,
    contratacion_id BIGINT NOT NULL,
    autor_id VARCHAR(100) NOT NULL,
    receptor_id VARCHAR(100) NOT NULL,
    puntaje INT NOT NULL,
    comentario VARCHAR(1000) NULL,
    fecha DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_calificacion_autor UNIQUE (contratacion_id, autor_id),
    CONSTRAINT ck_calificacion_puntaje CHECK (puntaje BETWEEN 1 AND 5),
    CONSTRAINT fk_calificacion_contratacion FOREIGN KEY (contratacion_id) REFERENCES contratacion (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE notificacion (
    id BIGINT NOT NULL AUTO_INCREMENT,
    destinatario_id VARCHAR(100) NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    mensaje VARCHAR(500) NOT NULL,
    fecha DATETIME(6) NOT NULL,
    leida BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX ix_notificacion_destinatario ON notificacion (destinatario_id, fecha);
